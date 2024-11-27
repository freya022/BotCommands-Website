package io.github.freya022.link.server.resolution

import io.github.freya022.link.server.*
import io.github.freya022.link.server.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.metadata.jvm.KotlinClassMetadata

object ClassMemberResolver {

    private val logger = KotlinLogging.logger { }

    fun singleMember(request: LinkRequest): LinkRepresentation {
        val (className, memberName) = request.identifier.split("#")
        val classes = apiClasses.filterBySimpleName(className)
        if (classes.isEmpty()) {
            throw LinkException("'$className' was not found")
        } else {
            val candidates = classes.flatMap { kotlinClass ->
                try {
                    getMemberCandidates(request, kotlinClass, memberName)
                } catch (e: LinkException) {
                    logger.trace(e) { "Failed to get member candidates of ${kotlinClass.simpleNestedName}" }
                    emptyList()
                }
            }.distinctBy { it.url }

            if (candidates.isEmpty()) {
                throw LinkException("'$memberName' is neither a function, property or enum value in '$className'")
            } else if (candidates.size > 1) {
                throw LinkException("Found multiple candidates for '$request':\n${candidates.joinToString("\n") { it.url }}")
            } else {
                return candidates.first()
            }
        }
    }

    private fun getMemberCandidates(request: LinkRequest, kotlinClass: KotlinClass, memberName: String, displayClassName: String = kotlinClass.simpleNestedName): List<LinkRepresentation> {
        val memberLabel = "$displayClassName.$memberName"

        val kmClass = (kotlinClass.metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: throw LinkException("'${kotlinClass.simpleNestedName}' is not a class")

        val baseLink = kmClass.getBaseLink(kotlinClass)
        val functionCandidates = when {
            request.functionsRequested -> kmClass.functions
                .filter { function -> function.name == memberName }
                .map { function -> LinkRepresentation(memberLabel, "$baseLink/${function.name.toKDocCase()}.html") }
            else -> emptyList()
        }

        val propertyCandidates = when {
            request.propertiesRequested -> kmClass.properties
                .filter { property -> property.name == memberName }
                .map { property -> LinkRepresentation(memberLabel, "$baseLink/${property.name.toKDocCase()}.html") }
            else -> emptyList()
        }

        val enumEntryCandidates = kmClass.enumEntries
            .filter { enumEntry -> enumEntry == memberName }
            .map { enumEntry -> LinkRepresentation(memberLabel, "$baseLink/${enumEntry.toKDocCase()}/index.html") }

        val companionCandidates = kmClass.companionObject?.let { companionObjectName ->
            val companionNestedName = "${kotlinClass.simpleNestedName}.$companionObjectName"
            val companionKotlinClass = apiClasses.first { apiClass -> apiClass.simpleNestedName == companionNestedName }
            getMemberCandidates(
                request,
                companionKotlinClass,
                // Don't show the .Companion
                memberName,
                displayClassName,
            )
        } ?: emptyList()

        return functionCandidates + propertyCandidates + enumEntryCandidates + companionCandidates
    }
}