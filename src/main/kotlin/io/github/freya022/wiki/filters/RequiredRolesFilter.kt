package io.github.freya022.wiki.filters

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.core.findAnnotation
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

private val logger = KotlinLogging.logger { }

@BService
@WikiLanguage(WikiLanguage.Language.KOTLIN)
class RequiredRolesFilter : ApplicationCommandFilter<String> {

    override val global: Boolean get() = false

    override fun check(event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): String? {
        val annotation = commandInfo.findAnnotation<RequiredRoles>() ?: return null

        val member = event.member
        if (member == null) {
            logger.warn { "@RequiredRoles can only be used on guild-only commands: $commandInfo" }
            return "You are missing required roles"
        }

        val missingRoles = getMissingRoles(annotation, member)
        if (missingRoles.isNotEmpty()) {
            logger.trace { "Denied access to $member as they are missing required roles: $missingRoles" }
            return "You are missing required roles"
        }

        return null
    }

    private fun getMissingRoles(annotation: RequiredRoles, member: Member): List<Role> {
        val guild = member.guild
        val requiredRoles = annotation.roles
            .map { id ->
                val role = guild.getRoleById(id)
                if (role == null) logger.warn { "Could not find role with ID $id in guild $guild" }
                role
            }
            .filterNotNull()

        return requiredRoles - member.roles.toSet()
    }
}