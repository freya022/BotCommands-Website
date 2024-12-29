# Recipes

!!! note

    Contributions to recipes are welcome,
    you can do so by contributing directly, or by suggesting (in the [support server](https://discord.gg/frpCcQfvTz))
    what topics should be explained, how they should be structured, give relevant use cases/examples...

## Components
### Awaiting a component with coroutines

You can also use components without setting a handler, and instead await the event:

```kotlin
--8<-- "wiki/commands/slash/SlashClickWaiter.kt:click_waiter-kotlin"
```

1. [[awaitOrNull]] returns `null` when the component expired, useful when combined with an elvis operator,
this is the equivalent of a `#!java try catch` on `TimeoutCancellationException`.
Since there is no timeout set here, the [default duration][[Components#defaultEphemeralTimeout]] is used.

2. [[awaitUnit]] is an extension to await and then return `Unit`, 
which helps in common scenarios where you want to reply using an elvis operator.

## Annotated runtime filters
!!! note

    As any runtime filter, you will also need a single rejection handler,
    see [for application commands][[ApplicationCommandRejectionHandler]],
    [for text commands][[TextCommandRejectionHandler]].

### Gating a command with a single role

Instead of using `#!java @Filter` directly, we use it as a meta-annotation to improve readability,
as well as making it easier to copy the role requirement.

=== "Kotlin"
    

=== "Java"
    

You can then use the annotation such as `#!java @RequiresStaffRole` on your text/application command,
you are also able to use multiple of these filters with different roles if necessary.

### Gating a command with values from the filter annotation

This shows how to gate a command with permissions,
of course this already exists with [[UserPermissions]],
but this is to show how you can read the values of the command.

=== "Kotlin"
    

=== "Java"
    

You can then use the annotation such as `#!java @RequiredPermissions(Permission.MANAGE_SERVER)` on your text/application command.