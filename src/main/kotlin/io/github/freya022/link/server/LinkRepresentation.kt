package io.github.freya022.link.server

import kotlinx.serialization.Serializable

@Serializable
data class LinkRepresentation(
    val label: String,
    val url: String,
//    val tooltip: String,
)