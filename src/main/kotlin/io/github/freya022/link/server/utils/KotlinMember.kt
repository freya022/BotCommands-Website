package io.github.freya022.link.server.utils

import kotlin.metadata.KmFunction
import kotlin.metadata.KmProperty
import kotlin.metadata.KmType
import kotlin.metadata.KmTypeParameter

sealed interface KotlinMember {
    val name: String
    val receiverParameterType: KmType?
    val typeParameters: List<KmTypeParameter>
}

@JvmInline
value class KotlinFunction(val kmFunction: KmFunction) : KotlinMember {

    override val name: String get() = kmFunction.name
    override val receiverParameterType: KmType? get() = kmFunction.receiverParameterType
    override val typeParameters: List<KmTypeParameter> get() = kmFunction.typeParameters
}

@JvmInline
value class KotlinProperty(val kmProperty: KmProperty) : KotlinMember {

    override val name: String get() = kmProperty.name
    override val receiverParameterType: KmType? get() = kmProperty.receiverParameterType
    override val typeParameters: List<KmTypeParameter> get() = kmProperty.typeParameters
}