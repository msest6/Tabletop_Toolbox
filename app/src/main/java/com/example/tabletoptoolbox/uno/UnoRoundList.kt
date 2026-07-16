package com.example.tabletoptoolbox.uno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tabletoptoolbox.data.model.UnoRound

/**
 * Prikazuje sve odigrane runde trenutne igre s mogućnošću brisanja bilo koje (ne samo zadnje).
 * Uređivanje se radi nad lokalnom kopijom (draft) - "Odustani" odbacuje izmjene, "Spremi" ih
 * commita u UnoViewModel, koji zatim svakom igraču ponovno izračuna bodove iz preostalih rundi.
 */
@Composable
fun UnoRoundsList(navController: NavController, buttonColors: ButtonColors, viewModel: UnoViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val draftRounds = remember { mutableStateListOf<UnoRound>() }

    LaunchedEffect(Unit) {
        if (draftRounds.isEmpty() && viewModel.rounds.isNotEmpty()) {
            draftRounds.addAll(viewModel.rounds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pregled rundi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.popBackStack() }
            ) {
                Text(text = "Odustani", fontSize = (screenWidth.value * 0.05f).sp)
            }
            Button(
                onClick = {
                    viewModel.commitRoundsEdit(draftRounds)
                    navController.popBackStack()
                }
            ) {
                Text(text = "Spremi", fontSize = (screenWidth.value * 0.05f).sp)
            }
        }

        if (draftRounds.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nema odigranih rundi u ovoj igri.",
                    fontSize = (screenWidth.value * 0.045f).sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(draftRounds.size, key = { index -> index }) { index ->
                    val round = draftRounds[index]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}",
                                fontSize = (screenWidth.value * 0.04f).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Kod više od 2 igrača ne stane sve u jedan redak, pa prikazujemo
                                // samo prva dva i "..." kao naznaku da ih ima još.
                                val visiblePlayers = viewModel.players.take(2)
                                for (playerIndex in visiblePlayers.indices) {
                                    val playerName = visiblePlayers[playerIndex].playerName
                                    val score = round.scores.getOrElse(playerIndex) { 0 }
                                    Text(
                                        text = "$playerName: $score",
                                        fontSize = (screenWidth.value * 0.04f).sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (viewModel.players.size > 2) {
                                    Text(
                                        text = "...",
                                        fontSize = (screenWidth.value * 0.04f).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Kao i u pikadu - briše se samo zadnja runda, ostale su zatamnjene i neaktivne.
                            IconButton(
                                modifier = Modifier.alpha(if (index != draftRounds.size - 1) 0.3f else 1f),
                                onClick = {
                                    if (index == draftRounds.size - 1) {
                                        draftRounds.removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Obriši rundu ${index + 1}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}