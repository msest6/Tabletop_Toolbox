package com.example.tabletoptoolbox.tabletier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.boardGames.GameEntity
import com.example.tabletoptoolbox.data.boardGames.GameRepository
import kotlinx.coroutines.flow.*

class TierListViewModel(
    private val repository: GameRepository
) : ViewModel() {

    val tieredGames: StateFlow<Map<Tier, List<GameEntity>>> = repository.getAllGames()
        .map { games ->
            val sorted = games.sortedByDescending { it.eloRating }
            assignTiers(sorted)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _selectedGame = MutableStateFlow<GameEntity?>(null)
    val selectedGame: StateFlow<GameEntity?> = _selectedGame.asStateFlow()

    fun onGameClicked(game: GameEntity) {
        _selectedGame.value = game
    }

    fun onDialogDismissed() {
        _selectedGame.value = null
    }
}