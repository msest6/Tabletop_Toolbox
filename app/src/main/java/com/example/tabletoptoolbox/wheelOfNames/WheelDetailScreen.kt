package com.example.tabletoptoolbox.wheelOfNames

import android.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WheelDetail(
    navController: NavController,
    buttonColors: ButtonColors,
    wheelViewModel: WheelViewModel,
    detailViewModel: WheelDetailViewModel
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        // --- NAZIV ---
        OutlinedTextField(
            value = detailViewModel.name,
            onValueChange = {
                detailViewModel.name = it
                detailViewModel.clearNameError()
            },
            label = { Text("Naslov") },
            singleLine = true,
            isError = detailViewModel.nameError != null,
            supportingText = {
                detailViewModel.nameError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // --- KOLO ---
        WheelCanvas(
            entries = detailViewModel.entries,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(enabled = !detailViewModel.isSpinning) {
                    focusManager.clearFocus()
                    scope.launch { detailViewModel.spin() }
                },
            rotationDeg = detailViewModel.rotationDeg,
            highlightIndex = detailViewModel.highlightIndex
        )

        Spacer(Modifier.height(12.dp))

        // --- ALATI: shuffle + sort ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { detailViewModel.shuffle() }) {
                Icon(Icons.Default.Shuffle, contentDescription = "Izmiješaj")
            }
            IconButton(onClick = { detailViewModel.sort() }) {
                // strelica pokazuje smjer koji će sljedeći klik primijeniti
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sortiraj"
                )
                Icon(
                    if (detailViewModel.sortAscending) Icons.Default.KeyboardArrowDown
                    else Icons.Default.KeyboardArrowUp,
                    contentDescription = null
                )
            }
        }

        // --- EDITOR (jedan entry po retku) ---
        OutlinedTextField(
            value = detailViewModel.entriesText,
            onValueChange = {
                detailViewModel.entriesText = it
                detailViewModel.clearEntriesError()
            },
            label = { Text("Stavke") },
            isError = detailViewModel.entriesError != null,
            supportingText = {
                detailViewModel.entriesError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Obriši")
            }
            Button(
                onClick = { if (detailViewModel.validate()) showSaveDialog = true },
                colors = buttonColors,
                modifier = Modifier.weight(1f)
            ) {
                Text("Spremi")
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Spremi kolo") },
            text = { Text("Jeste li sigurni da želite spremiti izmjene?") },
            confirmButton = {
                TextButton(onClick = {
                    wheelViewModel.upsert(detailViewModel.toWheel())
                    showSaveDialog = false
                }) { Text("Spremi") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Odustani") }
            }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Obriši kolo") },
            text = { Text("Jeste li sigurni da želite obrisati ovo kolo?") },
            confirmButton = {
                TextButton(onClick = {
                    detailViewModel.currentId?.let { wheelViewModel.delete(it) }
                    detailViewModel.clear()
                    showDeleteDialog = false
                }) { Text("Obriši") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Odustani") }
            }
        )
    }
    detailViewModel.winner?.let { winner ->
        AlertDialog(
            onDismissRequest = { detailViewModel.dismissWinner() },
            title = { Text("Rezultat") },
            text = {
                Text(
                    winner,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            confirmButton = {
                TextButton(onClick = { detailViewModel.dismissWinner() }) {
                    Text("U redu")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { detailViewModel.removeWinner() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Obriši") }
            }
        )
    }
}