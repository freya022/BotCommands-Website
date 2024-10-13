package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.wiki.ratelimit.WikiRateLimitProvider
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:rate_limit_existing-kotlin]
@Command
class SlashRateLimitExisting : ApplicationCommand() {

    // A rate limit for this slash command only
    @RateLimitReference(WikiRateLimitProvider.RATE_LIMIT_GROUP)
    @JDASlashCommand(name = "rate_limit_existing")
    suspend fun onSlashRateLimit(event: GuildSlashEvent) {
        event.reply("Hello world!").await()
    }
}
// --8<-- [end:rate_limit_existing-kotlin]
