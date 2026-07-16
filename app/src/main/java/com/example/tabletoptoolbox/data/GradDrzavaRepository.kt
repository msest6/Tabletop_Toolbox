package com.example.tabletoptoolbox.data

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

private object GradDrzavaKeys {
    val SCORE = Preference(intPreferencesKey("gradDrzavaScore"), 0)
    val CATEGORY_LIST = Preference(stringPreferencesKey("gradDrzavaCategoryList"), "")
    val CURRENT_LETTER = Preference(stringPreferencesKey("gradDrzavaCurrentLetter"), "A")
}

class GradDrzavaRepository(private val prefs: PreferencesDataStore) {

    suspend fun getScore(): Int = prefs.get(GradDrzavaKeys.SCORE)

    suspend fun setScore(score: Int) {
        prefs.set(GradDrzavaKeys.SCORE, score)
    }

    suspend fun getCategoryList(): List<String> =
        prefs.get(GradDrzavaKeys.CATEGORY_LIST)
            .split(",")
            .filter { it.isNotBlank() }

    suspend fun setCategoryList(categories: List<String>) {
        prefs.set(GradDrzavaKeys.CATEGORY_LIST, categories.joinToString(","))
    }

    suspend fun getCurrentLetter(): String = prefs.get(GradDrzavaKeys.CURRENT_LETTER)

    suspend fun setCurrentLetter(letter: String) {
        prefs.set(GradDrzavaKeys.CURRENT_LETTER, letter)
    }
}