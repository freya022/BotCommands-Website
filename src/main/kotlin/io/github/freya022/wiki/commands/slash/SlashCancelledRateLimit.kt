package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.utils.awaitUnit

@Suppress("unused")
class SlashCancelledRateLimit {

    // --8<-- [start:cancelled_rate_limit-kotlin]
    suspend fun onSlashRateLimit(event: GuildSlashEvent) {
        // Assuming we have voice states cached
        if (!event.member.voiceState!!.inAudioChannel()) {
            // Note that this would be easier done using a filter,
            // as no token would be used, and would also be cleaner.
            event.cancelRateLimit()
            return event.reply_("You must be in a voice channel").awaitUnit()
        }

        event.reply("Hello world!").await()
    }
    // --8<-- [end:cancelled_rate_limit-kotlin]
}