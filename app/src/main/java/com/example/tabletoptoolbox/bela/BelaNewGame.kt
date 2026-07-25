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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.example.tabletoptoolbox.data.model.BelaMode

/**
 * Ekran za pokretanje nove igre Bele. Uvijek prikazuje unos za 4 igrača, neovisno
 * o odabranom modu. Za 2 i 3 igrača se pri pokretanju igre koriste fiksna imena
 * ("ja"/"protivnik" za 2, "ja"/"lijevi"/"desni" za 3) - upisana imena se za te
 * modove ne koriste. Za 4 igrača koriste se stvarno upisana imena.
 * "Odustani" vraća na izbor igara bez spremanja, "Nastavi" pokreće novu igru.
 */
@Composable
fun BelaNewGame(navController: NavController, buttonColors: ButtonColors, viewModel: BelaViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var selectedMode by remember { mutableStateOf(BelaMode.FOUR) }
    var selectedTargetScore by remember(selectedMode) { mutableStateOf(selectedMode.defaultTargetScore) }

    val labels = when (selectedMode) {
        BelaMode.FOUR -> listOf("Ja", "Lijevi", "Suigrač", "Desni")
        BelaMode.THREE -> listOf("Ja", "Desni", "Lijevi")
        BelaMode.TWO_OPEN, BelaMode.TWO_CLOSED -> listOf("Ja", "Protivnik")
    }

    // Polja se resetiraju na prazno svaki put kad se promijeni mod,
    // jer se broj i raspored polja mijenjaju.
    var names by remember(selectedMode) { mutableStateOf(List(labels.size) { "" }) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BelaModeDropdown(
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it }
            )
            BelaTargetScoreDropdown(
                selectedScore = selectedTargetScore,
                onScoreSelected = { selectedTargetScore = it }
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nova igra",
                fontSize = (screenWidth.value * 0.07f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                key(selectedMode) {
                    BelaPlayerNameFields(
                        labels = labels,
                        names = names,
                        onNameChange = { index, value ->
                            names = names.toMutableList().also { it[index] = value }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val playerNames = names.mapIndexed { i, n ->
                            n.trim().ifBlank { labels.getOrElse(i) { "Igrač ${i + 1}" } }
                        }.toMutableList()
                        if (selectedMode == BelaMode.FOUR) {
                            val tmp = playerNames[1]
                            playerNames[1] = playerNames[3]
                            playerNames[3] = tmp
                        }
                        viewModel.startNewGame(selectedMode, playerNames, selectedTargetScore)
                        navController.navigate("bela")
                    },
                    modifier = Modifier
                        .width(screenWidth / 2)
                        .height(screenWidth / 6),
                    colors = buttonColors,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text(text = "Započni partiju", fontSize = (screenWidth.value * 0.05f).sp)
                }
            }
        }
    }
}