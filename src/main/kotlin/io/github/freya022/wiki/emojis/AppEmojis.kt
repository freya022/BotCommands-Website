package io.github.freya022.wiki.emojis

import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry
import io.github.freya022.botcommands.api.emojis.annotations.AppEmoji
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji

// --8<-- [start:eager_app_emojis-kotlin]
@AppEmojiContainer
object AppEmojis {
    @AppEmoji(emojiName = "kotlin_eager") // Optionally use this if you want to change defaults
    val kotlin: ApplicationEmoji by AppEmojisRegistry
}
// --8<-- [end:eager-app-emojis-kotlin]