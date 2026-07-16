package com.example.tabletoptoolbox.bela

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.BelaRepository
import com.example.tabletoptoolbox.data.model.BelaMode
import com.example.tabletoptoolbox.data.model.BelaRunda
import kotlinx.coroutines.launch

/**
 * Drži cjelokupni state i logiku za Belu - mod igre (2/3/4 igrača), igrače,
 * djelitelja, povijest partija i ukupne rezultate. Dijele je BelaNewGame, Bela
 * (glavni ekran) i BelaNewRound, pa se instancira jednom na razini MainActivity.
 */
class BelaViewModel(private val repository: BelaRepository) : ViewModel() {

    var mode by mutableStateOf(BelaMode.DEFAULT)
        private set

    var players by mutableStateOf<List<String>>(emptyList())
        private set

    var targetScore by mutableStateOf(1001)
        private set

    var dealerIndex by mutableStateOf(0)
        private set

    var isLoaded by mutableStateOf(false)
        private set

    /** Povijest svih odigranih partija - perzistira se. */
    val rounds = mutableStateListOf<BelaRunda>()

    /** Broj pobjeda po stupcu (timu/igraču) - perzistira se, resetira na 0 pri "Nova igra". */
    val wins = mutableStateListOf<Int>()

    /** Indeks stupca koji je pobijedio (dosegao cilj), ili null dok igra traje. */
    var winnerColumn: Int? by mutableStateOf(null)
        private set

    /** Ukupni rezultat po stupcu (tim ili pojedini igrač, ovisno o modu). */
    val totals: List<Int>
        get() = (0 until mode.columnCount).map { col ->
            rounds.sumOf { round ->
                (round.scores.getOrNull(col) ?: 0) + (round.zvanja.getOrNull(col) ?: 0)
            }
        }

    /** Ime igrača koji trenutno dijeli karte. */
    val dealerName: String get() = players.getOrElse(dealerIndex) { "" }

    private var loadStarted = false

    fun loadIfNeeded() {
        if (loadStarted) return
        loadStarted = true
        viewModelScope.launch {
            mode = repository.getMode()
            players = repository.getPlayerList()
            targetScore = repository.getGameScore()
            dealerIndex = repository.getDjelitelj()
            rounds.clear()
            rounds.addAll(repository.loadGame())
            val loadedWins = repository.getWins()
            wins.clear()
            wins.addAll(if (loadedWins.size == mode.columnCount) loadedWins else List(mode.columnCount) { 0 })
            evaluateWinner()
            isLoaded = true
        }
    }

    /** Naziv stupca za prikaz - "Mi"/"Vi" za 4 igrača (parovi ja+suigrač protiv lijevi+desni), inače ime igrača. */
    fun columnLabel(index: Int): String = when (mode) {
        BelaMode.FOUR -> if (index == 0) "Mi" else "Vi"
        else -> players.getOrElse(index) { "Igrač ${index + 1}" }
    }

    /** Za modove koji podržavaju auto-fill (zbroj bodova u otvorenoj Beli je uvijek 162). */
    fun autoFillPartner(knownValue: Int): Int? =
        if (mode.supportsAutoFill) (162 - knownValue).coerceAtLeast(0) else null

    /** Koji indeksi igrača (u `players`) pripadaju danom stupcu rezultata. */
    private fun playersInColumn(col: Int): List<Int> = when (mode) {
        BelaMode.FOUR -> if (col == 0) listOf(0, 2) else listOf(1, 3)
        else -> listOf(col)
    }

    /** Pokreće posve novu igru - poziva ga BelaNewGame ekran nakon unosa imena. */
    fun startNewGame(newMode: BelaMode, newPlayers: List<String>, newTargetScore: Int) {
        mode = newMode
        players = newPlayers
        targetScore = newTargetScore
        dealerIndex = 0
        rounds.clear()
        winnerColumn = null
        wins.clear()
        wins.addAll(List(newMode.columnCount) { 0 })
        viewModelScope.launch {
            repository.setMode(newMode)
            repository.setPlayerList(newPlayers)
            repository.setGameScore(newTargetScore)
            repository.setDjelitelj(0)
            repository.saveGame(rounds)
            repository.setWins(wins.toList())
        }
    }
    /**
     * Pokreće revanš nakon pobjede - zadržava igrače, mod, cilj i sačuvane pobjede,
     * resetira samo bodove trenutne partije (rounds), a djelitelja nasumično bira
     * između igrača pobjedničkog tima.
     */
    fun rematch() {
        val winner = winnerColumn ?: return
        rounds.clear()
        winnerColumn = null
        val winnerPlayerIndices = playersInColumn(winner).filter { it in players.indices }
        dealerIndex = winnerPlayerIndices.randomOrNull() ?: 0
        viewModelScope.launch {
            repository.saveGame(rounds)
            repository.setDjelitelj(dealerIndex)
        }
    }

