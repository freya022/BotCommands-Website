package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.wiki.filters.RequiredRoles

// Test class for [[RequiredRoles]]
@Command
class SlashStaffOnly : ApplicationCommand() {

    @RequiredRoles(1322683021924499516)
    @JDASlashCommand(name = "staff_only")
    fun onSlashStaffOnly(event: GuildSlashEvent) {
        event.reply_("You are staff", ephemeral = true).queue()
    }
}