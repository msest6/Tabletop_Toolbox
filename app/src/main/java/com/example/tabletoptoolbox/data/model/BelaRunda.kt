package com.example.tabletoptoolbox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BelaRunda(
    val scores: List<Int>,
    val zvanja: List<Int> = List(scores.size) { 0 }
)