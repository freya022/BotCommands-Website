# Using components

!!! warning "This requires a [database](using-botcommands/database.md) to be set up!"

Components provided by the framework are your usual Discord components with additional features,
they can be configured to:

- Be usable once
- Have timeouts
- Have method handlers or callbacks
- Have constraints (allow list for users/roles/permissions)

To get access to them, you can use the [[Buttons]] and [[SelectMenus]] factories,
as well as [[Components]] to delete them manually and make groups.

!!! note "Configuring components with Java"

    When configuring components, you need to use the framework's methods first, 
    and then use the JDA methods, and finally build.

!!! tip "Disabling classes depending on components"

    You can use [[RequiresComponents]] if you want your class to be disabled when the components are not available.

## Persistent components
They are components that still work after a restart,
their handlers are methods identified by their handler name,
set in [[JDAButtonListener]] / [[JDASelectMenuListener]].

Persistent components have a default timeout set in [[Components#defaultPersistentTimeout]],
which can be changed.

!!! info

    Components which expired while the bot was offline will run their timeout handlers at startup.

!!! Example
    === "Kotlin"
        In Kotlin, we can use extensions to make sure we call our component handlers in a type-safe manner.
        This way, you will have a compiler error if the handler and the arguments don't match,
        it will also allow using handlers without setting a name.
    
        This can only be used when the input argument types matches the handler parameter types.
    
        !!! note
            A similar `timeoutWith` function exists for timeouts.
    
        ```kotlin
        --8<-- "wiki/commands/slash/SlashClicker.kt:persistent-clicker-kotlin"
        ```
    
        You can also use components without setting a handler, and instead await the event:
    
        ```kotlin
        --8<-- "wiki/commands/slash/SlashClickWaiter.kt:click_waiter-kotlin"
        ```
    
        1. [[awaitOrNull]] returns `null` when the component expired, useful when combined with an elvis operator,
        this is the equivalent of a `#!java try catch` on `TimeoutCancellationException`.
        Since there is no timeout set here, the [default duration][[Components#defaultEphemeralTimeout]] is used.
    
        2. [[awaitUnit]] is an extension to await and then return `Unit`, 
        which helps in common scenarios where you want to reply using an elvis operator.
    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashClickerPersistent.java:persistent-clicker-java"
        ```

## Ephemeral components
They are components which get invalidated after a restart, meaning they can no longer be used,
their handlers are callbacks, which can also have a timeout set, and also use callbacks.

!!! info

    "Invalidated" means that they are deleted from the database, but not necessarily from the message.

Ephemeral components have a default timeout set in [[Components#defaultEphemeralTimeout]], which can be changed.

!!! Example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashClicker.kt:ephemeral-clicker-kotlin"
        ```
    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashClickerEphemeral.java:ephemeral-clicker-java"
        ```

## Component groups
Component groups can be created in any component factory, and allow you to configure one timeout for all components.

Also, when one of them gets invalidated (after being used with [`singleUse = true`][[IUniqueComponent#singleUse]]),
the entire group gets invalidated.

For example, this can be useful when the user needs to use a single component, once.

!!! warning "Ephemeral components in groups"

    If you put ephemeral components in your group, you must disable the timeout with [`noTimeout()`][[ITimeoutableComponent#noTimeout]].

The timeout works similarly to components, except the annotated handler is a [[GroupTimeoutHandler]].

!!! Example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashClickGroup.kt:click_group-kotlin"
        ```
    
    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashClickGroup.java:click_group-java"
        ```

## Reset timeout on use
The [`resetTimeoutOnUse`][[ITimeoutableComponent#resetTimeoutOnUse]] lets you reset the timeout each time the button is clicked.
The timeout is only reset if the button was actually used, it will not be reset if unauthorized users use it.

## Deleting components
Here are some tips on how to delete components:

- Most likely, you have the message (from a `ButtonEvent` for example) and you want to delete the buttons from the message and invalidate them,
in this case you should use [`deleteRows`][[AbstractComponentFactory#deleteRows]].
- In stateful interactions, where components can be **re**used,
you might sometimes want to store the IDs of the components,
to then invalidate them when the interaction expires.

    !!! example
        The built-in paginators stores all the `int` IDs of the components used in paginators,
        as they cannot be deleted on each page change, as the user might reuse a component they made themselves.
        Storing them this way is more efficient and allows deletion when the paginator expires, using [`deleteRows`][[AbstractComponentFactory#deleteComponentsByIds]].

- In other, rare cases, you have the component instances (not the JDA ones), for which you can use [`deleteComponents`][[AbstractComponentFactory#deleteComponents]]

## Filtering
Components also support filtering, you can use `addFilter` with either the filter type, or the filter instance directly.

!!! failure "Passing custom filter instances"

    You cannot pass filters that cannot be obtained via dependency injection,
    this includes composite filters (using `and` / `or`), 
    see [[ComponentInteractionFilter]] for more details

### Creating a filter

Creating a filter can be done
by implementing [[ComponentInteractionFilter]]
and registering it as a service, 
they run when a component is about to be executed.

Lets create a filter that allows the component to be usable in a predefined one channel:

!!! note

    Your filter needs to *not* be global in order to be used on specific components.

=== "Kotlin"
    ```kotlin
    --8<-- "wiki/filters/GeneralChannelFilter.kt:component_filter-kotlin"
    ```

    1. This is the return type of the filter, this will be passed as `userData` in your rejection handler.

=== "Java"
    ```java
    --8<-- "wiki/java/filters/GeneralChannelFilter.java:component_filter-java"
    ```

    1. This is the return type of the filter, this will be passed as `userData` in your rejection handler.

### Creating a rejection handler

You must then create **a single** [rejection handler][[ComponentInteractionRejectionHandler]] for **all your filters**, 
it runs when one of your filters fails.

!!! note

    All of your filters must have the same return type as the rejection handler (the generic you set on the interface).

=== "Kotlin"
    ```kotlin
    --8<-- "wiki/filters/ComponentRejectionHandler.kt:component_rejection_handler-kotlin"
    ```

    1. This is what was returned by one of your filters, this will be passed as `userData`.

=== "Java"
    ```java
    --8<-- "wiki/java/filters/ComponentRejectionHandler.java:component_rejection_handler-java"
    ```

    1. This is what was returned by one of your filters, this will be passed as `userData`.

### Using an existing filter
Now that your filter has been created, you can reference it in your component.

=== "Kotlin"
    ```kotlin
    buttons.primary("Can't click me").ephemeral {
        filters += filter<GeneralChannelFilter>()
    }
    ```

=== "Java"
    ```java
    buttons.primary("Can't click me")
        .ephemeral()
        .addFilter(GeneralChannelFilter.class)
        .build()
    ```

## Rate limiting
Just like application commands, components can be rate limited.
However, you will need to help the library differentiate components from each other (unlike commands which are differentiated by their names).

You will first need to create a [[ComponentRateLimitReference]],
you can do that with [`createRateLimitReference`][[AbstractComponentFactory#createRateLimitReference]],
present in any component factory ([[Components]], [[Buttons]], [[SelectMenus]]).

The `group` associated with the `discriminator` will need to be unique,
as to differentiate components (referenced by `discriminator`) using the same rate limiter (referenced by `group`).

!!! example
    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/SlashComponentRateLimit.kt:component_rate_limit-kotlin"
        ```
    
    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/SlashComponentRateLimit.java:component_rate_limit-java"
        ```