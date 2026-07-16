package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tabletoptoolbox.data.model.ThemeMode

private object ThemeKeys {
    val THEME = Preference(stringPreferencesKey("theme"), "LIGHT")
}

class ThemeRepository(private val prefs: PreferencesDataStore) {

    suspend fun getThemeMode(): ThemeMode {
        val str = prefs.get(ThemeKeys.THEME)
        return ThemeMode.valueOf(str)
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        prefs.set(ThemeKeys.THEME, themeMode.name)
    }
}