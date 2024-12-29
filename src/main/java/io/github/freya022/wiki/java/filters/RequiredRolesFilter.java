package io.github.freya022.wiki.java.filters;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo;
import io.github.freya022.botcommands.api.core.Logging;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.filters.RequiredRoles;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@WikiLanguage(WikiLanguage.Language.JAVA)
@BService
public class RequiredRolesFilter implements ApplicationCommandFilter<String> {
    private static final Logger LOGGER = Logging.getLogger();

    @Override
    public boolean getGlobal() {
        return false;
    }

    @Nullable
    @Override
    public String check(@NotNull GenericCommandInteractionEvent event, @NotNull ApplicationCommandInfo commandInfo) {
        final var annotation = commandInfo.findAnnotation(RequiredRoles.class);
        if (annotation == null) return null;

        final var member = event.getMember();
        if (member == null) {
            LOGGER.warn("@RequiredRoles can only be used on guild-only commands: {}", commandInfo);
            return "You are missing required roles";
        }

        final var missingRoles = getMissingRoles(annotation, member);
        if (!missingRoles.isEmpty()) {
            LOGGER.trace("Denied access to {} as they are missing required roles: {}", member, missingRoles);
            return "You are missing required roles";
        }

        return null;
    }

    @NotNull
    private static List<Role> getMissingRoles(@NotNull RequiredRoles annotation, @NotNull Member member) {
        final var guild = member.getGuild();
        return Arrays.stream(annotation.value())
                .mapToObj(id -> {
                    final var role = guild.getRoleById(id);
                    if (role == null) {
                        LOGGER.warn("Could not find role with ID {} in guild {}", id, guild);
                    }

                    return role;
                })
                .filter(Objects::nonNull)
                .filter(r -> !member.getRoles().contains(r))
                .toList();
    }
}
