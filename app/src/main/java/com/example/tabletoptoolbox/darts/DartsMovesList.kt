package com.example.tabletoptoolbox.darts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tabletoptoolbox.data.model.DartThrow

/**
 * Prikazuje listu odigranih poteza s mogućnošću brisanja zadnjeg. Uređivanje se radi nad lokalnom
 * kopijom (draft) - "Odustani" odbacuje izmjene, "Spremi" ih commita u DartsViewModel.
 */
@Composable
fun DartsMovesList(navController: NavController, buttonColors: ButtonColors, viewModel: DartsViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val draftMoves = remember { mutableStateListOf<DartThrow>() }
    var draftCurrentPlayer by remember { mutableIntStateOf(viewModel.currentPlayerIndex) }

    // Rezervirane širine stupaca - ime max 10 znakova, svaki score max 2 znamenke.
    // Fontovi u redu skaliraju se s 0.05f * screenWidth, pa širine računamo iz iste jedinice.
    // VAŽNO: ovo su širine za SAM SADRŽAJ (bez horizontalnog paddinga) - horizontalni razmak
    // između stupaca ide preko Arrangement.spacedBy na roditeljskom Row-u, a ne preko
    // .padding() na pojedinom Text-u, jer bi padding unutar fiksne širine "pojeo" prostor
    // predviđen za sadržaj i rezao dvoznamenkaste brojeve.
    val rowFontSizeDp = screenWidth.value * 0.05f
    val nameColumnWidth = (rowFontSizeDp * 0.65f * 10).dp   // mjesto za 10 znakova
    val scoreColumnWidth = (rowFontSizeDp * 0.9f * 2).dp    // mjesto za 2 znamenke

    LaunchedEffect(Unit) {
        if (draftMoves.isEmpty()) {
            draftMoves.addAll(viewModel.moves)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    viewModel.commitMovesEdit(draftMoves, draftCurrentPlayer)
                    navController.navigate("darts")
                },
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = "Spremi",
                    modifier = Modifier.padding(15.dp),
                    textAlign = TextAlign.Center,
                    fontSize = (screenWidth.value * 0.05f).sp
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(draftMoves.size, key = { index -> index }) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = viewModel.playerNames[draftMoves[index].playerIndex],
                        modifier = Modifier.width(nameColumnWidth),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = (screenWidth.value * 0.05f).sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = draftMoves[index].throws[0].toString(),
                            modifier = Modifier.width(scoreColumnWidth),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = (screenWidth.value * 0.05f).sp
                        )
                        Text(
                            text = draftMoves[index].throws[1].toString(),
                            modifier = Modifier.width(scoreColumnWidth),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = (screenWidth.value * 0.05f).sp
                        )
                        Text(
                            text = draftMoves[index].throws[2].toString(),
                            modifier = Modifier.width(scoreColumnWidth),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = (screenWidth.value * 0.05f).sp
                        )
                    }
                    Button(
                        modifier = Modifier.padding(10.dp).alpha(if (index != draftMoves.size - 1) 0.3f else 1f),
                        onClick = {
                            if (index == draftMoves.size - 1) {
                                draftMoves.removeAt(index)
                                draftCurrentPlayer -= 1
                                if (draftCurrentPlayer == -1) {
                                    draftCurrentPlayer = viewModel.playerNames.size - 1
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "X",
                            modifier = Modifier.size((screenWidth.value * 0.08f).dp)
                        )
                    }
                }
            }
        }
    }
}