package com.example.tabletoptoolbox.bela

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.BelaRepository

class BelaViewModelFactory(
    private val repository: BelaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BelaViewModel(repository) as T
    }
}