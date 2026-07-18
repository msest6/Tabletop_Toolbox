package com.example.tabletoptoolbox.data.boardGames

import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

object EloEngine {

    /**
     * Broj usporedbi potrebnih da se jedna igra binarnom pretragom smjesti
     * unutar liste od poolSize ostalih igara. Deterministički broj, ne ovisi
     * o ishodima usporedbi - zato ga možemo unaprijed znati za progress bar.
     */
    fun questionsNeededForPool(poolSize: Int): Int {
        if (poolSize <= 0) return 0
        return ceil(log2((poolSize + 1).toDouble())).toInt()
    }

    fun calculateNewElo(
        winnerElo: Double,
        loserElo: Double,
        kFactor: Double = 32.0
    ): Pair<Double, Double> {
        val expectedWinner = 1.0 / (1.0 + 10.0.pow((loserElo - winnerElo) / 400.0))
        val expectedLoser = 1.0 - expectedWinner

        val newWinnerElo = winnerElo + kFactor * (1 - expectedWinner)
        val newLoserElo = loserElo + kFactor * (0 - expectedLoser)

        return newWinnerElo to newLoserElo
    }
}