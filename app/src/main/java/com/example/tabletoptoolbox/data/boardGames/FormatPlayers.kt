package com.example.tabletoptoolbox.data.boardGames

fun GameEntity.formatPlayers(): String {
    val max = maxPlayers
    return when {
        max == null -> "$minPlayers"
        max == "+" -> "$minPlayers+"
        else -> "$minPlayers-$max"
    }
}