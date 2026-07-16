package com.example.tabletoptoolbox.uno

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Uno(navController: NavController, buttonColors: ButtonColors, viewModel: UnoViewModel) {
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cilj: ${viewModel.looseScore} bodova",
                    fontSize = (screenWidth.value * 0.05f).sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { navController.navigate("unoRoundsList") }) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Pregled rundi")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.players.size) { index ->
                    val player = viewModel.players[index]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = player.playerName,
                                fontSize = (screenWidth.value * 0.05f).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (player.looseNumber > 0) {
                                Text(
                                    text = "💀 x${player.looseNumber}",
                                    fontSize = (screenWidth.value * 0.04f).sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "${player.score}",
                                fontSize = (screenWidth.value * 0.055f).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { navController.navigate("unoNewRound") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = buttonColors
            ) {
                Text(text = "Unesi novu partiju", fontSize = (screenWidth.value * 0.05f).sp)
            }
        }
    }

    if (viewModel.loosers.isNotEmpty()) {
        Dialog(onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }) {
            AnimatedVisibility(
                visible = viewModel.loosers.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(500)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(500)),
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
                            text = if (viewModel.loosers.size == 1) "Gubitnik:" else "Gubitnici:",
                            fontSize = (screenWidth.value * 0.07f).sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = viewModel.loosers.joinToString(", "),
                            fontSize = (screenWidth.value * 0.06f).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.acknowledgeLoosers() },
                            modifier = Modifier.width(screenWidth / 2),
                            colors = buttonColors
                        ) {
                            Text(text = "U redu", fontSize = (screenWidth.value * 0.055f).sp)
                        }
                    }
                }
            }
        }
    }
}