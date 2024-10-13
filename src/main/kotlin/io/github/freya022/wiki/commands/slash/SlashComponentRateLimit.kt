package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.utils.lazyJDAEmoji
import io.github.freya022.wiki.java.ratelimit.WikiRateLimitProvider
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:component_rate_limit-kotlin]
@Command
class SlashComponentRateLimit(private val buttons: Buttons) : ApplicationCommand() {
    // This is to prevent an unnecessary load on startup, emojis are slow
    private val arrowUp: UnicodeEmoji by lazyJDAEmoji("arrow_up")
    private val arrowDown: UnicodeEmoji by lazyJDAEmoji("arrow_down")

    // The combination of the group and discriminator must be unique
    private val upvoteRateLimitReference = buttons.createRateLimitReference(
        // The name of the rate limiter
        group = WikiRateLimitProvider.RATE_LIMIT_GROUP,
        // The "discriminator" for this component,
        // this is important as this differentiates a component from others
        discriminator = "upvote"
    )
    private val downvoteRateLimitReference = buttons.createRateLimitReference(
        group = WikiRateLimitProvider.RATE_LIMIT_GROUP,
        discriminator = "downvote"
    )

    @JDASlashCommand(name = "component_rate_limit")
    suspend fun onSlashComponentRateLimit(event: GuildSlashEvent) {
        val upvoteButton = buttons.success("Upvote", arrowUp).ephemeral {
            rateLimitReference(upvoteRateLimitReference)

            bindTo { buttonEvent ->
                buttonEvent.reply("Your vote has been taken into account").setEphemeral(true).queue()
            }
        }

        val downvoteButton = buttons.danger("Downvote", arrowDown).ephemeral {
            rateLimitReference(downvoteRateLimitReference)

            bindTo { buttonEvent ->
                buttonEvent.reply("Your anger has been taken into account").setEphemeral(true).queue()
            }
        }

        event.reply("[Insert controversial sentence]")
            .addActionRow(upvoteButton, downvoteButton)
            .await()
    }
}
// --8<-- [end:component_rate_limit-kotlin]
