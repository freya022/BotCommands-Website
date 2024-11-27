package io.github.freya022.link.server.resolution

import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.LinkRequest
import io.github.freya022.link.server.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.metadata.jvm.KotlinClassMetadata

object TopLevelResolver {

    private val logger = KotlinLogging.logger { }

    fun singleTopLevel(request: LinkRequest): LinkRepresentation {
        val candidates = apiClasses.flatMap { kotlinClass ->
            try {
                getTopLevelCandidates(kotlinClass, request)
            } catch (e: LinkException) {
                logger.trace(e) { "Failed to get member candidates of ${kotlinClass.simpleNestedName}" }
                emptyList()
            }
        }.distinctBy { it.url }

        if (candidates.isEmpty()) {
            throw LinkException("'$request' is neither a top-level function or property")
        } else if (candidates.size > 1) {
            throw LinkException("Found multiple candidates for '$request':\n${candidates.joinToString("\n") { it.url } }")
        } else {
            return candidates.first()
        }
    }

    private fun getTopLevelCandidates(kotlinClass: KotlinClass, request: LinkRequest): List<LinkRepresentation> {
        val metadata = kotlinClass.metadata
        val kmPackage = (metadata as? KotlinClassMetadata.FileFacade)?.kmPackage
            ?: return emptyList()

        val baseLink = kmPackage.getBaseLink(kotlinClass)
        val functionCandidates = kmPackage.functions
            .filter { function -> function.name == request.identifier }
            .map(::KotlinFunction)
            .map { function -> LinkRepresentation(function.toSimpleString(), "$baseLink/${function.name.toKDocCase()}.html") }

        val propertyCandidates = kmPackage.properties
            .filter { property -> property.name == request.identifier }
            .map(::KotlinProperty)
            .map { property -> LinkRepresentation(property.toSimpleString(), "$baseLink/${property.name.toKDocCase()}.html") }

        return functionCandidates + propertyCandidates
    }
}