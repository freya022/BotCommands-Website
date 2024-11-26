package io.github.freya022.link.server.resolution

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
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
            val candidates = classes.flatMap { classInfo ->
                try {
                    getMemberCandidates(classInfo, memberName)
                } catch (e: LinkException) {
                    logger.trace(e) { "Failed to get member candidates of ${classInfo.simpleNestedName}" }
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

    private fun getMemberCandidates(classInfo: ClassInfo, memberName: String): List<LinkRepresentation> {
        val className = classInfo.name
        val memberLabel = "${classInfo.simpleNestedName}.$memberName"

        val metadata = readMetadata(classInfo)
            ?: throw LinkException("'$className' is not a Kotlin class")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: throw LinkException("'$className' is not a class")
        val functionCandidates = kmClass.functions
            .filter { function -> function.name == memberName }
            .map { function -> LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${function.name.toKDocCase()}.html") }

        val propertyCandidates = kmClass.properties
            .filter { property -> property.name == memberName }
            .map { property -> LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${property.name.toKDocCase()}.html") }

        val enumEntryCandidates = kmClass.enumEntries
            .filter { enumEntry -> enumEntry == memberName }
            .map { enumEntry -> LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${enumEntry.toKDocCase()}/index.html") }

        return functionCandidates + propertyCandidates + enumEntryCandidates
    }
}