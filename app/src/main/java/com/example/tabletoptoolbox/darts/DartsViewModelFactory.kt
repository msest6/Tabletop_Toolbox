package com.example.tabletoptoolbox.darts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.DartsRepository
import com.example.tabletoptoolbox.data.model.CheckoutTable

class DartsViewModelFactory(
    private val repository: DartsRepository,
    private val checkoutTable: List<CheckoutTable>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DartsViewModel(repository, checkoutTable) as T
    }
}