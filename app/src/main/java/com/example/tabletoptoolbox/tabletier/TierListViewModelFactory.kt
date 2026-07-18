package com.example.tabletoptoolbox.tabletier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.boardGames.GameRepository

class TierListViewModelFactory(
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TierListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TierListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}