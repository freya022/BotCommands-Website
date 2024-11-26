package io.github.freya022.link.server.resolution

import io.github.freya022.link.server.LinkException
import io.github.freya022.link.server.LinkRepresentation
import io.github.freya022.link.server.utils.apiClasses
import io.github.freya022.link.server.utils.filterBySimpleName
import io.github.freya022.link.server.utils.getBaseLink
import kotlin.metadata.ClassKind
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.kind

object ClassResolver {

    fun singleClass(className: String): LinkRepresentation {
        val classes = apiClasses.filterBySimpleName(className)
        if (classes.isEmpty()) {
            throw LinkException("'$className' was not found")
        } else if (classes.size > 1) {
            throw LinkException("Found multiple candidates for '$className': ${classes.joinToString { it.fullName }}")
        } else {
            val kotlinClass = classes.first()
            val kmClass = (kotlinClass.metadata as? KotlinClassMetadata.Class)?.kmClass
                ?: throw LinkException("'$className' is not a class")

            val baseLink = kmClass.getBaseLink(kotlinClass)
            return when (kmClass.kind) {
                ClassKind.ANNOTATION_CLASS -> LinkRepresentation("#!java @$className", "$baseLink/index.html")
                else -> LinkRepresentation(className, "$baseLink/index.html")
            }
        }
    }
}