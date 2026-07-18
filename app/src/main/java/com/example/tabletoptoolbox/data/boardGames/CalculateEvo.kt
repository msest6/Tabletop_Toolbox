package com.example.tabletoptoolbox.data.games

import kotlin.math.pow

/**
 * Izračunava nove Elo ratinge nakon usporedbe dvije igre.
 * kFactor određuje koliko agresivno se rating mijenja nakon jedne usporedbe -
 * veći broj = veće oscilacije, manji broj = sporija ali stabilnija konvergencija.
 */
fun calculateNewElo(
    winnerElo: Double,
    loserElo: Double,
    kFactor: Double = 32.0
): Pair<Double, Double> {
    val expectedWinner = 1.0 / (1.0 + 10.0.pow((loserElo - winnerElo) / 400.0))
    val expectedLoser = 1.0 - expectedWinner

    val newWinnerElo = winnerElo + kFactor * (1 - expectedWinner)
    val newLoserElo = loserElo + kFactor * (0 - expectedLoser)

    return Pair(newWinnerElo, newLoserElo)
}