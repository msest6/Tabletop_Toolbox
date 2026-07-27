package com.example.tabletoptoolbox.wheelOfNames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabletoptoolbox.data.WheelRepository

class WheelViewModelFactory(
    private val repository: WheelRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        WheelViewModel(repository) as T
}