package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tabletoptoolbox.data.model.Wheel
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private object WheelKeys {
    val WHEELS = Preference(stringPreferencesKey("wheelOfNamesWheels"), "[]")
}

class WheelRepository(private val prefs: PreferencesDataStore) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getWheels(): List<Wheel> =
        json.decodeFromString(prefs.get(WheelKeys.WHEELS))

    suspend fun setWheels(wheels: List<Wheel>) {
        prefs.set(WheelKeys.WHEELS, json.encodeToString(wheels))
    }

    // Convenience: read-modify-write nad cijelom kolekcijom
    suspend fun getWheel(id: String): Wheel? =
        getWheels().find { it.id == id }

    suspend fun upsertWheel(wheel: Wheel) {
        val wheels = getWheels()
        val idx = wheels.indexOfFirst { it.id == wheel.id }
        setWheels(
            if (idx >= 0) wheels.toMutableList().apply { this[idx] = wheel }
            else wheels + wheel
        )
    }

    suspend fun deleteWheel(id: String) {
        setWheels(getWheels().filterNot { it.id == id })
    }
}