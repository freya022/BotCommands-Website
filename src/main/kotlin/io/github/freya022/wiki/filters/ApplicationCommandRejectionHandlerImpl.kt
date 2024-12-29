package io.github.freya022.wiki.filters

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

@BService
class ApplicationCommandRejectionHandlerImpl : ApplicationCommandRejectionHandler<String> {

    override fun handle(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo, userData: String) {
        // We happen to have the 'userData' be the message to send,
        // but you can pass anything and then have more useful error messages, logs...
        event.reply(userData)
            .setEphemeral(true)
            .queue()
    }
}
