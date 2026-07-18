package com.example.tabletoptoolbox.tabletier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.boardGames.GameRepository

class TableTierViewModelFactory(
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TableTierViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TableTierViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}