package com.example.tabletoptoolbox.graddrzava

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.GradDrzavaRepository

class GradDrzavaViewModelFactory(
    private val repository: GradDrzavaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GradDrzavaViewModel(repository) as T
    }
}