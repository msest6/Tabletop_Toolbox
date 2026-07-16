package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tabletoptoolbox.data.model.UnoModel
import com.example.tabletoptoolbox.data.model.UnoRound
import kotlinx.serialization.json.Json

private object UnoKeys {
    val PLAYERS = Preference(stringPreferencesKey("unoPlayers"), "")
    val LOOSE_SCORE = Preference(intPreferencesKey("unoLoseScore"), 1000)
    val DJELITELJ = Preference(intPreferencesKey("unoDjelitelj"), 0)
    val GAME_RECORD = Preference(stringPreferencesKey("unoGameRecord"), "")
}

class UnoRepository(private val prefs: PreferencesDataStore) {
    suspend fun savePlayers(players: List<UnoModel>) {
        prefs.set(UnoKeys.PLAYERS, Json.encodeToString(players))
    }

    suspend fun loadPlayers(): List<UnoModel> {
        val jsonString = prefs.get(UnoKeys.PLAYERS)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }

    suspend fun getLooseScore(): Int = prefs.get(UnoKeys.LOOSE_SCORE)

    suspend fun setLooseScore(score: Int) {
        prefs.set(UnoKeys.LOOSE_SCORE, score)
    }

    suspend fun getDjelitelj(): Int = prefs.get(UnoKeys.DJELITELJ)

    suspend fun setDjelitelj(index: Int) {
        prefs.set(UnoKeys.DJELITELJ, index)
    }

    suspend fun saveGame(rounds: List<UnoRound>) {
        prefs.set(UnoKeys.GAME_RECORD, Json.encodeToString(rounds))
    }

    suspend fun loadGame(): List<UnoRound> {
        val jsonString = prefs.get(UnoKeys.GAME_RECORD)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }
}