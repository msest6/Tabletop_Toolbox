package com.example.tabletoptoolbox.darts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

/**
 * Prikazuje grid za unos parametara igre. Double-in/double-out prekidači se spremaju odmah
 * pri promjeni (kroz viewModel), dok se imena igrača i tip igre spremaju tek na "Pokreni igru".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DartsNewGame(navController: NavController, buttonColors: ButtonColors, viewModel: DartsViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val playerNames = remember { mutableStateListOf("") }
    var newGame by remember { mutableStateOf("901") }
    var showError1 by remember { mutableStateOf(false) }
    var showError2 by remember { mutableStateOf(false) }
    var showError3 by remember { mutableStateOf(false) }
    val possibleGamesList = remember { mutableStateListOf("301", "501", "701", "901") } //TODO: dodaj cricket
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = newGame,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Igra:") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(screenWidth / 3f)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        possibleGamesList.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game) },
                                onClick = {
                                    newGame = game
                                    expanded = false
                                    showError2 = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(screenWidth / 7))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Double-in ", fontSize = (screenWidth.value * 0.04f).sp)
                        Spacer(modifier = Modifier.width(screenWidth * 0.05f))
                        Switch(
                            checked = viewModel.doubleIn,
                            onCheckedChange = { viewModel.updateDoubleIn(it) },
                            modifier = Modifier.size(screenWidth / 8)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Double-out", fontSize = (screenWidth.value * 0.04f).sp)
                        Spacer(modifier = Modifier.width(screenWidth * 0.05f))
                        Switch(
                            checked = viewModel.doubleOut,
                            onCheckedChange = { viewModel.updateDoubleOut(it) },
                            modifier = Modifier.size(screenWidth / 8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Unesite imena igrača",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(
                    colors = buttonColors,
                    onClick = {
                        playerNames.clear()
                        playerNames.add("")
                        newGame = "901"
                        viewModel.updateDoubleIn(false)
                        viewModel.updateDoubleOut(true)
                    }
                ) {
                    Text("Reset")
                }
            }

            AnimatedVisibility(
                visible = showError1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Unesite barem jednog igrača!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError1 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showError2,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Igra mora biti jedna od ponuđenih: ${possibleGamesList.joinToString(", ")}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError2 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showError3,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Imena igrača mogu imati max 10 slova!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError3 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable container
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                items(playerNames.size, key = { index -> index }) { index ->
                    OutlinedTextField(
                        value = playerNames[index],
                        onValueChange = { newText ->
                            if (newText.length <= 10) {
                                playerNames[index] = newText
                            } else {
                                showError3 = true
                            }
                            if (index == playerNames.lastIndex && newText.isNotBlank() && playerNames.count() < 8) {
                                playerNames.add("")
                            }

                            if (newText.isBlank() && index != playerNames.lastIndex) {
                                playerNames.removeAt(index)
                            }
                        },
                        label = { Text("Igrač ${index + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        ElevatedButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            colors = buttonColors,
            onClick = {
                if (playerNames.any { it.isNotBlank() }) {
                    viewModel.startNewGame(
                        playerNames
                            .map { it.trim() }
                            .filter { it.isNotBlank() },
                        newGame,
                        viewModel.doubleIn,
                        viewModel.doubleOut
                    )
                    navController.navigate("dartsNew")
                } else {
                    showError1 = true
                }
            }
        ) {
            Text("Pokreni igru")
        }
    }
    LaunchedEffect(showError1) {
        if (showError1) {
            delay(3000)
            showError1 = false
        }
    }
    LaunchedEffect(showError2) {
        if (showError2) {
            delay(10000)
            showError2 = false
        }
    }
    LaunchedEffect(showError3) {
        if (showError3) {
            delay(10000)
            showError3 = false
        }
    }
}