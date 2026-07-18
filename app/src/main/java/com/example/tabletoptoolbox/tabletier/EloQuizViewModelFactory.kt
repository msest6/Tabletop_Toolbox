package com.example.tabletoptoolbox.tabletier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.boardGames.GameRepository

class EloQuizViewModelFactory(
    private val repository: GameRepository,
    private val targetGameId: Long? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EloQuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EloQuizViewModel(repository, targetGameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}