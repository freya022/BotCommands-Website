package io.github.freya022.link.server.utils

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.metadata.jvm.KotlinClassMetadata

class KotlinClass(
    val classInfo: ClassInfo,
    val metadata: KotlinClassMetadata,
) {

    val packageName: String get() = classInfo.packageName
    val fullName: String get() = classInfo.name
    val simpleNestedName: String get() = classInfo.simpleNestedName
}