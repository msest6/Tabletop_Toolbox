package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tabletoptoolbox.data.model.BoardGamePlayer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private object BoardGameKeys {
    val PLAYERS = Preference(stringPreferencesKey("boardGamePlayers"), "")
    val WIN_SCORE = Preference(intPreferencesKey("boardGameWinScore"), 0)
}

class BoardGameRepository(private val prefs: PreferencesDataStore) {

    suspend fun savePlayers(players: List<BoardGamePlayer>) {
        prefs.set(BoardGameKeys.PLAYERS, Json.encodeToString(players))
    }

    suspend fun loadPlayers(): List<BoardGamePlayer> {
        val jsonString = prefs.get(BoardGameKeys.PLAYERS)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }

    suspend fun getWinScore(): Int = prefs.get(BoardGameKeys.WIN_SCORE)

    suspend fun setWinScore(score: Int) {
        prefs.set(BoardGameKeys.WIN_SCORE, score)
    }
}