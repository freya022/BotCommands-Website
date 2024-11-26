package io.github.freya022.link.server.resolution

import io.github.classgraph.ClassInfo
import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.utils.*
import kotlin.metadata.jvm.KotlinClassMetadata

object TopLevelResolver {

    fun singleTopLevelFunction(identifier: String): LinkRepresentation {
        val candidates = apiClasses.mapNotNull { clazz -> getTopLevelFunctionOrNull(clazz, identifier) }
        if (candidates.isEmpty()) {
            throw LinkException("'$identifier' is neither a top-level function or property")
        } else if (candidates.size > 1) {
            throw LinkException("Found multiple candidates for '$identifier':\n${candidates.joinToString("\n") { it.url } }")
        } else {
            return candidates.first()
        }
    }

    private fun getTopLevelFunctionOrNull(clazz: ClassInfo, identifier: String): LinkRepresentation? {
        if (!clazz.annotations.directOnly().containsName(Metadata::class.java.name))
            return null

        val metadata = readMetadata(clazz)
            ?: return null
        val kmPackage = (metadata as? KotlinClassMetadata.FileFacade)?.kmPackage
            ?: return null

        val func = kmPackage.functions.firstOrNull { function -> function.name == identifier }
        if (func != null) {
            return LinkRepresentation(
                func.toSimpleString(),
                "${kmPackage.getBaseLink(clazz)}/${func.name.toKDocCase()}.html"
            )
        }

        val prop = kmPackage.properties.firstOrNull { property -> property.name == identifier }
        if (prop != null) {
            return LinkRepresentation(
                identifier,
                "${kmPackage.getBaseLink(clazz)}/${prop.name.toKDocCase()}.html"
            )
        }

        return null
    }
}