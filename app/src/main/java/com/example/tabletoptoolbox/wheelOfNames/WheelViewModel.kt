package com.example.tabletoptoolbox.wheelOfNames

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.model.Wheel
import com.example.tabletoptoolbox.data.WheelRepository
import kotlinx.coroutines.launch

class WheelViewModel(private val repository: WheelRepository) : ViewModel() {

    var wheels by mutableStateOf<List<Wheel>>(emptyList())
        private set

    private var loaded = false

    fun loadIfNeeded() {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            wheels = repository.getWheels()
        }
    }

    fun getWheel(id: String): Wheel? = wheels.find { it.id == id }

    // Poziva se tek na "Spremi" u detail screenu
    fun upsert(wheel: Wheel) {
        val idx = wheels.indexOfFirst { it.id == wheel.id }
        wheels = if (idx >= 0) wheels.toMutableList().apply { this[idx] = wheel }
        else wheels + wheel
        viewModelScope.launch { repository.setWheels(wheels) }
    }

    fun delete(id: String) {
        wheels = wheels.filterNot { it.id == id }
        viewModelScope.launch { repository.setWheels(wheels) }
    }
}