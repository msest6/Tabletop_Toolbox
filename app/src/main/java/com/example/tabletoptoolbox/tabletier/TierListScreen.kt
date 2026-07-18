package com.example.tabletoptoolbox.tabletier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tabletoptoolbox.data.boardGames.GameEntity
import com.example.tabletoptoolbox.data.boardGames.formatPlayers
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TierListScreen(navController: NavController, viewModel: TierListViewModel) {
    val tieredGames by viewModel.tieredGames.collectAsStateWithLifecycle()
    val selectedGame by viewModel.selectedGame.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(Tier.entries.toList(), key = { it.name }) { tier ->
            TierRow(
                tier = tier,
                games = tieredGames[tier].orEmpty(),
                onGameClicked = viewModel::onGameClicked
            )
            HorizontalDivider(thickness = 1.dp)
        }
    }

    selectedGame?.let { game ->
        GameDetailDialog(
            game = game,
            onDismiss = viewModel::onDialogDismissed,
            onEdit = {
                viewModel.onDialogDismissed()
                navController.navigate("tableTierNewGame?gameId=${game.id}")
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TierRow(
    tier: Tier,
    games: List<GameEntity>,
    onGameClicked: (GameEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // omogućava da Box ispod ispuni punu visinu reda
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .background(tierColor(tier)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tier.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Nema igara u ovom tieru",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                games.forEach { game ->
                    AsyncImage(
                        model = game.imagePath?.let { File(it) },
                        contentDescription = game.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onGameClicked(game) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

private fun tierColor(tier: Tier): Color = when (tier) {
    Tier.S -> Color(0xFFF5584D)
    Tier.A -> Color(0xFFFF9800)
    Tier.B -> Color(0xFFF6DE64)
    Tier.C -> Color(0xFF8BC34A)
    Tier.D -> Color(0xFF00BCD4)
    Tier.F -> Color(0xFF673AB7)
}

@Composable
private fun GameDetailDialog(game: GameEntity, onDismiss: () -> Unit, onEdit: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = game.imagePath?.let { File(it) },
                    contentDescription = game.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = game.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RatingChip(label = "Ocjena", value = String.format("%.1f", game.userRating))
                    RatingChip(label = "Elo", value = game.eloRating.roundToInt().toString())
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RatingChip(label = "Trajanje", value = "${game.playTimeMinutes} min")
                    RatingChip(label = "Igrača", value = game.formatPlayers())
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                ElevatedButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Uredi")
                }
            }
        }
    }
}

@Composable
private fun RatingChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}