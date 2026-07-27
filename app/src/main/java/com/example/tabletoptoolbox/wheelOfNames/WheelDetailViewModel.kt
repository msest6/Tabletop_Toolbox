package com.example.tabletoptoolbox.wheelOfNames

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tabletoptoolbox.data.model.Wheel
import java.util.UUID
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import kotlin.random.Random

class WheelDetailViewModel : ViewModel() {
    var name by mutableStateOf("")
    var entriesText by mutableStateOf("")     // sirovi textbox, jedan entry po retku
    var highlightIndex by mutableStateOf<Int?>(null)
    var nameError by mutableStateOf<String?>(null)
        private set
    var entriesError by mutableStateOf<String?>(null)
        private set
    var isSpinning by mutableStateOf(false)
        private set
    var winner by mutableStateOf<String?>(null)      // != null → prikaži pop-up
        private set

    private val rotation = Animatable(0f)
    val rotationDeg: Float get() = rotation.value

    private var id: String? = null            // null = novo kolo koje još nije spremljeno

    val currentId: String? get() = id
    fun load(wheel: Wheel?) {
        id = wheel?.id                        // ostaje null za wheelNew
        name = wheel?.name ?: ""
        entriesText = wheel?.entries?.joinToString("\n") ?: ""
        highlightIndex = null
    }

    val entries: List<String>
        get() = entriesText.lines().map { it.trim() }.filter { it.isNotBlank() }

    /**
     * Vrati Wheel spreman za spremanje. Ako je kolo novo (id == null),
     * generira novi id, upamti ga u VM-u i ugradi u vraćeni Wheel -
     * tako ponovni Spremi radi update umjesto novog inserta.
     */
    fun toWheel(): Wheel {
        val resolvedId = id ?: UUID.randomUUID().toString()
        id = resolvedId                       // ključno: upamti da više nismo "novi"
        return Wheel(id = resolvedId, name = name, entries = entries)
    }

    var sortAscending by mutableStateOf(true)
        private set

    fun shuffle() {
        entriesText = entries.shuffled().joinToString("\n")
    }

    fun sort() {
        val sorted = entries.sortedWith(String.CASE_INSENSITIVE_ORDER)
        entriesText = (if (sortAscending) sorted else sorted.asReversed()).joinToString("\n")
        sortAscending = !sortAscending
    }

    fun validate(): Boolean {
        nameError = if (name.isBlank()) "Naslov ne smije biti prazan" else null
        entriesError = if (entries.isEmpty()) "Stavke ne smiju biti prazne" else null
        return nameError == null && entriesError == null
    }

    fun clearNameError() { nameError = null }
    fun clearEntriesError() { entriesError = null }
    fun clear() {
        id = null
        name = ""
        entriesText = ""
        highlightIndex = null
        nameError = null
        entriesError = null
        sortAscending = true
    }

    suspend fun spin() {
        val list = entries
        if (isSpinning || list.size < 2) return
        isSpinning = true
        highlightIndex = null
        winner = null

        val sliceAngle = 360f / list.size
        val winnerIdx = Random.nextInt(list.size)

        // pokazivač je na vrhu (270° u canvas konvenciji gdje 0°=istok, CW pozitivan)
        val winnerCenter = winnerIdx * sliceAngle + sliceAngle / 2f
        val jitter = (Random.nextFloat() - 0.5f) * sliceAngle * 0.7f   // da ne stane baš u sredinu
        val current = rotation.value

        // treba: (winnerCenter - 90 + target) ≡ 270  (mod 360)
        val targetMod = ((270f - (winnerCenter - 90f) - jitter) % 360f + 360f) % 360f
        val currentMod = ((current % 360f) + 360f) % 360f
        val delta = ((targetMod - currentMod) % 360f + 360f) % 360f
        val target = current + delta + 360f * 5      // +5 punih okretaja

        rotation.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 4500, easing = FastOutSlowInEasing)
        )

        highlightIndex = winnerIdx
        winner = list[winnerIdx]
        isSpinning = false
    }
    fun dismissWinner() { winner = null }

    /** Ukloni pobjednika iz textboxa i zatvori pop-up. */
    fun removeWinner() {
        winner?.let { w ->
            val remaining = entries.toMutableList().apply { remove(w) }  // makne prvu pojavu
            entriesText = remaining.joinToString("\n")
        }
        winner = null
        highlightIndex = null
    }
}