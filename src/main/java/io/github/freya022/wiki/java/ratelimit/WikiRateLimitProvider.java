package io.github.freya022.wiki.java.ratelimit;

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope;
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter;
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier;
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets;
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager;
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:rate_limit_provider-java]
@BService
public class WikiRateLimitProvider implements RateLimitProvider {
    // The name of the rate limit, so you can reference it in your commands/components
    public static final String RATE_LIMIT_GROUP = "Wiki";

    @Override
    public void declareRateLimit(@NotNull RateLimitManager rateLimitManager) {
        // --8<-- [start:bucket_configuration-java]
        // Lets the user use the command 5 times in an hour, but also 2 times in 2 minutes to prevent spam
        final var bucketConfiguration = Buckets.createSpikeProtected(
                5,                      // 5 uses
                Duration.ofHours(1),    // Gives 5 tokens gradually, during an hour (1 token every 12 minutes)
                2,                      // 2 uses
                Duration.ofMinutes(2)   // Give 2 tokens every 2 minutes
        );
        // --8<-- [end:bucket_configuration-java]

        // --8<-- [start:rate_limiter-java]
        final var rateLimiter = RateLimiter.createDefault(
                // Apply to each user, regardless of channel/guild
                RateLimitScope.USER,
                // Give our constant bucket configuration
                BucketConfigurationSupplier.constant(bucketConfiguration),
                // Delete the message telling the user about the remaining rate limit after it expires
                true
        );
        // --8<-- [end:rate_limiter-java]

        // Register
        rateLimitManager.rateLimit(RATE_LIMIT_GROUP, rateLimiter);
    }
}
// --8<-- [end:rate_limit_provider-java]
