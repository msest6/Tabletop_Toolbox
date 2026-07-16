package com.example.tabletoptoolbox.boardgames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.BoardGameRepository

class BoardGameViewModelFactory(
    private val repository: BoardGameRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BoardGameViewModel(repository) as T
    }
}