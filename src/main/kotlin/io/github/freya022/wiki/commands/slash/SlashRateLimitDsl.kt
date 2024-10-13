package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:rate_limit-kotlin_dsl]
@Command
class SlashRateLimitDsl : GlobalApplicationCommandProvider {

    suspend fun onSlashRateLimit(event: GuildSlashEvent) {
        event.reply("Hello world!").await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("rate_limit_dsl", function = ::onSlashRateLimit) {
            // Lets the user use the command 5 times in an hour, but also 2 times in 2 minutes to prevent spam
            val bucketConfiguration = Buckets.spikeProtected(
                capacity = 5,               // 5 uses
                duration = 1.hours,         // Gives 5 tokens gradually, during an hour (1 token every 12 minutes)
                spikeCapacity = 2,          // 2 uses
                spikeDuration = 2.minutes   // Give 2 tokens every 2 minutes
            )

            val rateLimiter = RateLimiter.createDefault(
                // Apply to each user, regardless of channel/guild
                RateLimitScope.USER,
                // Delete the message telling the user about the remaining rate limit after it expires
                deleteOnRefill = true,
                // Give our constant bucket configuration
                configurationSupplier = bucketConfiguration.toSupplier()
            )

            rateLimit(rateLimiter)
        }
    }
}
// --8<-- [end:rate_limit-kotlin_dsl]