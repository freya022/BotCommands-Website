package io.github.freya022.wiki.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:rate_limit_provider-kotlin]
@BService
class WikiRateLimitProvider : RateLimitProvider {

    override fun declareRateLimit(manager: RateLimitManager) {
        // --8<-- [start:bucket_configuration-kotlin]
        // Lets the user use the command 5 times in an hour, but also 2 times in 2 minutes to prevent spam
        val bucketConfiguration = Buckets.spikeProtected(
            capacity = 5,               // 5 uses
            duration = 1.hours,         // Gives 5 tokens gradually, during an hour (1 token every 12 minutes)
            spikeCapacity = 2,          // 2 uses
            spikeDuration = 2.minutes   // Give 2 tokens every 2 minutes
        )
        // --8<-- [end:bucket_configuration-kotlin]

        // --8<-- [start:rate_limiter-kotlin]
        val rateLimiter = RateLimiter.createDefault(
            // Apply to each user, regardless of channel/guild
            RateLimitScope.USER,
            // Delete the message telling the user about the remaining rate limit after it expires
            deleteOnRefill = true,
            // Give our constant bucket configuration
            configurationSupplier = bucketConfiguration.toSupplier()
        )
        // --8<-- [end:rate_limiter-kotlin]

        // Register, any command can use it
        manager.rateLimit(RATE_LIMIT_GROUP, rateLimiter)
    }

    companion object {
        // The name of the rate limit, so you can reference it in your commands/components
        const val RATE_LIMIT_GROUP = "Wiki"
    }
}
// --8<-- [end:rate_limit_provider-kotlin]