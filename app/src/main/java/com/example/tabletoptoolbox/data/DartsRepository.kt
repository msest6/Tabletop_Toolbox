package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tabletoptoolbox.data.model.DartThrow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private object DartsKeys {
    val PLAYER_NAME_LIST = Preference(stringPreferencesKey("dartsPlayerNameList"), "")
    val ACTIVE_GAME = Preference(stringPreferencesKey("dartsActiveGame"), "")
    val DOUBLE_IN = Preference(booleanPreferencesKey("dartsDoubleIn"), false)
    val DOUBLE_OUT = Preference(booleanPreferencesKey("dartsDoubleOut"), true)
    val GAME_RECORD = Preference(stringPreferencesKey("dartsGameRecord"), "")
    val CURRENT_PLAYER = Preference(intPreferencesKey("dartsCurrentPlayer"), 0)
}

class DartsRepository(private val prefs: PreferencesDataStore) {

    suspend fun getPlayerNameList(): List<String> =
        prefs.get(DartsKeys.PLAYER_NAME_LIST)
            .split(",")
            .filter { it.isNotBlank() }

    suspend fun savePlayerNameList(names: List<String>) {
        prefs.set(DartsKeys.PLAYER_NAME_LIST, names.joinToString(","))
    }

    suspend fun getActiveGame(): String = prefs.get(DartsKeys.ACTIVE_GAME)

    suspend fun setActiveGame(game: String) {
        prefs.set(DartsKeys.ACTIVE_GAME, game)
    }

    suspend fun getDoubleIn(): Boolean = prefs.get(DartsKeys.DOUBLE_IN)

    suspend fun setDoubleIn(value: Boolean) {
        prefs.set(DartsKeys.DOUBLE_IN, value)
    }

    suspend fun getDoubleOut(): Boolean = prefs.get(DartsKeys.DOUBLE_OUT)

    suspend fun setDoubleOut(value: Boolean) {
        prefs.set(DartsKeys.DOUBLE_OUT, value)
    }

    suspend fun getCurrentPlayer(): Int = prefs.get(DartsKeys.CURRENT_PLAYER)

    suspend fun setCurrentPlayer(index: Int) {
        prefs.set(DartsKeys.CURRENT_PLAYER, index)
    }

    suspend fun saveGame(moves: List<DartThrow>) {
        prefs.set(DartsKeys.GAME_RECORD, Json.encodeToString(moves))
    }

    suspend fun loadGame(): List<DartThrow> {
        val jsonString = prefs.get(DartsKeys.GAME_RECORD)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }
}