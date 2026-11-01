package com.example.tabletoptoolbox.bela

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tabletoptoolbox.data.model.BelaMode
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

/**
 * Ekran za unos rezultata jedne partije. Broj polja ovisi o viewModel.mode.columnCount.
 * Kad mod podržava auto-fill (otvorena Bela s 2 igrača/4 igrača), unos bodova jedne strane
 * automatski izračuna drugu (zbroj je uvijek 162) - i dalje se može ručno prepisati.
 */
@Composable
fun BelaNewRound(navController: NavController, buttonColors: ButtonColors, viewModel: BelaViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val mode = viewModel.mode

    val scoreFields = remember(mode) { mutableStateOf(List(mode.columnCount) { "" }) }
    val zvanjaFields = remember(mode) { mutableStateOf(List(mode.columnCount) { "" }) }
    var showError by remember { mutableStateOf(false) }
    var showError2 by remember { mutableStateOf(false) }
    var showError3 by remember { mutableStateOf(false) }
    var showError4 by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var zvaoIndex by remember(mode) { mutableStateOf<Int?>(null) }
    var belotConfirmIndex by remember(mode) { mutableStateOf<Int?>(null) }

    fun updateZvanja(index: Int, newValue: String) {
        if (newValue.isNotEmpty() && !newValue.all { it.isDigit() }) return
        val updated = zvanjaFields.value.toMutableList()
        updated[index] = newValue
        zvanjaFields.value = updated
    }

    fun addZvanja(index: Int, addedValue: String){
        if (addedValue.isNotEmpty() && !addedValue.all { it.isDigit() }) return
        val updated = zvanjaFields.value.toMutableList()
        if (updated[index] == ""){
            updated[index] = addedValue
        }
        else updated[index] = (addedValue.toInt() + updated[index].toInt()).toString()
        zvanjaFields.value = updated
    }

    fun updateScore(index: Int, newValue: String) {
        if (newValue.isNotEmpty() && !newValue.all { it.isDigit() }) return
        val updated = scoreFields.value.toMutableList()
        updated[index] = newValue
        val entered = newValue.toIntOrNull()

        if (entered == 162) {
            // Netko je pokupio sve bodove - ostali automatski idu na 0 (vrijedi za sve modove).
            for (i in updated.indices) {
                if (i != index) updated[i] = "0"
            }
        } else if (mode.supportsAutoFill && mode.columnCount == 2) {
            val otherIndex = 1 - index
            if (entered != null) {
                updated[otherIndex] = viewModel.autoFillPartner(entered)?.toString() ?: ""
            }
        } else if (mode.columnCount == 3 && entered != null) {
            val otherIndices = (0 until mode.columnCount).filter { it != index }
            val filled = otherIndices.filter { updated[it].isNotEmpty() }
            val empty = otherIndices.filter { updated[it].isEmpty() }
            if (filled.size == 1 && empty.size == 1) {
                val otherValue = updated[filled[0]].toIntOrNull() ?: 0
                val remaining = 162 - entered - otherValue
                if (remaining in 0..162) {
                    updated[empty[0]] = remaining.toString()
                }
            }
        }

        scoreFields.value = updated
        showError = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    for (col in 0 until mode.columnCount) {
                        updateScore(col, "")
                        updateZvanja(col, "")
                    }
                    zvaoIndex = null
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(screenWidth / 5),
                colors = buttonColors,
                contentPadding = PaddingValues(2.dp)
            ) {
                Text(
                    text = "Reset",
                    fontSize = (screenWidth.value * 0.05f).sp
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zove",
                    fontSize = (screenWidth.value * 0.07f).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (col in 0 until mode.columnCount) {
                        var scoreFieldValue by remember(mode, col) {
                            mutableStateOf(TextFieldValue(text = scoreFields.value[col]))
                        }
                        var zvanjaFieldValue by remember(mode, col) {
                            mutableStateOf(TextFieldValue(text = zvanjaFields.value[col]))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RadioButton(
                                selected = zvaoIndex == col,
                                onClick = {
                                    zvaoIndex = if (zvaoIndex == col) null else col
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.columnLabel(col),
                                fontWeight = FontWeight.Bold,
                                fontSize = (screenWidth.value * 0.05f).sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = scoreFieldValue,
                                onValueChange = { newValue ->
                                    val newText = newValue.text
                                    if (newText.isEmpty() || (newText.toIntOrNull() != null && newText.toInt() <= 162)) {
                                        updateScore(col, newText)
                                        scoreFieldValue = newValue
                                    } else if (newText.toIntOrNull() != null && newText.toInt() > 162) {
                                        updateScore(col, "162")
                                        scoreFieldValue = newValue.copy(text = "162")
                                    }
                                },
                                label = { Text("Bodovi") },
                                singleLine = true,
                                modifier = Modifier
                                    .width(screenWidth / (mode.columnCount + 1))
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            scoreFieldValue = scoreFieldValue.copy(
                                                selection = TextRange(
                                                    0,
                                                    scoreFieldValue.text.length
                                                )
                                            )
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = zvanjaFieldValue,
                                onValueChange = { newValue ->
                                    updateZvanja(col, newValue.text)
                                    zvanjaFieldValue = newValue
                                },
                                label = { Text("Zvanja") },
                                singleLine = true,
                                modifier = Modifier
                                    .width(screenWidth / (mode.columnCount + 1))
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            zvanjaFieldValue = zvanjaFieldValue.copy(
                                                selection = TextRange(
                                                    0,
                                                    zvanjaFieldValue.text.length
                                                )
                                            )
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                supportingText = {
                                    Text(
                                        text = "Ukupno: ${(if (scoreFields.value[col] == "") 0 else scoreFields.value[col].toInt()) + (if (zvanjaFields.value[col] == "") 0 else zvanjaFields.value[col].toInt())}",
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { addZvanja(col, "20") },
                                    modifier = Modifier
                                        .width(screenWidth / 9)
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "20", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { addZvanja(col, "50") },
                                    modifier = Modifier
                                        .width(screenWidth / 9)
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "50", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { addZvanja(col, "90") },
                                    modifier = Modifier
                                        .width(screenWidth / 9)
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "90", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { addZvanja(col, "100") },
                                    modifier = Modifier
                                        .width(screenWidth / 9)
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "100", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { addZvanja(col, "150") },
                                    modifier = Modifier
                                        .width(screenWidth / 9)
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "150", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { addZvanja(col, "200") },
                                    modifier = Modifier
                                        .width(screenWidth / 9)
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "200", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { belotConfirmIndex = col },
                                    modifier = Modifier
                                        .width(screenWidth / (mode.columnCount + 1))
                                        .height(screenWidth / 9),
                                    colors = buttonColors,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(text = "Belot", fontSize = (screenWidth.value * 0.05f).sp)
                                }
                            }
                        }
                        LaunchedEffect(scoreFields.value[col]) {
                            if (scoreFieldValue.text != scoreFields.value[col]) {
                                scoreFieldValue = scoreFieldValue.copy(
                                    text = scoreFields.value[col],
                                    selection = TextRange(scoreFields.value[col].length)
                                )
                            }
                        }
                        LaunchedEffect(zvanjaFields.value[col]) {
                            if (zvanjaFieldValue.text != zvanjaFields.value[col]) {
                                zvanjaFieldValue = zvanjaFieldValue.copy(
                                    text = zvanjaFields.value[col],
                                    selection = TextRange(zvanjaFields.value[col].length)
                                )
                            }
                        }
                    }
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ Unesite bodove za sve igrače/timove!",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (showError2) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ Zbroj bodova bez zvanja mora biti 162!",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (showError3) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ Zbroj bodova bez zvanja može biti najviše 162!",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (showError4) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ Pogrešan unos zvanja!",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                LaunchedEffect(showError) {
                    if (showError) {
                        delay(10000)
                        showError = false
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
                LaunchedEffect(showError4) {
                    if (showError4) {
                        delay(10000)
                        showError4 = false
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val scores = scoreFields.value.map { it.toIntOrNull() }
                            val zvanja = zvanjaFields.value.map { it.toIntOrNull() ?: 0 }
                            val total = scores.indices.sumOf { i -> (scores[i] ?: 0) }
                            val totalZvanja = zvanja.indices.sumOf { i -> (zvanja[i] ?: 0) }
                            if (scores.any { it == null }) {
                                showError = true
                            } else if (total != 162 && mode != BelaMode.TWO_CLOSED) {
                                showError2 = true
                            } else if (total > 162) {
                                showError3 = true
                            } else if (totalZvanja % 10 != 0) {
                                showError4 = true
                            } else {
                                viewModel.addRound(
                                    scores.map { it!! }.toMutableList(),
                                    zvanja.toMutableList(),
                                    zvaoIndex
                                )
                                navController.navigate("bela")
                            }
                        },
                        modifier = Modifier
                            .width(screenWidth / 3)
                            .height(screenWidth / 7),
                        colors = buttonColors,
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "Spremi", fontSize = (screenWidth.value * 0.05f).sp)
                    }
                }
            }
            belotConfirmIndex?.let { col ->
                AlertDialog(
                    onDismissRequest = { belotConfirmIndex = null },
                    title = { Text("Potvrda belota") },
                    text = { Text("Je li tim \"${viewModel.columnLabel(col)}\" ostvario belot?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.declareBelot(col)
                                belotConfirmIndex = null
                                navController.navigate("bela")
                            },
                            colors = buttonColors
                        ) {
                            Text("Da")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { belotConfirmIndex = null },
                            colors = buttonColors
                        ) {
                            Text("Ne")
                        }
                    }
                )
            }
        }
    }
}