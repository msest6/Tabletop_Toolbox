package com.example.tabletoptoolbox.tabletier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.boardGames.GameRepository
import com.example.tabletoptoolbox.data.boardGames.GameEntity
import com.example.tabletoptoolbox.data.boardGames.SortColumn
import com.example.tabletoptoolbox.data.boardGames.SortDirection
import com.example.tabletoptoolbox.data.boardGames.SortState
import kotlinx.coroutines.flow.*

class TableTierViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _sortState = MutableStateFlow(SortState())
    val sortState: StateFlow<SortState> = _sortState.asStateFlow()

    val games: StateFlow<List<GameEntity>> = combine(
        repository.getAllGames(),
        _sortState
    ) { games, sort -> sortGames(games, sort) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedGame = MutableStateFlow<GameEntity?>(null)
    val selectedGame: StateFlow<GameEntity?> = _selectedGame.asStateFlow()

    fun onGameClicked(game: GameEntity) {
        _selectedGame.value = game
    }

    fun onDialogDismissed() {
        _selectedGame.value = null
    }

    fun onColumnClicked(column: SortColumn) {
        _sortState.update { current ->
            if (current.column == column) {
                current.copy(
                    direction = if (current.direction == SortDirection.ASC)
                        SortDirection.DESC else SortDirection.ASC
                )
            } else {
                val defaultDirection = if (column == SortColumn.NAME)
                    SortDirection.ASC else SortDirection.DESC
                SortState(column = column, direction = defaultDirection)
            }
        }
    }

    private fun sortGames(games: List<GameEntity>, sort: SortState): List<GameEntity> {
        val comparator: Comparator<GameEntity> = when (sort.column) {
            SortColumn.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortColumn.RATING -> compareBy { it.userRating }
            SortColumn.ELO -> compareBy { it.eloRating }
            SortColumn.TIME -> compareBy { it.playTimeMinutes }
        }
        val ordered = games.sortedWith(comparator)
        return if (sort.direction == SortDirection.DESC) ordered.asReversed() else ordered
    }
}