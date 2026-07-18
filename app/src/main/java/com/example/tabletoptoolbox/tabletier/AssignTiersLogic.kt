package com.example.tabletoptoolbox.tabletier

import com.example.tabletoptoolbox.data.boardGames.GameEntity
import kotlin.math.roundToInt

enum class Tier(val label: String) {
    S("S"), A("A"), B("B"), C("C"), D("D"), F("F")
}

private val TIER_PERCENTAGES = listOf(
    Tier.S to 0.08,
    Tier.A to 0.12,
    Tier.B to 0.20,
    Tier.C to 0.20,
    Tier.D to 0.20,
    Tier.F to 0.20
)

/**
 * Očekuje listu igara već sortiranu silazno po eloRatingu.
 * Koristi kumulativno zaokruživanje kako bi zbroj svih tierova
 * uvijek bio jednak ukupnom broju igara, bez obzira na postotke.
 */
fun assignTiers(gamesSortedByEloDesc: List<GameEntity>): Map<Tier, List<GameEntity>> {
    if (gamesSortedByEloDesc.isEmpty()) {
        return Tier.entries.associateWith { emptyList() }
    }

    val total = gamesSortedByEloDesc.size
    val counts = mutableListOf<Int>()
    var assigned = 0
    var cumulativeIdeal = 0.0

    for ((_, pct) in TIER_PERCENTAGES) {
        cumulativeIdeal += total * pct
        val cumulativeCount = cumulativeIdeal.roundToInt().coerceAtMost(total)
        val count = (cumulativeCount - assigned).coerceAtLeast(0)
        counts.add(count)
        assigned += count
    }
    // ako je zbog zaokruživanja ostalo neraspoređenih igara, idu u zadnji tier (F)
    if (assigned < total) {
        counts[counts.lastIndex] += (total - assigned)
    }

    val result = linkedMapOf<Tier, List<GameEntity>>()
    var index = 0
    for (i in TIER_PERCENTAGES.indices) {
        val tier = TIER_PERCENTAGES[i].first
        val count = counts[i]
        result[tier] = gamesSortedByEloDesc.subList(index, index + count).toList()
        index += count
    }
    return result
}