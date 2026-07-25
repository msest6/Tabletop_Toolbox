package com.example.tabletoptoolbox.tabletier

import com.example.tabletoptoolbox.data.boardGames.EloEngine
import com.example.tabletoptoolbox.data.boardGames.GameEntity
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.random.Random

class SwissTournament(
    games: List<GameEntity>,
    private val startingElo: Double = 1200.0,
    private val random: Random = Random.Default
) {

    private class Standing(
        val id: Long,
        var elo: Double,
        var wins: Int = 0,
        var comparisonCount: Int,
        val opponents: MutableSet<Long> = mutableSetOf(),
        var hadBye: Boolean = false
    )

    private data class Match(val a: Long, val b: Long)

    data class EloUpdate(val id: Long, val elo: Double, val comparisonCount: Int)

    private val gameById: Map<Long, GameEntity> = games.associateBy { it.id }

    val totalRounds: Int =
        if (games.size < 2) 0 else ceil(log2(games.size.toDouble())).toInt()
    private val pairingsPerRound: Int = games.size / 2
    val totalQuestions: Int = pairingsPerRound * totalRounds

    private var round = 0
    private var asked = 0
    private var queue: ArrayDeque<Match> = ArrayDeque()

    val questionsAsked: Int get() = asked
    val isFinished: Boolean get() = queue.isEmpty() && round >= totalRounds

    private val standings: Map<Long, Standing> = games.associate {
        it.id to Standing(id = it.id, elo = startingElo, comparisonCount = 0)
    }
    init {
        if (totalRounds > 0) buildNextRound()
    }

    fun currentPair(): Pair<GameEntity, GameEntity>? {
        val m = queue.firstOrNull() ?: return null
        return gameById.getValue(m.a) to gameById.getValue(m.b)
    }

    /** Bilježi ishod SAMO u memoriju (standings). Ništa se ne piše u bazu ovdje. */
    fun submitResult(winnerId: Long): Boolean {
        val m = queue.firstOrNull() ?: return false
        if (winnerId != m.a && winnerId != m.b) return false
        queue.removeFirst()

        val loserId = if (winnerId == m.a) m.b else m.a
        val w = standings.getValue(winnerId)
        val l = standings.getValue(loserId)

        val (newW, newL) = EloEngine.calculateNewElo(w.elo, l.elo)
        w.elo = newW
        l.elo = newL
        w.wins += 1
        w.comparisonCount += 1
        l.comparisonCount += 1
        w.opponents += loserId
        l.opponents += winnerId
        asked += 1

        if (queue.isEmpty() && round < totalRounds) buildNextRound()
        return true
    }

    private fun buildNextRound() {
        if (round >= totalRounds) return
        round += 1

        val sorted = standings.values
            .shuffled(random) // nasumično lomljenje izjednačenih
            .sortedWith(
                compareByDescending<Standing> { it.wins }.thenByDescending { it.elo }
            )
            .toMutableList()

        val used = mutableSetOf<Long>()
        // bye za neparan broj: zadnji koji još nije imao bye
        if (sorted.size % 2 == 1) {
            val bye = sorted.lastOrNull { !it.hadBye } ?: sorted.last()
            bye.hadBye = true
            bye.wins += 1 // besplatna pobjeda, bez Elo promjene
            used += bye.id
        }

        val pool = sorted.filter { it.id !in used }.toMutableList()
        val matches = mutableListOf<Match>()
        while (pool.size >= 2) {
            val a = pool.removeAt(0)
            // prvi sljedeći s kojim još nije igrao; ako su svi odigrani, dopusti revanš
            var idx = pool.indexOfFirst { it.id !in a.opponents }
            if (idx == -1) idx = 0
            val b = pool.removeAt(idx)
            matches += Match(a.id, b.id)
        }

        matches.shuffle(random) // izmiješaj redoslijed prikaza unutar runde
        queue = ArrayDeque(matches)
    }
    /** Buchholz = zbroj pobjeda svih protivnika s kojima je igra odigrala. Veći = jači protivnici. */
    private fun buchholzOf(s: Standing): Int =
        s.opponents.sumOf { standings.getValue(it).wins }

    /**
     * Konačni Elo za SVE igre - poziva se tek kad je kviz gotov, tek se tada piše u bazu.
     * Primarno: broj pobjeda (grupe razmaknute za ELO_PER_WIN).
     * Tiebreak: unutar iste grupe razmicanje po Buchholzu, ograničeno tako da se
     * igra NIKAD ne prelije u susjednu grupu (pomak < pola koraka).
     */
    fun results(): List<EloUpdate> {
        val all = standings.values.toList()
        if (all.isEmpty()) return emptyList()

        val meanBuchholzByWins = all
            .groupBy { it.wins }
            .mapValues { (_, grupa) -> grupa.map { buchholzOf(it) }.average() }

        return all.map { s ->
            val groupMean = meanBuchholzByWins.getValue(s.wins)
            val shift = (BUCHHOLZ_WEIGHT * (buchholzOf(s) - groupMean))
                .coerceIn(-BUCHHOLZ_MAX_SHIFT, BUCHHOLZ_MAX_SHIFT)

            val finalElo = startingElo + ELO_PER_WIN * (s.wins - totalRounds / 2.0) + shift
            EloUpdate(s.id, finalElo, s.comparisonCount)
        }
    }

    private companion object {
        /** Korak Ela po pobjedi (= K iz EloEngine). Win-grupe su za toliko razmaknute. */
        const val ELO_PER_WIN = 32.0
        /** Max pomak od Buchholza. MORA biti < ELO_PER_WIN / 2 da se grupe ne preklope. */
        const val BUCHHOLZ_MAX_SHIFT = 15.0
        /** Ela po jedinici razlike Buchholza naspram prosjeka grupe. Podesivo. */
        const val BUCHHOLZ_WEIGHT = 2.0
    }
}