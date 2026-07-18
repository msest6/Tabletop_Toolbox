package com.example.tabletoptoolbox.data.boardGames

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val imagePath: String?,
    val userRating: Float,
    val eloRating: Double = 1200.0,
    val comparisonCount: Int = 0,
    val playTimeMinutes: Int,
    val minPlayers: Int,
    val maxPlayers: String?
)