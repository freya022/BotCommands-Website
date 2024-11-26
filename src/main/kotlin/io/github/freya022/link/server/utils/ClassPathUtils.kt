package io.github.freya022.link.server.utils

import io.github.classgraph.ClassGraph
import kotlin.metadata.jvm.KotlinClassMetadata

private val metadataAnnotationName = Metadata::class.java.name

val apiClasses: List<KotlinClass> = ClassGraph()
    .enableClassInfo()
    .enableAnnotationInfo()
    .acceptPackages("io.github.freya022.botcommands.api")
    .scan()
    .allClasses
    // Only keep Kotlin classes
    .mapNotNull { classInfo ->
        val metadataAnnotationInfo = classInfo.annotationInfo.directOnly()[metadataAnnotationName] ?: return@mapNotNull null
        val metadata = metadataAnnotationInfo.loadClassAndInstantiate() as Metadata
        KotlinClass(classInfo, KotlinClassMetadata.readStrict(metadata))
    }

fun List<KotlinClass>.filterBySimpleName(simpleClassName: String): List<KotlinClass> =
    filter { classInfo -> classInfo.simpleNestedName.replace('$', '.') == simpleClassName }
