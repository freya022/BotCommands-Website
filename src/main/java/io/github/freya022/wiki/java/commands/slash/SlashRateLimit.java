package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.*;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;

import java.time.temporal.ChronoUnit;

@SuppressWarnings({"DefaultAnnotationParam"})
@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:rate_limit-java]
@Command
public class SlashRateLimit extends ApplicationCommand {

    // A rate limit for this slash command only
    @RateLimit(
            // Apply to each user, regardless of channel/guild
            scope = RateLimitScope.USER,
            // Delete the message telling the user about the remaining rate limit after it expires
            deleteOnRefill = true,
            // At least one of those needs to be empty for the command to be rejected
            bandwidths = {
                    // 5 uses, 5 tokens gets added gradually over an hour (so, 1 token every 12 minutes)
                    @Bandwidth(
                            capacity = 5, // 5 uses
                            refill = @Refill(
                                    type = RefillType.GREEDY,       // Gradually
                                    tokens = 5,                     // gives 5 tokens
                                    period = 1,                     // every 1
                                    periodUnit = ChronoUnit.HOURS   // hour
                            )
                    ),
                    // 2 uses, 2 tokens gets added at once after 2 minutes
                    // This is to prevent the user from spamming the command
                    @Bandwidth(
                            capacity = 2, // 2 uses
                            refill = @Refill(
                                    type = RefillType.INTERVAL,     // At once,
                                    tokens = 2,                     // give 2 tokens
                                    period = 2,                     // every 2
                                    periodUnit = ChronoUnit.MINUTES // minutes
                            )
                    ),
            }
    )
    @JDASlashCommand(name = "rate_limit")
    public void onSlashRateLimit(GuildSlashEvent event) {
        event.reply("Hello world!").queue();
    }
}
// --8<-- [end:rate_limit-java]