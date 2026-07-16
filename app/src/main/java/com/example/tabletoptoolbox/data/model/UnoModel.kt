package com.example.tabletoptoolbox.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnoModel(
    var playerName: String,
    var score: Int,
    var looseNumber: Int
) {

}