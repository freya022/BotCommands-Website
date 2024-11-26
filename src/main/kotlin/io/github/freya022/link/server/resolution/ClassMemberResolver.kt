package io.github.freya022.link.server.resolution

import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.metadata.jvm.KotlinClassMetadata

object ClassMemberResolver {

    private val logger = KotlinLogging.logger { }

    fun singleMember(identifier: String): LinkRepresentation {
        val (className, memberName) = identifier.split("#")
        val classes = apiClasses.filterBySimpleName(className)
        if (classes.isEmpty()) {
            throw LinkException("'$className' was not found")
        } else {
            val candidates = classes.flatMap { kotlinClass ->
                try {
                    getMemberCandidates(kotlinClass, memberName)
                } catch (e: LinkException) {
                    logger.trace(e) { "Failed to get member candidates of ${kotlinClass.simpleNestedName}" }
                    emptyList()
                }
            }
            if (candidates.isEmpty()) {
                throw LinkException("'$memberName' is neither a function, property or enum value in '$className'")
            } else if (candidates.size > 1) {
                throw LinkException("Found multiple candidates for '$identifier':\n${candidates.joinToString("\n") { it.url }}")
            } else {
                return candidates.first()
            }
        }
    }

    private fun getMemberCandidates(kotlinClass: KotlinClass, memberName: String): List<LinkRepresentation> {
        val className = kotlinClass.simpleNestedName
        val memberLabel = "$className.$memberName"

        val kmClass = (kotlinClass.metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: throw LinkException("'$className' is not a class")

        val baseLink = kmClass.getBaseLink(kotlinClass)
        val functionCandidates = kmClass.functions
            .filter { function -> function.name == memberName }
            // Good news! This takes the *first* one, just like how overload resolution works in Dokka!
            .distinctBy { function -> function.name }
            .map { function -> LinkRepresentation(memberLabel, "$baseLink/${function.name.toKDocCase()}.html") }

        val propertyCandidates = kmClass.properties
            .filter { property -> property.name == memberName }
            .map { property -> LinkRepresentation(memberLabel, "$baseLink/${property.name.toKDocCase()}.html") }

        val enumEntryCandidates = kmClass.enumEntries
            .filter { enumEntry -> enumEntry == memberName }
            .map { enumEntry -> LinkRepresentation(memberLabel, "$baseLink/${enumEntry.toKDocCase()}/index.html") }

        return functionCandidates + propertyCandidates + enumEntryCandidates
    }
}