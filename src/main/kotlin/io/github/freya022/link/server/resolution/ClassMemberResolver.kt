package io.github.freya022.link.server.resolution

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.utils.*
import kotlin.metadata.jvm.KotlinClassMetadata

object ClassMemberResolver {

    fun singleMember(identifier: String): LinkRepresentation {
        val (className, memberName) = identifier.split("#")
        val classes = apiClasses.filterBySimpleName(className)
        if (classes.isEmpty()) {
            throw LinkException("'$className' was not found")
        } else {
            val candidates = classes.mapNotNull { classInfo -> getMemberOrNull(classInfo, memberName) }
            if (candidates.isEmpty()) {
                throw LinkException("'$memberName' is neither a function, property or enum value in '$className'")
            } else if (candidates.size > 1) {
                throw LinkException("Found multiple candidates for '$identifier':\n${candidates.joinToString("\n") { it.url }}")
            } else {
                return candidates.first()
            }
        }
    }

    private fun getMemberOrNull(classInfo: ClassInfo, memberName: String): LinkRepresentation? {
        val className = classInfo.name
        val memberLabel = "${classInfo.simpleNestedName}.$memberName"

        val metadata = readMetadata(classInfo)
            ?: throw LinkException("'$className' is not a Kotlin class")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: throw LinkException("'$className' is not a class")
        val func = kmClass.functions.firstOrNull { function -> function.name == memberName }
        if (func != null)
            return LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${func.name.toKDocCase()}.html")

        val prop = kmClass.properties.firstOrNull { property -> property.name == memberName }
        if (prop != null)
            return LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${prop.name.toKDocCase()}.html")

        val enumEntry = kmClass.enumEntries.firstOrNull { enumEntry -> enumEntry == memberName }
        if (enumEntry != null)
            return LinkRepresentation(
                memberLabel,
                "${kmClass.getBaseLink(classInfo)}/${enumEntry.toKDocCase()}/index.html"
            )

        return null
    }
}