package io.github.freya022.link.server.resolution

import io.github.classgraph.ClassInfo
import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.metadata.jvm.KotlinClassMetadata

object TopLevelResolver {

    private val logger = KotlinLogging.logger { }

    fun singleTopLevel(identifier: String): LinkRepresentation {
        val candidates = apiClasses.flatMap { clazz ->
            try {
                getTopLevelCandidates(clazz, identifier)
            } catch (e: LinkException) {
                logger.trace(e) { "Failed to get member candidates of ${clazz.simpleNestedName}" }
                emptyList()
            }
        }
        if (candidates.isEmpty()) {
            throw LinkException("'$identifier' is neither a top-level function or property")
        } else if (candidates.size > 1) {
            throw LinkException("Found multiple candidates for '$identifier':\n${candidates.joinToString("\n") { it.url } }")
        } else {
            return candidates.first()
        }
    }

    private fun getTopLevelCandidates(clazz: ClassInfo, identifier: String): List<LinkRepresentation> {
        if (!clazz.annotations.directOnly().containsName(Metadata::class.java.name))
            return emptyList()

        val metadata = readMetadata(clazz)
            ?: return emptyList()
        val kmPackage = (metadata as? KotlinClassMetadata.FileFacade)?.kmPackage
            ?: return emptyList()

        val functionCandidates = kmPackage.functions
            .filter { function -> function.name == identifier }
            .map { function -> LinkRepresentation(function.toSimpleString(), "${kmPackage.getBaseLink(clazz)}/${function.name.toKDocCase()}.html") }

        val propertyCandidates = kmPackage.properties
            .filter { property -> property.name == identifier }
            .map { property -> LinkRepresentation(identifier, "${kmPackage.getBaseLink(clazz)}/${property.name.toKDocCase()}.html") }

        return functionCandidates + propertyCandidates
    }
}