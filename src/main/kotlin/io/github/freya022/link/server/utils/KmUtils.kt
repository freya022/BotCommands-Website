package io.github.freya022.link.server.utils

import kotlin.metadata.*
import kotlin.metadata.jvm.moduleName

fun KotlinMember.toSimpleString(): String = buildString {
    receiverParameterType?.let { receiver ->
        this.append(receiver.toSimpleString(this@toSimpleString))
        this.append(".")
    }
    this.append(name)
}

fun KmType?.toSimpleString(func: KotlinMember): String {
    if (this == null) return "*"

    val classifierStr = when (val argClassifier = classifier) {
        is KmClassifier.Class -> argClassifier.simpleNestedName
        is KmClassifier.TypeParameter -> func.typeParameters.single { it.id == argClassifier.id }.name
        else -> error("Unsupported classifier: $argClassifier")
    }

    val argumentsStr = if (arguments.isNotEmpty()) {
        "<${arguments.joinToString(", ") { projection -> projection.toSimpleString(func) }}>"
    } else {
        ""
    }

    return classifierStr + argumentsStr
}

fun KmTypeProjection.toSimpleString(func: KotlinMember): String = buildString {
    if (variance == KmVariance.IN) {
        append("in ")
    } else if (variance == KmVariance.OUT) {
        append("out ")
    }

    append(type.toSimpleString(func))
}

val KmClassifier.Class.simpleNestedName: String
    get() = name.dropWhile { !it.isUpperCase() }.replace('/', '.')

fun KmPackage.getBaseLink(kotlinClass: KotlinClass): String {
    val module = moduleName!!.toKDocCase()
    val packageName = kotlinClass.packageName
    return "https://docs.bc.freya02.dev/$module/$packageName"
}

fun KmClass.getBaseLink(kotlinClass: KotlinClass): String {
    val module = moduleName!!.toKDocCase()
    val packageName = kotlinClass.packageName
    val classNames = kotlinClass.simpleNestedName.split(".").joinToString("/") { it.toKDocCase() }
    return "https://docs.bc.freya02.dev/$module/$packageName/$classNames"
}