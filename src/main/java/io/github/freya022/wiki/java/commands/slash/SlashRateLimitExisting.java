package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.wiki.java.ratelimit.WikiRateLimitProvider;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:rate_limit_existing-java]
@Command
public class SlashRateLimitExisting extends ApplicationCommand {

    // A rate limit for this slash command only
    @RateLimitReference(WikiRateLimitProvider.RATE_LIMIT_GROUP)
    @JDASlashCommand(name = "rate_limit_existing")
    public void onSlashRateLimit(GuildSlashEvent event) {
        event.reply("Hello world!").queue();
    }
}
// --8<-- [end:rate_limit_existing-java]