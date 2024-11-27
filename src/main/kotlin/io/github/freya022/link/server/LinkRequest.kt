package io.github.freya022.link.server

import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.enumSetOfAll
import io.github.freya022.botcommands.api.core.utils.unmodifiableView

private val flagRegex = Regex("""(\w+)=(\w+)""")

class LinkRequest(
    query: String,
) {
    enum class DeclarationType {
        FUNCTION,
        PROPERTY,
    }

    val identifier: String = query.substringBefore(" ")
    val declarationTypes: Set<DeclarationType>

    init {
        val flags: Map<String, String> = flagRegex.findAll(query.substringAfter(" "))
            .map { it.groupValues }
            .associate { (_, key, value) -> key to value }

        declarationTypes = when (val typeFlag = flags["type"]) {
            null -> enumSetOfAll<DeclarationType>().unmodifiableView()
            else -> typeFlag
                .split("|")
                .mapTo(enumSetOf()) { declType -> DeclarationType.entries.first { it.name.contentEquals(declType, ignoreCase = true) } }
                .unmodifiableView()
        }
    }

    override fun toString(): String {
        return "LinkRequest(identifier='$identifier', declarationTypes=$declarationTypes)"
    }
}

val LinkRequest.propertiesRequested: Boolean get() = LinkRequest.DeclarationType.PROPERTY in declarationTypes
val LinkRequest.functionsRequested: Boolean get() = LinkRequest.DeclarationType.FUNCTION in declarationTypes