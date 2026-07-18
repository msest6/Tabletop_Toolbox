package com.example.tabletoptoolbox.tabletier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.boardGames.EloEngine
import com.example.tabletoptoolbox.data.boardGames.GameEntity
import com.example.tabletoptoolbox.data.boardGames.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EloQuizUiState(
    val isLoading: Boolean = true,
    val currentQuestionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val leftGame: GameEntity? = null,
    val rightGame: GameEntity? = null,
    val isFinished: Boolean = false,
    val notEnoughGames: Boolean = false
)

/**
 * targetGameId == null -> puna re-evaluacija cijele baze (sporiji, temeljitiji proces).
 * targetGameId != null -> brzi insert: samo ta jedna igra se umeće među ostale
 *                         binarnom pretragom, ostale se ne diraju.
 */
class EloQuizViewModel(
    private val repository: GameRepository,
    private val targetGameId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EloQuizUiState())
    val uiState: StateFlow<EloQuizUiState> = _uiState.asStateFlow()

    private var batch: List<GameEntity> = emptyList()
    private var batchIndex = 0

    private var candidates: List<GameEntity> = emptyList()
    private var low = 0
    private var high = 0
    private var focusGame: GameEntity? = null

    private var totalQuestions = 0
    private var questionsAskedSoFar = 0

    init {
        viewModelScope.launch {
            val allGames = repository.getAllGames().first()
            startSession(allGames)
        }
    }

    private fun startSession(allGames: List<GameEntity>) {
        if (targetGameId != null) {
            startQuickInsertSession(allGames)
        } else {
            startFullReEvaluationSession(allGames)
        }
    }

    /** Brzi mod: fokus je isključivo novo dodana igra, umeće se među sve ostale. */
    private fun startQuickInsertSession(allGames: List<GameEntity>) {
        val newGame = allGames.find { it.id == targetGameId }
        val others = allGames.filter { it.id != targetGameId }

        if (newGame == null || others.isEmpty()) {
            // nema s čime usporediti (npr. ovo je prva igra u bazi) - odmah gotovo, ostaje default elo
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
            return
        }

        batch = listOf(newGame)
        batchIndex = 0

        totalQuestions = EloEngine.questionsNeededForPool(others.size)
        questionsAskedSoFar = 0

        setupNextFocusGame(allGames)
    }

    /** Puni mod: sve igre iz baze prolaze kroz binarno umetanje, počevši od najmanje uspoređivanih. */
    private fun startFullReEvaluationSession(allGames: List<GameEntity>) {
        if (allGames.size < 2) {
            _uiState.update { it.copy(isLoading = false, notEnoughGames = true) }
            return
        }

        batch = allGames.sortedBy { it.comparisonCount }
        batchIndex = 0

        val poolSize = allGames.size - 1
        totalQuestions = EloEngine.questionsNeededForPool(poolSize) * batch.size
        questionsAskedSoFar = 0

        setupNextFocusGame(allGames)
    }

    private fun setupNextFocusGame(allGamesSnapshot: List<GameEntity>) {
        if (batchIndex >= batch.size) {
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
            return
        }

        val focus = batch[batchIndex]
        focusGame = focus
        candidates = allGamesSnapshot
            .filter { it.id != focus.id }
            .sortedByDescending { it.eloRating }
        low = 0
        high = candidates.lastIndex

        presentCurrentQuestion()
    }

    private fun presentCurrentQuestion() {
        val focus = focusGame

        if (focus == null || low > high || candidates.isEmpty()) {
            batchIndex++
            viewModelScope.launch {
                val freshSnapshot = repository.getAllGames().first()
                setupNextFocusGame(freshSnapshot)
            }
            return
        }

        val mid = (low + high) / 2
        val opponent = candidates[mid]

        _uiState.update {
            it.copy(
                isLoading = false,
                currentQuestionNumber = questionsAskedSoFar + 1,
                totalQuestions = totalQuestions,
                leftGame = focus,
                rightGame = opponent,
                isFinished = false
            )
        }
    }

    fun onGameChosen(chosen: GameEntity) {
        val focus = focusGame ?: return
        val mid = (low + high) / 2
        val opponent = candidates[mid]

        val winner = chosen
        val loser = if (chosen.id == focus.id) opponent else focus

        viewModelScope.launch {
            val (newWinnerElo, newLoserElo) = EloEngine.calculateNewElo(
                winnerElo = winner.eloRating,
                loserElo = loser.eloRating
            )

            val updatedWinner = winner.copy(
                eloRating = newWinnerElo,
                comparisonCount = winner.comparisonCount + 1
            )
            val updatedLoser = loser.copy(
                eloRating = newLoserElo,
                comparisonCount = loser.comparisonCount + 1
            )

            repository.updateGame(updatedWinner)
            repository.updateGame(updatedLoser)

            focusGame = if (updatedWinner.id == focus.id) updatedWinner else updatedLoser

            if (chosen.id == focus.id) {
                high = mid - 1
            } else {
                low = mid + 1
            }

            questionsAskedSoFar++
            presentCurrentQuestion()
        }
    }
}