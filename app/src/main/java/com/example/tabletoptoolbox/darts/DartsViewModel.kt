package com.example.tabletoptoolbox.darts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.DartsRepository
import com.example.tabletoptoolbox.data.model.CheckoutTable
import com.example.tabletoptoolbox.data.model.DartThrow
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlinx.coroutines.launch

private val DARTBOARD_NUMBERS = listOf(
    20, 1, 18, 4, 13, 6, 10, 15, 2, 17,
    3, 19, 7, 16, 8, 11, 14, 9, 12, 5
)

private data class PolarCoord(val radius: Float, val angle: Float)

/**
 * Drži cjelokupni state i logiku za pikado - dijele ga DartsNewGame, Darts (glavna igra,
 * korištena na dvije rute) i DartsMovesList, pa se instancira jednom na razini MainActivity.
 */
class DartsViewModel(
    private val repository: DartsRepository,
    private val checkoutTable: List<CheckoutTable>
) : ViewModel() {

    var playerNames by mutableStateOf<List<String>>(emptyList())
        private set

    var targetScore by mutableStateOf("501")
        private set

    var doubleIn by mutableStateOf(false)
        private set

    var doubleOut by mutableStateOf(true)
        private set

    var currentPlayerIndex by mutableStateOf(0)
        private set

    var isLoaded by mutableStateOf(false)
        private set

    /** Povijest svih odigranih poteza - perzistira se. */
    val moves = mutableStateListOf<DartThrow>()

    /** Trenutni rezultat po igraču, izveden iz targetScore i moves. */
    val scores = mutableStateListOf<Int>()

    /** Imena pobjednika ove igre (može ih biti više ako više igrača istovremeno dođe do 0). */
    val winners = mutableStateListOf<String>()

    private var dart1 by mutableStateOf(0)
    private var dart2 by mutableStateOf(0)
    private var dart3 by mutableStateOf(0)

    var dart1Text by mutableStateOf("")
        private set
    var dart2Text by mutableStateOf("")
        private set
    var dart3Text by mutableStateOf("")
        private set

    /** Koliko strelica igrač još treba baciti ovaj potez (3, 2, 1 ili 0). */
    var dartsThrown by mutableStateOf(3)
        private set

    private var loadStarted = false

    fun loadIfNeeded() {
        if (loadStarted) return
        loadStarted = true
        viewModelScope.launch {
            playerNames = repository.getPlayerNameList()
            targetScore = repository.getActiveGame().ifBlank { "501" }
            doubleIn = repository.getDoubleIn()
            doubleOut = repository.getDoubleOut()
            currentPlayerIndex = repository.getCurrentPlayer()
            moves.clear()
            moves.addAll(repository.loadGame())
            isLoaded = true
        }
    }

    fun updateDoubleIn(value: Boolean) {
        doubleIn = value
        viewModelScope.launch { repository.setDoubleIn(value) }
    }

    fun updateDoubleOut(value: Boolean) {
        doubleOut = value
        viewModelScope.launch { repository.setDoubleOut(value) }
    }

    /** Pokreće posve novu igru (poziva ga DartsNewGame ekran nakon unosa igrača). */
    fun startNewGame(names: List<String>, target: String, doubleIn: Boolean, doubleOut: Boolean) {
        playerNames = names
        targetScore = target
        this.doubleIn = doubleIn
        this.doubleOut = doubleOut
        updateCurrentPlayer(0)
        viewModelScope.launch {
            repository.savePlayerNameList(names)
            repository.setActiveGame(target)
            repository.setDoubleIn(doubleIn)
            repository.setDoubleOut(doubleOut)
        }
    }

    /**
     * Priprema rundu za prikaz - poziva se svaki put kad se uđe na "darts" ili "dartsNew" rutu.
     * @param resumeExisting true za nastavak spremljene igre, false za potpuno svjež početak.
     */
    fun prepareRound(resumeExisting: Boolean) {
        if (resumeExisting) {
            resetScoresFromMoves()
        } else {
            moves.clear()
            viewModelScope.launch { repository.saveGame(moves) }
            resetScoresToTarget()
            winners.clear()
        }
        dart1 = 0; dart2 = 0; dart3 = 0
        dart1Text = ""; dart2Text = ""; dart3Text = ""
        dartsThrown = 3
        updateCheckoutHints()
        checkForWinners()
    }

    /** Registrira dodir na ploči - izračuna segment/multiplikator i sprema kao sljedeću strelicu. */
    fun registerDartTap(
        tapOffset: Offset,
        boardCenter: IntOffset,
        boardRadiusPx: Float,
        doubleOuterPx: Float,
        doubleInnerPx: Float,
        tripleOuterPx: Float,
        tripleInnerPx: Float,
        outerBullPx: Float,
        innerBullPx: Float
    ) {
        val polar = calculatePolarCoordinates(tapOffset, boardCenter)
        if (polar.radius > boardRadiusPx) return

        val segment = getSegment(polar.angle)
        val multiplier = when {
            polar.radius <= innerBullPx -> 50
            polar.radius <= outerBullPx -> 25
            polar.radius in tripleInnerPx..tripleOuterPx -> 3
            polar.radius in doubleInnerPx..doubleOuterPx -> 2
            polar.radius > doubleOuterPx -> 0
            else -> 1
        }

        var finalScore = 0
        var addOn = ""
        when (multiplier) {
            50 -> { finalScore = 50; addOn = " (2xBull)" }
            25 -> { finalScore = 25; addOn = " (Bull)" }
            0 -> { finalScore = 0; addOn = " (Promašaj)" }
            2 -> { finalScore = segment * multiplier; addOn = " (2x$segment)" }
            3 -> { finalScore = segment * multiplier; addOn = " (3x$segment)" }
            1 -> { finalScore = segment; addOn = "" }
        }

        when (dartsThrown) {
            3 -> { dart1 = finalScore; dart1Text = "$dart1$addOn"; dartsThrown -= 1 }
            2 -> { dart2 = finalScore; dart2Text = "$dart2$addOn"; dartsThrown -= 1 }
            1 -> { dart3 = finalScore; dart3Text = "$dart3$addOn"; dartsThrown -= 1 }
        }
        updateCheckoutHints()
    }

    /** Poništava pending strelicu na danoj poziciji (1, 2 ili 3) - klik na ikonu strelice. */
    fun undoDart(position: Int) {
        when (position) {
            1 -> if (dart1Text.isNotEmpty() && dartsThrown == 2) {
                dart1 = 0; dart1Text = ""; dartsThrown += 1
            }
            2 -> if (dart2Text.isNotEmpty() && dartsThrown == 1) {
                dart2 = 0; dart2Text = ""; dartsThrown += 1
            }
            3 -> if (dart3Text.isNotEmpty() && dartsThrown == 0) {
                dart3 = 0; dart3Text = ""; dartsThrown += 1
            }
        }
        updateCheckoutHints()
    }

    /** "Spremi" gumb tijekom igre - primjenjuje double-in/double-out pravila i prelazi na sljedećeg igrača. */
    fun submitThrow() {
        val target = targetScore.toInt()
        if (scores[currentPlayerIndex] == target && doubleIn) {
            if ("2x" in dart1Text) {
                scores[currentPlayerIndex] -= dart1 + dart2 + dart3
            } else if ("2x" in dart2Text) {
                dart1 = 0
                scores[currentPlayerIndex] -= dart2 + dart3
            } else if ("2x" in dart3Text) {
                dart2 = 0; dart1 = 0
                scores[currentPlayerIndex] -= dart3
            } else {
                dart3 = 0; dart2 = 0; dart1 = 0
            }
        } else {
            val sum = scores[currentPlayerIndex] - dart1 - dart2 - dart3
            if (sum > 0) {
                if (doubleOut) {
                    if (sum != 1) scores[currentPlayerIndex] = sum
                } else {
                    scores[currentPlayerIndex] = sum
                }
            } else if (sum == 0) {
                if (doubleOut) {
                    if ("2x" in dart3Text) {
                        scores[currentPlayerIndex] = 0
                    } else if (dart3 == 0) {
                        if ("2x" in dart2Text) {
                            scores[currentPlayerIndex] = 0
                        } else if (dart2 == 0) {
                            if ("2x" in dart1Text) {
                                scores[currentPlayerIndex] = 0
                            } else {
                                dart1 = 0
                            }
                        } else {
                            dart2 = 0; dart1 = 0
                        }
                    } else {
                        dart3 = 0; dart2 = 0; dart1 = 0
                    }
                } else {
                    scores[currentPlayerIndex] = 0
                }
            }
        }

        addMoveInternal(currentPlayerIndex, dart1, dart2, dart3)
        updateCurrentPlayer(currentPlayerIndex + 1)
        dart1 = 0; dart2 = 0; dart3 = 0
        dart1Text = ""; dart2Text = ""; dart3Text = ""
        dartsThrown = 3
        checkForWinners()
    }

    /** Poziva ga dijalog pobjednika prije prelaska na novu igru. */
    fun restartAfterWin() {
        moves.clear()
        viewModelScope.launch { repository.saveGame(moves) }
    }

    /** Poziva ga DartsMovesList ekran nakon uređivanja/brisanja poteza. */
    fun commitMovesEdit(newMoves: List<DartThrow>, newCurrentPlayer: Int) {
        moves.clear()
        moves.addAll(newMoves)
        viewModelScope.launch { repository.saveGame(moves) }
        updateCurrentPlayer(newCurrentPlayer)
    }

    private fun updateCurrentPlayer(index: Int) {
        if (playerNames.isEmpty()) {
            currentPlayerIndex = 0
            return
        }
        currentPlayerIndex = ((index % playerNames.size) + playerNames.size) % playerNames.size
        viewModelScope.launch { repository.setCurrentPlayer(currentPlayerIndex) }
    }

    private fun resetScoresToTarget() {
        val target = targetScore.toIntOrNull() ?: 0
        scores.clear()
        scores.addAll(List(playerNames.size) { target })
    }

    private fun resetScoresFromMoves() {
        resetScoresToTarget()
        for (move in moves) {
            for (dart in move.throws) {
                if (move.playerIndex in scores.indices) {
                    scores[move.playerIndex] -= dart
                }
            }
        }
    }

    /** Provjerava je li runda upravo završila (potez se vratio na igrača 0) i tko je pritom stigao do 0. */
    private fun checkForWinners() {
        if (currentPlayerIndex != 0 || winners.isNotEmpty()) return
        val zeroScorers = scores.indices.filter { scores[it] == 0 }.map { playerNames[it] }
        if (zeroScorers.isNotEmpty()) {
            winners.clear()
            winners.addAll(zeroScorers)
        }
    }

    private fun updateCheckoutHints() {
        if (scores.isEmpty() || currentPlayerIndex !in scores.indices) return
        val sum = scores[currentPlayerIndex] - dart1 - dart2 - dart3
        for (item in checkoutTable) {
            if (item.score == sum) {
                if (dart1 == 0) {
                    dart1Text = item.hand[0]
                    dart2Text = item.hand[1]
                    dart3Text = item.hand[2]
                } else if (dart2 == 0) {
                    dart2Text = item.hand[0]
                    dart3Text = item.hand[1]
                } else if (dart3 == 0) {
                    dart3Text = item.hand[0]
                }
            }
        }
    }

    private fun addMoveInternal(playerIndex: Int, d1: Int, d2: Int, d3: Int) {
        moves.add(DartThrow(playerIndex, listOf(d1, d2, d3)))
        viewModelScope.launch { repository.saveGame(moves) }
    }

    private fun calculatePolarCoordinates(tap: Offset, center: IntOffset): PolarCoord {
        val dx = tap.x - center.x
        val dy = center.y - tap.y
        val radius = sqrt(dx * dx + dy * dy)
        var angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
        if (angle < 0) angle += 360f
        return PolarCoord(radius, angle)
    }

    private fun getSegment(angle: Float): Int {
        val segmentAngle = 360f / 20f
        val adjustedAngle = (-(angle) + 90f + segmentAngle / 2f + 360f) % 360f
        val index = (adjustedAngle / segmentAngle).toInt().coerceIn(0, 19)
        return DARTBOARD_NUMBERS[index]
    }
}