package com.example.tabletoptoolbox.bela

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

/**
 * Glavni ekran Bele - vrh prikazuje ukupni rezultat, sredina povijest odigranih partija,
 * dno gumb za unos nove partije. Prikazuje dijalog pobjednika kad neki stupac dosegne cilj.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Bela(navController: NavController, buttonColors: ButtonColors, viewModel: BelaViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Djelitelj: ${viewModel.dealerName}", fontSize = (screenWidth.value * 0.06f).sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.advanceDealer() }) {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Sljedeći djelitelj")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Postavke")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BelaScoreboard(viewModel)

            BelaRoundsList(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Button(
                onClick = { navController.navigate("belaNovaRunda") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = buttonColors
            ) {
                Text(text = "Unesi novu partiju", fontSize = (screenWidth.value * 0.05f).sp)
            }
        }
    }

    if (viewModel.winnerColumn != null) {
        Dialog(onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }) {
            AnimatedVisibility(
                visible = viewModel.winnerColumn != null,
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
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Pobjednik: ${viewModel.columnLabel(viewModel.winnerColumn ?: 0)}",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = viewModel.wins.joinToString(" - "),
                            fontSize = (screenWidth.value * 0.06f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.rematch() },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .padding(bottom = 8.dp),
                            colors = buttonColors
                        ) {
                            Text(text = "Revanš", fontSize = (screenWidth.value * 0.06f).sp)
                        }
                        Button(
                            onClick = { navController.navigate("belaNewGame") },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .padding(bottom = 8.dp),
                            colors = buttonColors
                        ) {
                            Text(text = "Nova igra", fontSize = (screenWidth.value * 0.06f).sp)
                        }
                    }
                }
            }
        }
    }
}