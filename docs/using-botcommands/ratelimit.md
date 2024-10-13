# Using rate limits
Rate limits lets you reject interactions when they are used too much in a time span.

!!! abstract "For more details, check out the [docs of Bucket4J](https://bucket4j.com/8.14.0/toc.html)"

## Defining a rate limit
Rate limits can be defined by implementing [`RateLimitProvider`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.declaration/-rate-limit-provider/index.html),
the overridden function will run before registering commands, so you can use them anywhere.

### Defining bucket configurations
A `BucketConfiguration` defines what the limits are, you can supply different configurations based on the context,
by implementing a [`BucketConfigurationSupplier`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.bucket/-bucket-configuration-supplier/index.html), or you can create a configuration with the factories in [`Buckets`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.bucket/-buckets/index.html),
or with the [`BucketConfiguration` builder](https://bucket4j.com/8.14.0/toc.html#bucket-bonfiguration).

!!! tip

    If you want to give the same configuration regardless of context,
    you can use [`BucketConfigurationSupplier.constant(BucketConfiguration)`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.bucket/-bucket-configuration-supplier/-companion/constant.html) (or [`BucketConfiguration#toSupplier()`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.bucket/to-supplier.html) for Kotlin users)

!!! example

    === "Kotlin"
        ```kotlin
--8<-- "wiki/ratelimit/WikiRateLimitProvider.kt:bucket_configuration-kotlin"
        ```

    === "Java"
        ```java
--8<-- "wiki/java/ratelimit/WikiRateLimitProvider.java:bucket_configuration-java"
        ```

### Creating a rate limiter
The [default rate limiters](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit/-rate-limiter/-companion/index.html) should provide you ready-made implementations for both in-memory and proxied buckets,
refer to the example attached to them.

!!! example

    === "Kotlin"
        ```kotlin
--8<-- "wiki/ratelimit/WikiRateLimitProvider.kt:rate_limiter-kotlin"
        ```

    === "Java"
        ```java
--8<-- "wiki/java/ratelimit/WikiRateLimitProvider.java:rate_limiter-java"
        ```

However, you can also create a custom one by implementing [`RateLimiter`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit/-rate-limiter/index.html), which is the combination of:

- [`BucketAccessor`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.bucket/-bucket-accessor/index.html): Retrieves a [Bucket] based on the context
- [`RateLimitHandler`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.handler/-rate-limit-handler/index.html): Handles when an interaction has been rate limited (often to tell the user about it)

!!! tip

    When making a custom rate limiter, you can delegate one of the default implementations to avoid reimplementing existing behavior.

### Registering the rate limiter
You can now register using [`RateLimitManager#rateLimit(String, RateLimiter)`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit.declaration/-rate-limit-manager/rate-limit.html),
the `group` (name of the rate limiter) must be unique.

!!! example "Full example"

    === "Kotlin"
        ```kotlin
        --8<-- "wiki/ratelimit/WikiRateLimitProvider.kt:rate_limit_provider-kotlin"
        ```

    === "Java"
        ```java
        --8<-- "wiki/java/ratelimit/WikiRateLimitProvider.java:rate_limit_provider-java"
        ```

## Cancelling rate limits
If your interaction does an early return, you can also return the token with [`cancelRateLimit()`](https://docs.bc.freya02.dev/-bot-commands/io.github.freya022.botcommands.api.commands.ratelimit/-cancellable-rate-limit/cancel-rate-limit.html), 
so the user won't get penalized for this interaction.

!!! example

    === "Kotlin"
        ```kotlin
    --8<-- "wiki/commands/slash/SlashCancelledRateLimit.kt:cancelled_rate_limit-kotlin"
        ```

    === "Java"
        ```java
    --8<-- "wiki/java/commands/slash/SlashCancelledRateLimit.java:cancelled_rate_limit-java"
        ```