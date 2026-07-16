package com.example.tabletoptoolbox.bela

import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabletoptoolbox.data.model.BelaMode

/**
 * Ikona koja otvara padajući izbornik za odabir broja igrača - koristi je i ekran
 * za novu igru i glavni ekran s rezultatima. Klik na ikonu otvara opcije; odabir zatvara meni.
 */
@Composable
fun BelaModeDropdown(selectedMode: BelaMode, onModeSelected: (BelaMode) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Broj igrača: ${selectedMode.label}"
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BelaMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}
/**
 * Ikona/gumb koji otvara padajući izbornik za odabir ciljnog broja bodova (501/701/1001).
 * Koristi ga BelaNewGame prilikom postavljanja nove igre.
 */
@Composable
fun BelaTargetScoreDropdown(
    selectedScore: Int,
    onScoreSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = "$selectedScore")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BelaMode.AVAILABLE_TARGET_SCORES.forEach { score ->
                DropdownMenuItem(
                    text = { Text(score.toString()) },
                    onClick = {
                        onScoreSelected(score)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BelaNameField(initialValue: String, label: String, onValueChange: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var fieldValue by remember { mutableStateOf(TextFieldValue(initialValue)) }
    var isFocused by remember { mutableStateOf(false) }
    val maxChar = 8

    LaunchedEffect(isFocused) {
        if (isFocused) {
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
        }
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { newValue ->
            if (newValue.text.length <= maxChar){
                fieldValue = newValue
                onValueChange(newValue.text)
            }
        },
        label = { Text(label) },
        supportingText = {
            Text(
                text = "${fieldValue.text.length} / $maxChar",
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        singleLine = true,
        modifier = Modifier
            .width(screenWidth / 3)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}

/**
 * Polja za unos imena igrača - uvijek prikazuje 4 polja u "stol" rasporedu
 * (suigrač gore, lijevi/desni sa strane, ja dolje), neovisno o odabranom modu.
 * Za modove s manje igrača (2 ili 3) ova imena se ne koriste - tamo se koriste
 * fiksna imena (vidi BelaNewGame - poziv na viewModel.startNewGame). Svako polje
 * dolazi pred-popunjeno početnom vrijednosti; klik u polje selektira sav tekst.
 */
@Composable
fun BelaPlayerNameFields(
    labels: List<String>,
    names: List<String>,
    onNameChange: (Int, String) -> Unit
) {
    when (labels.size) {
        4 -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BelaNameField(names.getOrElse(2) { "" }, labels[2]) { onNameChange(2, it) }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BelaNameField(names.getOrElse(1) { "" }, labels[1]) { onNameChange(1, it) }
                    BelaNameField(names.getOrElse(3) { "" }, labels[3]) { onNameChange(3, it) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BelaNameField(names.getOrElse(0) { "" }, labels[0]) { onNameChange(0, it) }
            }
        }
        3 -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BelaNameField(names.getOrElse(0) { "" }, labels[0]) { onNameChange(0, it) }
                Spacer(modifier = Modifier.height(16.dp))
                BelaNameField(names.getOrElse(1) { "" }, labels[1]) { onNameChange(1, it) }
                Spacer(modifier = Modifier.height(16.dp))
                BelaNameField(names.getOrElse(2) { "" }, labels[2]) { onNameChange(2, it) }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        else -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                BelaNameField(names.getOrElse(0) { "" }, labels[0]) { onNameChange(0, it) }
                Spacer(modifier = Modifier.height(16.dp))
                BelaNameField(names.getOrElse(1) { "" }, labels[1]) { onNameChange(1, it) }
            }
        }
    }
}

/** Prikazuje ukupne rezultate po stupcu (tim ili igrač, ovisno o modu). */
@Composable
fun BelaScoreboard(viewModel: BelaViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(24.dp))
        for (col in 0 until viewModel.mode.columnCount) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.columnLabel(col),
                    fontSize = if (viewModel.mode != BelaMode.THREE) (screenWidth.value * 0.07f).sp else (screenWidth.value * 0.04f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.winnerColumn == col) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = viewModel.totals.getOrElse(col) { 0 }.toString(),
                    fontSize = (screenWidth.value * 0.09f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width((screenWidth.value * 0.1f).dp))
    }
}

/** Prikazuje povijest odigranih partija, jednu po retku. */
@Composable
fun BelaRoundsList(viewModel: BelaViewModel, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    LazyColumn(modifier = modifier) {
        itemsIndexed(viewModel.rounds) { index, round ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}.",
                    fontSize = (screenWidth.value * 0.04f).sp,
                    modifier = Modifier.width(24.dp)
                )
                for (col in 0 until viewModel.mode.columnCount) {
                    val score = round.scores.getOrElse(col) { 0 }
                    val zvanja = round.zvanja.getOrElse(col) { 0 }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (zvanja != 0) "$score (+$zvanja)" else "$score",
                            fontSize = (screenWidth.value * 0.04f).sp
                        )
                    }
                }
                Button(
                    modifier = Modifier
                        .size((screenWidth.value * 0.1f).dp)
                        .alpha(if (index != viewModel.rounds.lastIndex) 0.3f else 1f),
                    contentPadding = PaddingValues(2.dp),
                    onClick = {
                        if (index == viewModel.rounds.lastIndex) {
                            viewModel.removeLastRound()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Obriši",
                        modifier = Modifier.size((screenWidth.value * 0.06f).dp)
                    )
                }
            }
        }
    }
}