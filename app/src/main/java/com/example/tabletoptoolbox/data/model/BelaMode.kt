package com.example.tabletoptoolbox.data.model

/**
 * Opisuje varijantu Bele po broju igrača. Ovo je jedini izvor istine za to koliko
 * je stupaca u rezultatu, koliko polja za ime treba na ekranu za novu igru,
 * podržava li unos partije auto-popunjavanje (zbroj bodova u otvorenoj Beli je uvijek 162),
 * i koji je zadani cilj bodova za tu varijantu.
 */
enum class BelaMode(
    val playerCount: Int,
    val columnCount: Int,
    val label: String,
    val supportsAutoFill: Boolean,
    val defaultTargetScore: Int
) {
    TWO_OPEN(playerCount = 2, columnCount = 2, label = "2 igrača (otvoreno)", supportsAutoFill = true, defaultTargetScore = 501),
    TWO_CLOSED(playerCount = 2, columnCount = 2, label = "2 igrača (zatvoreno)", supportsAutoFill = false, defaultTargetScore = 501),
    THREE(playerCount = 3, columnCount = 3, label = "3 igrača", supportsAutoFill = false, defaultTargetScore = 701),
    FOUR(playerCount = 4, columnCount = 2, label = "4 igrača", supportsAutoFill = true, defaultTargetScore = 1001);

    companion object {
        val DEFAULT = FOUR
        val AVAILABLE_TARGET_SCORES = listOf(501, 701, 1001)
    }
}