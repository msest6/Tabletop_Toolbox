package com.example.tabletoptoolbox.wheelOfNames

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.min

private val wheelPalette = listOf(
    Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0),
    Color(0xFF29B6F6), Color(0xFF26A69A), Color(0xFF9CCC65),
    Color(0xFFFFEE58), Color(0xFFFFA726), Color(0xFF8D6E63),
    Color(0xFF78909C)
)

/** Skrati string s "…" da stane u zadanu širinu (u px). */
private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text
    var end = text.length
    while (end > 0 && paint.measureText(text.substring(0, end) + "…") > maxWidth) end--
    return if (end <= 0) "…" else text.substring(0, end) + "…"
}

/**
 * Crta kolo za zadanu listu entryja.
 * rotationDeg = trenutni kut vrtnje (0 dok stoji; kasnije ga animiramo).
 * highlightIndex = pobjednički isječak nakon vrtnje (null = ništa istaknuto).
 */
@Composable
fun WheelCanvas(
    entries: List<String>,
    modifier: Modifier = Modifier,
    rotationDeg: Float = 0f,
    highlightIndex: Int? = null
) {
    Canvas(modifier = modifier) {
        if (entries.isEmpty()) return@Canvas

        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        val sliceAngle = 360f / entries.size
        val showText = sliceAngle >= 6f          // ispod ~60 entryja tekst je ionako nečitljiv

        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)

        // 1) Obojeni isječci
        for (i in entries.indices) {
            val startAngle = i * sliceAngle - 90f + rotationDeg   // -90f = kreni od vrha
            val base = wheelPalette[i % wheelPalette.size]
            // ako imamo pobjednika, ostale zatamnimo
            val color = if (highlightIndex != null && i != highlightIndex)
                base.copy(alpha = 0.3f) else base

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sliceAngle,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
        }

        // 2) Bijeli obrub oko pobjednika
        if (highlightIndex != null) {
            drawArc(
                color = Color.White,
                startAngle = highlightIndex * sliceAngle - 90f + rotationDeg,
                sweepAngle = sliceAngle,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = radius * 0.025f)
            )
        }

        // 3) Radijalni tekst (od centra prema rubu)
        if (showText) {
            val sliceRad = Math.toRadians(sliceAngle.toDouble()).toFloat()
            val maxTextSize = radius * 0.10f
            val fitTextSize = sliceRad * radius * 0.45f          // stane li u debljinu isječka
            val fontSize = min(maxTextSize, fitTextSize)

            val textEnd = radius * 0.92f                         // gdje tekst završava (blizu ruba)
            val available = textEnd - radius * 0.12f             // koliko dužine ima na raspolaganju

            for (i in entries.indices) {
                val midAngle = i * sliceAngle - 90f + rotationDeg + sliceAngle / 2f
                drawContext.canvas.nativeCanvas.apply {
                    save()
                    rotate(midAngle, center.x, center.y)         // +x sad gleda u sredinu isječka

                    val paint = Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = fontSize
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    val label = ellipsize(entries[i], paint, available)

                    // vertikalno centriranje po baseline-u (kao u DartsBoard)
                    val textHeight = paint.descent() - paint.ascent()
                    val textOffset = textHeight / 2 - paint.descent()

                    drawText(label, center.x + textEnd, center.y + textOffset, paint)
                    restore()
                }
            }
        }

        // 4) Fiksni pokazivač na vrhu (NE rotira jer je izvan rotacije isječaka)
        val p = radius * 0.07f
        val pointer = Path().apply {
            moveTo(center.x - p, center.y - radius)
            lineTo(center.x + p, center.y - radius)
            lineTo(center.x, center.y - radius * 0.86f)
            close()
        }
        drawPath(pointer, Color.Black)
    }
}
