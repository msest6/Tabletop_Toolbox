package com.example.tabletoptoolbox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BoardGamePlayer(
    var playerName: String,
    var score: Int,
    var winNumber: Int
)