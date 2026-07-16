package com.example.tabletoptoolbox.graddrzava

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonColors
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

@Composable
fun GradDrzavaNewGame(navController: NavController, buttonColors: ButtonColors, viewModel: GradDrzavaViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val categoriesList = remember { mutableStateListOf("") }
    var showError1 by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
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
                    text = "Unesite imena kategorija",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(
                    colors = buttonColors,
                    onClick = {
                        categoriesList.clear()
                        categoriesList.add("")
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
                            text = "⚠️ Unesite barem jednu kategoriju!",
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
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriesList.size, key = { index -> index }) { index ->
                    OutlinedTextField(
                        value = categoriesList[index],
                        onValueChange = { newText ->
                            categoriesList[index] = newText
                            if (index == categoriesList.lastIndex && newText.isNotBlank()) {
                                categoriesList.add("")
                            }

                            if (newText.isBlank() && index != categoriesList.lastIndex) {
                                categoriesList.removeAt(index)
                            }
                        },
                        label = { Text("Kategorija ${index + 1}") },
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
                if (categoriesList.any { it.isNotBlank() }) {
                    viewModel.updateCategories(
                        categoriesList
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    )
                    viewModel.resetScore()
                    viewModel.resetToLetterA()
                    navController.navigate("gradDrzava")
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
}