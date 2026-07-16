package com.example.tabletoptoolbox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DartThrow(
    val playerIndex: Int,
    val throws: List<Int>
)