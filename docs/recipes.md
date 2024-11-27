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