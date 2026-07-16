package com.example.tabletoptoolbox.uno

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.UnoRepository
import com.example.tabletoptoolbox.data.model.UnoModel
import com.example.tabletoptoolbox.data.model.UnoRound
import kotlinx.coroutines.launch

class UnoViewModel(private val repository: UnoRepository) : ViewModel() {
    val players = mutableStateListOf<UnoModel>()

    /** Sve odigrane runde trenutne igre - "izvor istine" iz kojeg se računaju bodovi igrača. */
    val rounds = mutableStateListOf<UnoRound>()

    var looseScore by mutableIntStateOf(1000)
        private set

    private var dealerIndex by mutableIntStateOf(0)

    /** Ime trenutnog djelitelja - prikazuje se u navbaru. */
    val dealerName: String
        get() = players.getOrNull(dealerIndex)?.playerName ?: ""

    /** Imena igrača koji su u trenutnom krugu dosegli ciljani broj bodova. */
    var loosers by mutableStateOf<List<String>>(emptyList())
        private set

    /** Postaje true tek kad su podaci stvarno učitani iz repozitorija - koristi ga UI da izbjegne treptaj default vrijednosti. */
    var isLoaded by mutableStateOf(false)
        private set

    private var loadStarted = false

    fun loadIfNeeded() {
        if (loadStarted) return
        loadStarted = true
        viewModelScope.launch {
            looseScore = repository.getLooseScore()
            dealerIndex = repository.getDjelitelj()
            val loadedPlayers = repository.loadPlayers()
            players.clear()
            players.addAll(loadedPlayers)
            val loadedRounds = repository.loadGame()
            rounds.clear()
            rounds.addAll(loadedRounds)
            isLoaded = true
        }
    }

    private fun persistPlayers() {
        viewModelScope.launch { repository.savePlayers(players) }
    }

    private fun persistRounds() {
        viewModelScope.launch { repository.saveGame(rounds) }
    }

    fun advanceDealer() {
        if (players.isEmpty()) return
        dealerIndex = (dealerIndex + 1) % players.size
        viewModelScope.launch { repository.setDjelitelj(dealerIndex) }
    }

    fun startNewGame(newPlayerNames: List<String>, newLooseScore: Int) {
        // Sprječava da loadIfNeeded() naknadno prepiše ovo stanje stare (async) vrijednosti
        loadStarted = true
        isLoaded = true

        players.clear()
        for (name in newPlayerNames) {
            if (name.isBlank()) continue
            players.add(UnoModel(name, 0, 0))
        }
        looseScore = newLooseScore
        dealerIndex = 0
        rounds.clear()
        loosers = emptyList()

        viewModelScope.launch {
            repository.setLooseScore(newLooseScore)
            repository.setDjelitelj(0)
            repository.savePlayers(players)
            repository.saveGame(rounds)
        }
    }

    /**
     * Preračunava bodove svakog igrača kao zbroj njegovih bodova iz svih (ne izbrisanih) rundi.
     * Ako runda ne postoji za nekog igrača (ili ih uopće nema), njegov je zbroj 0.
     * Koristi se i pri dodavanju nove partije i pri uređivanju/brisanju rundi na ekranu pregleda.
     */
    private fun recalculateFromRounds() {
        val totals = IntArray(players.size)
        for (round in rounds) {
            for (i in players.indices) {
                totals[i] += round.scores.getOrElse(i) { 0 }
            }
        }

        val newLoosers = mutableListOf<String>()
        for (i in players.indices) {
            players[i] = players[i].copy(score = totals[i])
            if (looseScore != 0 && totals[i] >= looseScore) {
                newLoosers.add(players[i].playerName)
            }
        }
        persistPlayers()
        persistRounds()
        if (newLoosers.isNotEmpty()) {
            loosers = newLoosers
        }
    }

    /** Dodaje novu partiju (rundu) i preračunava bodove svih igrača iz cijele povijesti rundi. */
    fun addRoundScores(roundScores: List<Int>) {
        rounds.add(UnoRound(scores = roundScores))
        recalculateFromRounds()
    }

    /**
     * Poziva se s ekrana za pregled rundi kad korisnik klikne "Spremi" - zamjenjuje cijelu
     * povijest rundi uređenom (draft) verzijom i ponovno računa bodove igrača iz nje.
     */
    fun commitRoundsEdit(newRounds: List<UnoRound>) {
        rounds.clear()
        rounds.addAll(newRounds)
        recalculateFromRounds()
    }

    /** Poziva se kad korisnik potvrdi popup gubitnika - dodaje gubitnički bod i resetira bodove/runde za sljedeći krug. */
    fun acknowledgeLoosers() {
        for (name in loosers) {
            val idx = players.indexOfFirst { it.playerName == name }
            if (idx != -1) {
                players[idx] = players[idx].copy(looseNumber = players[idx].looseNumber + 1)
            }
        }
        for (i in players.indices) {
            players[i] = players[i].copy(score = 0)
        }
        rounds.clear()
        persistPlayers()
        persistRounds()
        loosers = emptyList()
    }

    fun resetAll() {
        for (i in players.indices) {
            players[i] = players[i].copy(score = 0, looseNumber = 0)
        }
        rounds.clear()
        persistPlayers()
        persistRounds()
    }
}