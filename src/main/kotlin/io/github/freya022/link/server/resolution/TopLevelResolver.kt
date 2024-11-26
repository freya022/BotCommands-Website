package io.github.freya022.link.server.resolution

import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.metadata.jvm.KotlinClassMetadata

object TopLevelResolver {

    private val logger = KotlinLogging.logger { }

    fun singleTopLevel(identifier: String): LinkRepresentation {
        val candidates = apiClasses.flatMap { kotlinClass ->
            try {
                getTopLevelCandidates(kotlinClass, identifier)
            } catch (e: LinkException) {
                logger.trace(e) { "Failed to get member candidates of ${kotlinClass.simpleNestedName}" }
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

    private fun getTopLevelCandidates(kotlinClass: KotlinClass, identifier: String): List<LinkRepresentation> {
        val metadata = kotlinClass.metadata
        val kmPackage = (metadata as? KotlinClassMetadata.FileFacade)?.kmPackage
            ?: return emptyList()

        val baseLink = kmPackage.getBaseLink(kotlinClass)
        val functionCandidates = kmPackage.functions
            .filter { function -> function.name == identifier }
            // Good news! This takes the *first* one, just like how overload resolution works in Dokka!
            .distinctBy { function -> function.name }
            .map { function -> LinkRepresentation(function.toSimpleString(), "$baseLink/${function.name.toKDocCase()}.html") }

        val propertyCandidates = kmPackage.properties
            .filter { property -> property.name == identifier }
            .map { property -> LinkRepresentation(identifier, "$baseLink/${property.name.toKDocCase()}.html") }

        return functionCandidates + propertyCandidates
    }
}