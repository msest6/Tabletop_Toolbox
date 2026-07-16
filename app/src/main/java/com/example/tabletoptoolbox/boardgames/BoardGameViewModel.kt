package com.example.tabletoptoolbox.boardgames

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.BoardGameRepository
import com.example.tabletoptoolbox.data.model.BoardGamePlayer
import kotlinx.coroutines.launch

/**
 * Drži cjelokupni state i poslovnu logiku za "Igre na ploči".
 * Composable (BoardGamesScreen) samo prikazuje ovo stanje i poziva metode -
 * ne sadrži nikakvu logiku bodovanja niti izračuna.
 */
class BoardGameViewModel(private val repository: BoardGameRepository) : ViewModel() {

    val players = mutableStateListOf<BoardGamePlayer>()

    var winScore by mutableIntStateOf(0)
        private set

    /** Ime pobjednika, ili prazan string ako trenutno nitko nije pobijedio. */
    var winner by mutableStateOf("")
        private set

    /** Postaje true tek kad su podaci stvarno učitani iz repozitorija - koristi ga UI da izbjegne treptaj default vrijednosti. */
    var isLoaded by mutableStateOf(false)
        private set

    private var loadStarted = false

    fun loadIfNeeded() {
        if (loadStarted) return
        loadStarted = true
        viewModelScope.launch {
            winScore = repository.getWinScore()
            val loadedPlayers = repository.loadPlayers()
            players.clear()
            if (loadedPlayers.isEmpty()) {
                players.add(BoardGamePlayer("", 0, 0))
            } else {
                players.addAll(loadedPlayers)
            }
            isLoaded = true
        }
    }

    /** Čista funkcija - parsira unos za "za pobjedu" polje. Vraća null ako unos nije valjan broj. */
    fun parseWinScoreInput(text: String): Int? = text.toIntOrNull()

    fun updateWinScore(score: Int) {
        winScore = score
        viewModelScope.launch { repository.setWinScore(score) }
    }

    fun updatePlayerName(index: Int, newName: String) {
        if (index !in players.indices) return
        players[index] = players[index].copy(playerName = newName)

        if (index == players.lastIndex && newName.isNotBlank()) {
            players.add(BoardGamePlayer("", 0, 0))
        }
        if (newName.isBlank() && index != players.lastIndex) {
            players.removeAt(index)
        }
        persistPlayers()
    }

    fun incrementScore(index: Int) {
        val player = players.getOrNull(index) ?: return
        if (player.playerName.isBlank()) return
        applyScoreChange(index, player.copy(score = player.score + 1))
    }

    fun decrementScore(index: Int) {
        val player = players.getOrNull(index) ?: return
        if (player.playerName.isBlank()) return
        applyScoreChange(index, player.copy(score = player.score - 1))
    }

    private fun applyScoreChange(index: Int, updated: BoardGamePlayer) {
        val wonNow = winScore != 0 && updated.score >= winScore
        val finalPlayer = if (wonNow) updated.copy(winNumber = updated.winNumber + 1) else updated
        players[index] = finalPlayer
        persistPlayers()
        if (wonNow) {
            winner = finalPlayer.playerName
        }
    }

    /** Poziva se kad korisnik potvrdi dijalog pobjednika - resetira sve rezultate na 0. */
    fun acknowledgeWinner() {
        for (i in players.indices) {
            players[i] = players[i].copy(score = 0)
        }
        persistPlayers()
        winner = ""
    }

    fun rollDice(): Int = (1..6).random()

    fun flipCoin(): String = if ((0..1).random() == 0) "Glava" else "Pismo"

    /** Puni reset - briše sve rezultate, sve pobjede, i vraća "za pobjedu" na 0. */
    fun resetAll() {
        for (i in players.indices) {
            players[i] = players[i].copy(score = 0, winNumber = 0)
        }
        persistPlayers()
        updateWinScore(0)
    }

    private fun persistPlayers() {
        viewModelScope.launch { repository.savePlayers(players) }
    }
}