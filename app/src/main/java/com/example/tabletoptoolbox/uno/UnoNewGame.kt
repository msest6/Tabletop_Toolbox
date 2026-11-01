package com.example.tabletoptoolbox.uno

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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnoNewGame(navController: NavController, buttonColors: ButtonColors, viewModel: UnoViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // mutableStateListOf je bitan - obični mutableListOf nije observable pa Compose ne
    // detektira promjene i unos imena igrača se ne prikazuje u UI-u
    val playerNames = remember { mutableStateListOf("") }
    var looseScore by remember { mutableIntStateOf(1000) }
    var showError1 by remember { mutableStateOf(false) }
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Unesite imena igrača",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.width(140.dp)
                    ) {
                        OutlinedTextField(
                            value = if (looseScore == 0) "" else looseScore.toString(),
                            onValueChange = { newText ->
                                val parsed = newText.toIntOrNull()
                                if (newText.isBlank()) {
                                    looseScore = 0
                                } else if (parsed != null) {
                                    looseScore = parsed
                                }
                            },
                            label = { Text("Bodovi") },
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                // PrimaryEditable je ključan - default (PrimaryNotEditable) tretira
                                // polje kao read-only anchor pa prvi klik samo otvara/fokusira,
                                // a tek drugi klik pusti tipkanje
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(1000, 500).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.toString()) },
                                    onClick = {
                                        looseScore = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
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
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                items(playerNames.size, key = { index -> index }) { index ->
                    OutlinedTextField(
                        value = playerNames[index],
                        onValueChange = { newText ->
                            if (newText.length <= 8) {
                                playerNames[index] = newText
                                if (index == playerNames.lastIndex && newText.isNotBlank()) {
                                    playerNames.add("")
                                }

                                if (newText.isBlank() && index != playerNames.lastIndex) {
                                    playerNames.removeAt(index)
                                }
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
                        playerNames,
                        looseScore
                    )
                    navController.navigate("uno")
                } else {
                    showError1 = true
                }
            }
        ) {
            Text("Pokreni igru")
        }
        ElevatedButton(
            modifier = Modifier.align(Alignment.BottomStart),
            colors = buttonColors,
            onClick = {
                playerNames.clear()
                playerNames.add("")
            }
        ) {
            Text("Reset")
        }
    }
    LaunchedEffect(showError1) {
        if (showError1) {
            delay(3000)
            showError1 = false
        }
    }
}