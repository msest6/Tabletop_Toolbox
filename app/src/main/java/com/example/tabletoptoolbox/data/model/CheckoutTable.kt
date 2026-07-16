package com.example.tabletoptoolbox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutTable(
    val score: Int,
    val hand: List<String>
)