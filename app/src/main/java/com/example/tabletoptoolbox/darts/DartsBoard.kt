package com.example.tabletoptoolbox.darts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin

/**
 * Funkcija za crtanje ploče za pikado
 */
@Composable
fun DartsBoard(navController: NavController, buttonColors: ButtonColors, modifier: Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Canvas(modifier = modifier) {
            val center = Offset(size.width / 2, size.height / 2)

            val numbers = listOf(
                20, 1, 18, 4, 13, 6, 10, 15, 2, 17,
                3, 19, 7, 16, 8, 11, 14, 9, 12, 5
            )

            val segmentAngle = 360f / 20f

            val radius = size.minDimension / 2

            val doubleOuter = radius * 0.85f
            val doubleInner = doubleOuter * (0.85f / 0.95f)
            val tripleOuter = doubleOuter * (0.60f / 0.95f)
            val tripleInner = doubleOuter * (0.52f / 0.95f)
            val outerBull = doubleOuter * (0.15f / 0.95f)
            val innerBull = doubleOuter * (0.05f / 0.95f)

            // Crna podloga
            drawCircle(Color.Black, radius, center)

            // PROLAZ 1: Double ring (vanjski sloj)
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val ringColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - doubleOuter, center.y - doubleOuter),
                    size = Size(doubleOuter * 2, doubleOuter * 2)
                )
            }

            // PROLAZ 2: Single vanjski (doubleInner) - prekriva unutrašnjost double
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val mainColor = if (i % 2 == 0) Color(0xFF111111) else Color(0xFFF5F5F5)

                drawArc(
                    color = mainColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - doubleInner, center.y - doubleInner),
                    size = Size(doubleInner * 2, doubleInner * 2)
                )
            }

            // PROLAZ 3: Triple ring
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val ringColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - tripleOuter, center.y - tripleOuter),
                    size = Size(tripleOuter * 2, tripleOuter * 2)
                )
            }

            // PROLAZ 4: Single unutarnji (tripleInner) - prekriva unutrašnjost triple
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val mainColor = if (i % 2 == 0) Color(0xFF111111) else Color(0xFFF5F5F5)

                drawArc(
                    color = mainColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - tripleInner, center.y - tripleInner),
                    size = Size(tripleInner * 2, tripleInner * 2)
                )
            }

            // Metalni razdjelnici
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val angleRad = Math.toRadians(startAngle.toDouble())
                val lineEnd = Offset(
                    center.x + doubleOuter * cos(angleRad).toFloat(),
                    center.y + doubleOuter * sin(angleRad).toFloat()
                )
                drawLine(
                    Color.Gray,
                    center,
                    lineEnd,
                    strokeWidth = 2f
                )
            }

            // Bullseye
            drawCircle(Color(0xFF2E7D32), outerBull, center)
            drawCircle(Color(0xFFD32F2F), innerBull, center)

            // Brojevi
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f

                drawContext.canvas.nativeCanvas.apply {
                    val textRadius = doubleOuter * 1.1f
                    val textAngle = Math.toRadians((startAngle + segmentAngle / 2).toDouble())

                    val x = center.x + textRadius * cos(textAngle).toFloat()
                    val y = center.y + textRadius * sin(textAngle).toFloat()

                    val paint = Paint().apply {
                        color = Color(0xFFFFFFFF).toArgb()
                        textSize = radius * 0.09f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    val textHeight = paint.descent() - paint.ascent()
                    val textOffset = textHeight / 2 - paint.descent()

                    drawText(numbers[i].toString(), x, y + textOffset, paint)
                }
            }
        }
    }
}