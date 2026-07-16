package com.example.tabletoptoolbox.darts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tabletoptoolbox.R

/**
 * Prikazuje grid za igranje pikada. Sav state (rezultati, trenutni potez, pobjednici) i logika
 * (double-in/double-out pravila, geometrija ploče, checkout hintovi) žive u DartsViewModel -
 * ovaj composable samo prikazuje stanje i prosljeđuje dodire.
 * @param loadGame true za nastavak spremljene igre ("darts" ruta), false za svjež početak ("dartsNew" ruta)
 */
@Composable
fun Darts(
    navController: NavController,
    buttonColors: ButtonColors,
    loadGame: Boolean,
    viewModel: DartsViewModel
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(Unit) {
        viewModel.prepareRound(resumeExisting = loadGame)
    }

    if (viewModel.winners.isNotEmpty()) {
        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {
            AnimatedVisibility(
                visible = viewModel.winners.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(2000)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(2000)),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Text(
                            text = if (viewModel.winners.count() > 1)
                                "Pobjednici: ${viewModel.winners.joinToString(",")}"
                            else
                                "Pobjednik: ${viewModel.winners[0]}",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.restartAfterWin()
                                navController.navigate("dartsNew")
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "Restart",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }

    val radius = if (screenWidth < screenHeight) screenWidth / 2 else screenHeight / 2
    val doubleOuter = radius * 0.85f
    val doubleInner = doubleOuter * (0.85f / 0.95f)
    val tripleOuter = doubleOuter * (0.60f / 0.95f)
    val tripleInner = doubleOuter * (0.52f / 0.95f)
    val outerBull = doubleOuter * (0.15f / 0.95f)
    val innerBull = doubleOuter * (0.05f / 0.95f)

    @Composable
    fun ScoreCard(name: String, score: Int, isActive: Boolean) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = name.take(10),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (-10).dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 2.dp)
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.width(screenWidth / 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.strelica_pikado),
                    contentDescription = "Strelica",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp)
                        .alpha(if (viewModel.dartsThrown == 3) 1f else 0.3f)
                        .clickable { viewModel.undoDart(1) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = viewModel.dart1Text,
                    modifier = Modifier.alpha(if (viewModel.dart1Text.isNotEmpty()) 1f else 0.3f)
                )
            }
            Row(
                modifier = Modifier.width(screenWidth / 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.strelica_pikado),
                    contentDescription = "Strelica",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp)
                        .alpha(if (viewModel.dartsThrown >= 2) 1f else 0.3f)
                        .clickable { viewModel.undoDart(2) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = viewModel.dart2Text,
                    modifier = Modifier.alpha(if (viewModel.dart2Text.isNotEmpty()) 1f else 0.3f)
                )
            }
            Row(
                modifier = Modifier.width(screenWidth / 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.strelica_pikado),
                    contentDescription = "Strelica",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp)
                        .alpha(if (viewModel.dartsThrown >= 1) 1f else 0.3f)
                        .clickable { viewModel.undoDart(3) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = viewModel.dart3Text,
                    modifier = Modifier.alpha(if (viewModel.dart3Text.isNotEmpty()) 1f else 0.3f)
                )
            }
        }
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = if (viewModel.doubleIn) "Double-In: Da" else "Double-In: Ne", modifier = Modifier.padding(10.dp))
            Text(text = if (viewModel.doubleOut) "Double-Out: Da" else "Double-Out: Ne", modifier = Modifier.padding(10.dp))
        }
        Column(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("dartsGameDisplay") },
                modifier = Modifier
                    .padding(10.dp)
                    .width(screenWidth / 5)
            ) {
                Icon(
                    imageVector = Icons.Default.DensityMedium,
                    contentDescription = "Lista Poteza",
                    modifier = Modifier.size((screenWidth.value * 0.08f).dp)
                )
            }
            ElevatedButton(
                modifier = Modifier.padding(16.dp),
                colors = buttonColors,
                onClick = { viewModel.submitThrow() }
            ) {
                Text(text = "Spremi", fontSize = (screenWidth.value * 0.05f).sp)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(viewModel.playerNames.count()) { index ->
                if (index < viewModel.playerNames.size) {
                    ScoreCard(
                        name = viewModel.playerNames[index],
                        score = viewModel.scores.getOrNull(index) ?: 0,
                        isActive = index == viewModel.currentPlayerIndex
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2.2f)
                    )
                }
            }
        }
    }
    DartsBoard(navController, buttonColors, modifier = Modifier
        .size(screenWidth)
        .pointerInput(Unit) {
            val density = density
            val doubleOuterPx = with(density) { doubleOuter.toPx() }
            val doubleInnerPx = with(density) { doubleInner.toPx() }
            val tripleOuterPx = with(density) { tripleOuter.toPx() }
            val tripleInnerPx = with(density) { tripleInner.toPx() }
            val outerBullPx = with(density) { outerBull.toPx() }
            val innerBullPx = with(density) { innerBull.toPx() }
            detectTapGestures { tapOffset ->
                viewModel.registerDartTap(
                    tapOffset = tapOffset,
                    boardCenter = size.center,
                    boardRadiusPx = size.width / 2f,
                    doubleOuterPx = doubleOuterPx,
                    doubleInnerPx = doubleInnerPx,
                    tripleOuterPx = tripleOuterPx,
                    tripleInnerPx = tripleInnerPx,
                    outerBullPx = outerBullPx,
                    innerBullPx = innerBullPx
                )
            }
        }
    )
}