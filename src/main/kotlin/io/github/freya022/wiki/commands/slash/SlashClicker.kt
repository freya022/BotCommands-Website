package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.builder.timeoutWith
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import kotlinx.coroutines.withTimeout
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

// Exists only for @TopLevelSlashCommandData
@Command
class SlashDummyClicker : ApplicationCommand() {
    @JDASlashCommand(name = "clicker", subcommand = "dummy")
    @TopLevelSlashCommandData
    fun onSlashClicker(event: GuildSlashEvent) {
        event.reply_("Unused", ephemeral = true).queue()
    }
}

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:persistent-clicker-kotlin]
@Command
class SlashPersistentClicker(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "clicker", subcommand = "persistent", description = "Creates a button you can infinitely click")
    suspend fun onSlashClicker(event: GuildSlashEvent) {
        val button = createButton(event, count = 0)
        event.replyComponents(row(button)).await()
    }

    // No need for a name if you use the type-safe "bindWith" extensions
    @JDAButtonListener
    suspend fun onCookieClick(event: ButtonEvent, @ComponentData count: Int) {
        val button = createButton(event, count + 1)
        event.editButton(button).await()
    }

    // Same thing here, no name required
    @ComponentTimeoutHandler
    fun onCookieTimeout(timeout: ComponentTimeoutData, @TimeoutData count: Int) {
        println("User finished clicking $count cookies")
    }

    private suspend fun createButton(event: Interaction, @ComponentData count: Int): Button {
        // Create a primary-styled button
        return buttons.primary("$count cookies")
            // Sets the emoji on the button,
            // this can be an unicode emoji, an alias or even a custom emoji
            .withEmoji("cookie")

            // Create a button that can be used even after a restart
            .persistent {
                // Only allow the caller to use the button
                constraints += event.user

                // Timeout and call the method after the button hasn't been used for a day
                // The timeout gets cancelled if the button is invalidated
                timeoutWith(1.days, ::onCookieTimeout, count)

                // When clicked, run the onCookieClick method with the count
                // Extension for type-safe binding, no need to type the name
                bindWith(::onCookieClick, count)
            }
    }
}
// --8<-- [end:persistent-clicker-kotlin]

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:ephemeral-clicker-kotlin]
@Command
class SlashEphemeralClicker(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "clicker", subcommand = "ephemeral", description = "Creates a button you can click until the bot restarts or 15 minutes later")
    suspend fun onSlashClicker(event: GuildSlashEvent) {
        val button = createButton(event, count = 0)
        event.replyComponents(row(button)).await()
    }

    private suspend fun createButton(event: IDeferrableCallback, count: Int): Button {
        // Create a primary-styled button
        return buttons.primary("$count cookies")
            // Sets the emoji on the button,
            // this can be an unicode emoji, an alias or even a custom emoji
            .withEmoji("cookie")

            // Create a button that can be used until the bot restarts
            .ephemeral {
                // Only allow the caller to use the button
                constraints += event.user

                // Run this callback 15 minutes after the button has been created
                // The timeout gets cancelled if the button is invalidated
                timeout(15.minutes) {
                    if (!event.hook.isExpired) {
                        event.hook.retrieveOriginal()
                            .map { it.components.asDisabled() }
                            .flatMap { event.hook.editOriginalComponents(it) }
                            .queue()
                        event.hook.sendMessage("You clicked $count cookies!").setEphemeral(true).queue()
                    } else {
                        println("User finished clicking $count cookies")
                    }
                }

                // When clicked, run this callback
                bindTo { buttonEvent ->
                    val newButton = createButton(buttonEvent, count + 1)
                    buttonEvent.editButton(newButton).await()
                }
            }
    }
}
// --8<-- [end:ephemeral-clicker-kotlin]