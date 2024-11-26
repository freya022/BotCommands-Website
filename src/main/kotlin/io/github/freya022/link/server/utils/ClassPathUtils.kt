package io.github.freya022.link.server.utils

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.metadata.jvm.KotlinClassMetadata

val apiClasses: List<ClassInfo> = ClassGraph()
    .enableClassInfo()
    .enableAnnotationInfo()
    .acceptPackages("io.github.freya022.botcommands.api")
    .scan()
    .allClasses

fun List<ClassInfo>.filterBySimpleName(simpleClassName: String): List<ClassInfo> =
    filter { classInfo -> classInfo.simpleNestedName.replace('$', '.') == simpleClassName }

fun readMetadata(classInfo: ClassInfo): KotlinClassMetadata? {
    val metadataAnnotation = classInfo.annotationInfo.directOnly()[Metadata::class.java.name]?.loadClassAndInstantiate() as Metadata?
        ?: return null
    return KotlinClassMetadata.readStrict(metadataAnnotation)
}