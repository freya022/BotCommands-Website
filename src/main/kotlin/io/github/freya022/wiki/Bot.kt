package io.github.freya022.wiki

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.light
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.config.Config
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:jdaservice-kotlin]
@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> = defaultIntents(/* _Additional_ intents */ GatewayIntent.GUILD_VOICE_STATES)

    override val cacheFlags: Set<CacheFlag> = setOf(/* _Additional_ cache flags */ CacheFlag.VOICE_STATE)

    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        light(
            config.token,
            activity = Activity.customStatus("In Kotlin with ❤️")
        ) {
            // Other configs
        }
    }
}
// --8<-- [end:jdaservice-kotlin]