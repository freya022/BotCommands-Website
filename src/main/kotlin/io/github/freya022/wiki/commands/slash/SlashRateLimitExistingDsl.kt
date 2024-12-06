package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.wiki.ratelimit.WikiRateLimitProvider
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:rate_limit_existing-kotlin_dsl]
@Command
class SlashRateLimitExistingDsl : GlobalApplicationCommandProvider {

    suspend fun onSlashRateLimit(event: GuildSlashEvent) {
        event.reply("Hello world!").await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("rate_limit_existing_dsl", function = ::onSlashRateLimit) {
            // Use the rate limiter we defined in [[WikiRateLimitProvider]]
            rateLimitReference(WikiRateLimitProvider.RATE_LIMIT_GROUP)
        }
    }
}
// --8<-- [end:rate_limit_existing-kotlin_dsl]