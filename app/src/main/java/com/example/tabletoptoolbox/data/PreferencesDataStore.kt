package com.example.tabletoptoolbox.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Opisuje jedan ključ u DataStoreu zajedno s njegovom default vrijednosti.
 * Repozitoriji definiraju svoje instance ovoga - ova klasa ne zna ništa
 * o tome za koju se igru ključ koristi.
 */
data class Preference<T>(
    val key: Preferences.Key<T>,
    val defaultValue: T
)

/**
 * Tanki, generički sloj iznad Jetpack DataStorea.
 * Ne sadrži nikakvu logiku specifičnu za pojedinu igru -
 * to je posao repozitorija (DartsRepository, BelaRepository, ...).
 */
class PreferencesDataStore(private val context: Context) {

    suspend fun <T> get(preference: Preference<T>): T {
        return context.dataStore.data.first()[preference.key] ?: preference.defaultValue
    }

    suspend fun <T> set(preference: Preference<T>, value: T) {
        context.dataStore.edit { settings ->
            settings[preference.key] = value
        }
    }
}