package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class SlashCancelledRateLimit {
    // --8<-- [start:cancelled_rate_limit-java]
    public void onSlashRateLimit(GuildSlashEvent event) {
        // Assuming we have voice states cached
        if (!event.getMember().getVoiceState().inAudioChannel()) {
            // Note that this would be easier done using a filter,
            // as no token would be used, and would also be cleaner.
            event.cancelRateLimit();
            event.reply("You must be in a voice channel").queue();
            return;
        }

        event.reply("Hello world!").queue();
    }
    // --8<-- [end:cancelled_rate_limit-java]
}
