package com.example.tabletoptoolbox.graddrzava

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.GradDrzavaRepository
import kotlinx.coroutines.launch

/** Hrvatska abeceda korištena za igru - javno dostupna radi ponovne upotrebe i testiranja. */
val GRAD_DRZAVA_ALPHABET = listOf(
    "A", "B", "C", "Č", "Ć", "D", "Dž", "Đ", "E", "F", "G", "H", "I", "J", "K",
    "L", "Lj", "M", "N", "Nj", "O", "P", "R", "S", "Š", "T", "U", "V", "Z", "Ž"
)

val GRAD_DRZAVA_POSSIBLE_SCORES = listOf(0, 5, 10, 15)

/**
 * Drži cjelokupni state i logiku za "Grad Država". Dijele je tri ekrana
 * (Menu, NewGame, Screen) pa se instancira jednom na razini MainActivity,
 * a ne po pojedinoj ruti.
 */
class GradDrzavaViewModel(private val repository: GradDrzavaRepository) : ViewModel() {

    var categories by mutableStateOf<List<String>>(emptyList())
        private set

    var currentLetter by mutableStateOf("A")
        private set

    var score by mutableStateOf(0)
        private set

    var isLoaded by mutableStateOf(false)
        private set

    /** Bodovi odabrani ovaj krug, po kategoriji - poništavaju se nakon spremanja ili promjene slova. */
    val roundScores = mutableStateListOf<Int>()

    /** Odgovori koje igrač upisuje ovaj krug - resetiraju se na trenutno slovo. */
    val roundAnswers = mutableStateListOf<String>()

    private var loadStarted = false

    fun loadIfNeeded() {
        if (loadStarted) return
        loadStarted = true
        viewModelScope.launch {
            score = repository.getScore()
            currentLetter = repository.getCurrentLetter()
            categories = repository.getCategoryList()
            resizeRoundState()
            isLoaded = true
        }
    }

    fun isValidLetter(letter: String): Boolean = letter in GRAD_DRZAVA_ALPHABET

    /** Postavlja kategorije igre - poziva ga ekran za novu igru. */
    fun updateCategories(newCategories: List<String>) {
        categories = newCategories
        resizeRoundState()
        viewModelScope.launch { repository.setCategoryList(newCategories) }
    }

    /** Pokušava postaviti novo trenutno slovo iz tekstualnog unosa. Vraća false ako slovo nije valjano. */
    fun trySetCurrentLetter(letter: String): Boolean {
        val upperLetter = letter.uppercase()
        if (!isValidLetter(upperLetter)) return false
        applyNewLetter(upperLetter)
        return true
    }

    fun setCategoryScore(index: Int, points: Int) {
        if (index in roundScores.indices) roundScores[index] = points
    }

    fun updateAnswer(index: Int, text: String) {
        if (index in roundAnswers.indices) roundAnswers[index] = text
    }

    /** "Spremi" - zbraja bodove ovog kruga u ukupni score i resetira krug za sljedeće slovo. */
    fun submitRound() {
        val roundTotal = roundScores.sum()
        applyScore(score + roundTotal)
        resetRoundScores()
        resetRoundAnswersToCurrentLetter()
    }

    /** Čista funkcija - bira nasumično slovo, bez ikakve promjene state-a. Koristi UI da prikaže prijedlog prije potvrde. */
    fun pickRandomLetter(): String = GRAD_DRZAVA_ALPHABET.random()

    /** Potvrđuje odabrano nasumično slovo (nakon pickRandomLetter) i resetira bodove ovog kruga. */
    fun confirmLetter(letter: String) {
        applyNewLetter(letter)
        resetRoundScores()
    }

    /** Puni reset runde - vraća slovo na "A" i briše bodove ovog kruga. Ukupni score ostaje netaknut. */
    fun resetToLetterA() {
        applyNewLetter("A")
        resetRoundScores()
        applyScore(score)
    }

    /** Resetira samo ukupni score na 0. */
    fun resetScore() {
        applyScore(0)
    }

    private fun applyNewLetter(letter: String) {
        currentLetter = letter
        resetRoundAnswersToCurrentLetter()
        viewModelScope.launch { repository.setCurrentLetter(letter) }
    }

    private fun applyScore(newScore: Int) {
        score = newScore
        viewModelScope.launch { repository.setScore(newScore) }
    }

    private fun resetRoundScores() {
        for (i in roundScores.indices) roundScores[i] = 0
    }

    private fun resetRoundAnswersToCurrentLetter() {
        for (i in roundAnswers.indices) roundAnswers[i] = currentLetter
    }

    private fun resizeRoundState() {
        roundScores.clear()
        roundScores.addAll(List(categories.size) { 0 })
        roundAnswers.clear()
        roundAnswers.addAll(List(categories.size) { currentLetter })
    }
}