    /** Dodaje novu odigranu partiju, sprema je i pomiče djelitelja na sljedećeg igrača. */
    fun addRound(scores: MutableList<Int>, zvanja: MutableList<Int>, zvaoIndex: Int?) {
        if (zvaoIndex != null) {
            val score: Int = scores[zvaoIndex] + zvanja[zvaoIndex] //broj bodova koje je skupio igrač koji zove

            var total = 0 //ukupni broj bodova cijele igre
            for (i in 0 until scores.size) {
                total += scores[i] + zvanja[i]
            }

            if (score < total / 2) {
                val padScore = scores[zvaoIndex]
                val padZvanja = zvanja[zvaoIndex]
                for (i in 0 until scores.size) {
                    if (i == zvaoIndex) {
                        scores[i] = 0
                        zvanja[i] = 0
                    } else if (scores.size == 2) {
                        scores[i] += padScore
                        zvanja[i] += padZvanja
                    }
                }
            }
        }
        for (i in 0 until scores.size){
            if (scores[i] == 0 && zvanja[i] != 0){
                val zvanje = zvanja[i]
                zvanja[i] = 0
                if (scores.size != 3){
                    val indeks = if (i == 0) 1 else 0
                    zvanja[indeks] += zvanje
                }
            }
        }
        rounds.add(BelaRunda(scores, zvanja))
        persistRounds()
        advanceDealer()
        evaluateWinner()
    }

    /** Uklanja zadnju partiju - za ispravak pogreške pri unosu. */
    fun removeLastRound() {
        if (rounds.isEmpty()) return
        rounds.removeAt(rounds.lastIndex)
        persistRounds()
        winnerColumn = null
    }

    /** Ručno pomiče djelitelja - koristi ga strelica za osvježavanje u gornjoj traci. */
    fun advanceDealer() {
        if (players.isEmpty()) return
        dealerIndex = (dealerIndex + 1) % players.size
        viewModelScope.launch { repository.setDjelitelj(dealerIndex) }
    }

    private fun persistRounds() {
        viewModelScope.launch { repository.saveGame(rounds) }
    }

    private fun persistWins() {
        viewModelScope.launch { repository.setWins(wins.toList()) }
    }
    /**
     * Provjerava je li netko dosegao cilj. Kad se pobjeda detektira po prvi put (prijelaz
     * iz null u ne-null), dodaje se bod pobjedniku - 2 boda ako nijedan protivnički stupac
     * nije skupio barem pola ciljnog rezultata, inače 1 bod.
     */
    private fun evaluateWinner() {
        val current = totals
        val maxScore = current.maxOrNull() ?: 0
        val newWinner = if (maxScore >= targetScore) current.indexOf(maxScore) else null

        if (newWinner != null && winnerColumn == null) {
            val halfTarget = targetScore / 2.0
            val opponentsBelowHalf = current.indices
                .filter { it != newWinner }
                .all { current[it] < halfTarget }
            val pointsToAdd = if (opponentsBelowHalf) 2 else 1
            if (newWinner < wins.size) {
                wins[newWinner] = wins[newWinner] + pointsToAdd
                persistWins()
            }
        }
        winnerColumn = newWinner
    }
    /**
     * Proglašava belot za dani stupac - belot je automatska pobjeda igre neovisno o
     * trenutnom rezultatu, pa se rundе trenutne partije ne dodaju/spremaju, samo se
     * odmah dodaje jedan bod pobjedničkom timu i pokreće dijalog pobjednika.
     */
    fun declareBelot(col: Int) {
        if (col !in wins.indices) return
        wins[col] = wins[col] + 1
        persistWins()
        winnerColumn = col
    }
}