package io.github.freya022.wiki.filters

import io.github.freya022.botcommands.api.commands.annotations.Filter

@Filter(RequiredRolesFilter::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class RequiredRoles(@get:JvmName("value") vararg val roles: Long)
