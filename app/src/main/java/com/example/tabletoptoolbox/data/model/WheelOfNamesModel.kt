package com.example.tabletoptoolbox.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Wheel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val entries: List<String> = emptyList()
)