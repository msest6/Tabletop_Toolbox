package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tabletoptoolbox.data.model.BelaMode
import com.example.tabletoptoolbox.data.model.BelaRunda
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private object BelaKeys {
    val DJELITELJ = Preference(intPreferencesKey("belaDjelitelj"), 0)
    val GAME_RECORD = Preference(stringPreferencesKey("belaGameRecord"), "")
    val PLAYER_LIST = Preference(stringPreferencesKey("belaPlayerList"), "")
    val GAME_SCORE = Preference(intPreferencesKey("belaGameScore"), 1001)
    val MODE = Preference(stringPreferencesKey("belaMode"), BelaMode.DEFAULT.name)
    val WINS = Preference(stringPreferencesKey("belaWins"), "")
}

class BelaRepository(private val prefs: PreferencesDataStore) {

    private val defaultPlayers = listOf("ja", "lijevi", "suigrač", "desni")

    suspend fun getDjelitelj(): Int = prefs.get(BelaKeys.DJELITELJ)

    suspend fun setDjelitelj(index: Int) {
        prefs.set(BelaKeys.DJELITELJ, index)
    }

    suspend fun saveGame(rounds: List<BelaRunda>) {
        prefs.set(BelaKeys.GAME_RECORD, Json.encodeToString(rounds))
    }

    suspend fun loadGame(): List<BelaRunda> {
        val jsonString = prefs.get(BelaKeys.GAME_RECORD)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }

    suspend fun getPlayerList(): List<String> {
        val list = prefs.get(BelaKeys.PLAYER_LIST).split(",").filter { it.isNotBlank() }
        return list.ifEmpty { defaultPlayers }
    }

    suspend fun setPlayerList(players: List<String>) {
        prefs.set(BelaKeys.PLAYER_LIST, players.joinToString(","))
    }

    suspend fun getGameScore(): Int = prefs.get(BelaKeys.GAME_SCORE)

    suspend fun setGameScore(score: Int) {
        prefs.set(BelaKeys.GAME_SCORE, score)
    }

    suspend fun getMode(): BelaMode {
        val stored = prefs.get(BelaKeys.MODE)
        return BelaMode.entries.find { it.name == stored } ?: BelaMode.DEFAULT
    }

    suspend fun setMode(mode: BelaMode) {
        prefs.set(BelaKeys.MODE, mode.name)
    }
    suspend fun getWins(): List<Int> {
        val stored = prefs.get(BelaKeys.WINS)
        return if (stored.isBlank()) emptyList() else stored.split(",").mapNotNull { it.toIntOrNull() }
    }

    suspend fun setWins(wins: List<Int>) {
        prefs.set(BelaKeys.WINS, wins.joinToString(","))
    }
}