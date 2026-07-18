package com.example.tabletoptoolbox.tabletier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tabletoptoolbox.data.boardGames.GameEntity
import com.example.tabletoptoolbox.data.boardGames.SortColumn
import com.example.tabletoptoolbox.data.boardGames.SortDirection
import com.example.tabletoptoolbox.data.boardGames.SortState
import com.example.tabletoptoolbox.data.boardGames.formatPlayers
import java.io.File
import kotlin.math.roundToInt

// Fiksne širine stupaca - moraju biti iste u headeru i u redovima da se poravnaju.
private val ImageColumnWidth = 56.dp
private val NameColumnWidth = 160.dp
private val RatingColumnWidth = 80.dp
private val EloColumnWidth = 80.dp
private val TimeColumnWidth = 100.dp
private val PlayersColumnWidth = 100.dp

@Composable
fun TableTier(
    navController: NavController,
    buttonColors: ButtonColors,
    viewModel: TableTierViewModel,
    horizontalScrollState: ScrollState
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val sortState by viewModel.sortState.collectAsStateWithLifecycle()
    val selectedGame by viewModel.selectedGame.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TableTierActionsRow(
            onEloClicked = { navController.navigate("elo") },
            onAddGameClicked = { navController.navigate("tableTierNewGame") },
            buttonColors = buttonColors
        )
        HorizontalDivider()

        TableHeaderRow(
            sortState = sortState,
            onColumnClicked = viewModel::onColumnClicked,
            horizontalScrollState = horizontalScrollState
        )
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(games, key = { it.id }) { game ->
                GameRow(
                    game = game,
                    onClick = { viewModel.onGameClicked(game) },
                    horizontalScrollState = horizontalScrollState
                )
                HorizontalDivider(thickness = 1.dp)
            }
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

@Composable
private fun TableTierActionsRow(
    onEloClicked: () -> Unit,
    onAddGameClicked: () -> Unit,
    buttonColors: ButtonColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ElevatedButton(
            colors = buttonColors,
            onClick = onEloClicked,
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "Evaluacija igara",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Elo")
        }

        Text(
            text = "Tablica Igara",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ElevatedButton(
            colors = buttonColors,
            onClick = onAddGameClicked,
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj igru",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TableHeaderRow(
    sortState: SortState,
    onColumnClicked: (SortColumn) -> Unit,
    horizontalScrollState: ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(ImageColumnWidth)) // prostor za sliku

        HeaderCell(
            label = "Naziv",
            column = SortColumn.NAME,
            sortState = sortState,
            onColumnClicked = onColumnClicked,
            modifier = Modifier.width(NameColumnWidth)
        )
        HeaderCell(
            label = "Ocjena",
            column = SortColumn.RATING,
            sortState = sortState,
            onColumnClicked = onColumnClicked,
            modifier = Modifier.width(RatingColumnWidth)
        )
        HeaderCell(
            label = "Elo",
            column = SortColumn.ELO,
            sortState = sortState,
            onColumnClicked = onColumnClicked,
            modifier = Modifier.width(EloColumnWidth)
        )
        HeaderCell(
            label = "Trajanje",
            column = SortColumn.TIME,
            sortState = sortState,
            onColumnClicked = onColumnClicked,
            modifier = Modifier.width(TimeColumnWidth)
        )
        HeaderCellStatic(
            label = "Igrača",
            modifier = Modifier.width(PlayersColumnWidth)
        )
    }
}

@Composable
private fun HeaderCell(
    label: String,
    column: SortColumn,
    sortState: SortState,
    onColumnClicked: (SortColumn) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onColumnClicked(column) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        if (sortState.column == column) {
            Icon(
                imageVector = if (sortState.direction == SortDirection.ASC)
                    Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Stupac zaglavlja bez mogućnosti sortiranja (Trajanje, Igrača). */
@Composable
private fun HeaderCellStatic(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun GameRow(
    game: GameEntity,
    onClick: () -> Unit,
    horizontalScrollState: ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .horizontalScroll(horizontalScrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = game.imagePath?.let { File(it) },
            contentDescription = game.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = game.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(NameColumnWidth)
        )
        Text(
            text = String.format("%.1f", game.userRating),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(RatingColumnWidth)
        )
        Text(
            text = game.eloRating.roundToInt().toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(EloColumnWidth)
        )
        Text(
            text = "${game.playTimeMinutes} min",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(TimeColumnWidth)
        )
        Text(
            text = game.formatPlayers(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(PlayersColumnWidth)
        )
    }
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