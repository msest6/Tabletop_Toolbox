package com.example.tabletoptoolbox.boardgames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ExposureNeg1
import androidx.compose.material.icons.filled.ExposurePlus1
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tabletoptoolbox.R

@Composable
fun BoardGames(
    navController: NavController,
    buttonColors: ButtonColors,
    viewModel: BoardGameViewModel
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    // Tekstualno polje za "za pobjedu" je čisto UI stanje (kursor, selekcija) -
    // sinkronizira se s ViewModelom jednom, kad se podaci učitaju.
    var winScoreField by remember(viewModel.isLoaded) {
        mutableStateOf(TextFieldValue(viewModel.winScore.toString()))
    }
    var showError1 by remember { mutableStateOf(false) }
    var popUp1 by remember { mutableStateOf(false) }
    var popUp2 by remember { mutableStateOf(false) }
    var popUp3 by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            winScoreField = winScoreField.copy(
                selection = TextRange(0, winScoreField.text.length)
            )
        }
    }

    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                OutlinedTextField(
                    value = winScoreField,
                    onValueChange = { newText ->
                        winScoreField = newText
                    },
                    label = { Text("Za Pobjedu:") },
                    singleLine = true,
                    modifier = Modifier
                        .width(screenWidth / 3f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                val parsed = viewModel.parseWinScoreInput(winScoreField.text)
                                if (parsed == null) {
                                    winScoreField = TextFieldValue(viewModel.winScore.toString())
                                    showError1 = true
                                } else {
                                    viewModel.updateWinScore(parsed)
                                    showError1 = false
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button(
                        onClick = {
                            popUp1 = true
                        },
                        modifier = Modifier.width(screenWidth / 6).height(screenWidth / 6),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kocka),
                            contentDescription = "Kocka",
                            modifier = Modifier
                                .scale(2.2f),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            popUp2 = true
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(screenWidth / 6)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Coin",
                            modifier = Modifier.scale(2.2f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Unesite imena igrača",
                    fontSize = (screenWidth.value * 0.05f).sp,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(
                    colors = buttonColors,
                    onClick = {
                        popUp3 = true
                    }
                ) {
                    Text(
                        text = "Reset",
                        fontSize = (screenWidth.value * 0.05f).sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
                            text = "⚠️ Unos u polje za pobjedu mora biti broj!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError1 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.players.size, key = { index -> index }) { index ->
                    val player = viewModel.players[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedTextField(
                            value = player.playerName,
                            onValueChange = { newText: String ->
                                viewModel.updatePlayerName(index, newText)
                            },
                            label = { Text(if (player.playerName != "") "Pobjede: ${player.winNumber}" else "Novi igrač") },
                            singleLine = true,
                            modifier = Modifier.width(screenWidth / 3)
                        )
                        var scoreField by remember(player.score) {
                            mutableStateOf(TextFieldValue(player.score.toString()))
                        }
                        var isScoreFocused by remember { mutableStateOf(false) }

                        LaunchedEffect(isScoreFocused) {
                            if (isScoreFocused) {
                                withFrameNanos { } // pričekaj default postavljanje kursora
                                scoreField = scoreField.copy(selection = TextRange(0, scoreField.text.length))
                            }
                        }

                        BasicTextField(
                            value = scoreField,
                            onValueChange = { scoreField = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = (screenWidth.value * 0.08f).sp,
                                color = LocalContentColor.current
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .onFocusChanged { focusState ->
                                    isScoreFocused = focusState.isFocused
                                    if (!focusState.isFocused) {
                                        val parsed = scoreField.text.trim().toIntOrNull()
                                        if (parsed != null && parsed != player.score) {
                                            viewModel.setScore(index, parsed)
                                        } else {
                                            // neispravan unos ili bez promjene: vrati stari broj
                                            scoreField = TextFieldValue(player.score.toString())
                                        }
                                    }
                                }
                        )
                        Button(
                            colors = buttonColors,
                            modifier = Modifier.width(screenWidth / 8).height(screenWidth / 8)
                                .alpha(if (player.playerName == "") 0.3f else 1f),
                            shape = RoundedCornerShape(6.dp),
                            onClick = { viewModel.incrementScore(index) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExposurePlus1,
                                contentDescription = "plusOne",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Button(
                            colors = buttonColors,
                            modifier = Modifier.width(screenWidth / 8).height(screenWidth / 8)
                                .alpha(if (player.playerName == "") 0.3f else 1f),
                            shape = RoundedCornerShape(6.dp),
                            onClick = { viewModel.decrementScore(index) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExposureNeg1,
                                contentDescription = "minusOne",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(showError1) {
        if (showError1) {
            kotlinx.coroutines.delay(10000)
            showError1 = false
        }
    }
    if (viewModel.winner != "") {
        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {

            AnimatedVisibility(
                visible = viewModel.winner != "",
                enter = fadeIn(animationSpec = tween(2000)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(2000)),
                exit = fadeOut() + scaleOut()
            ) {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = "Pobjednik: ${viewModel.winner}",
                            fontSize = (screenWidth.value * 0.06f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = { viewModel.acknowledgeWinner() },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "Nastavi",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (popUp1) {
        var rolled by remember { mutableStateOf(viewModel.rollDice()) }
        Dialog(
            onDismissRequest = { popUp1 = false }
        ) {

            AnimatedVisibility(
                visible = popUp1,
                enter = fadeIn(animationSpec = tween(2000)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(2000)),
                exit = fadeOut() + scaleOut()
            ) {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Text(
                            text = rolled.toString(),
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                popUp1 = false
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "OK",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (popUp2) {
        var flipped by remember { mutableStateOf(viewModel.flipCoin()) }
        Dialog(
            onDismissRequest = { popUp2 = false }
        ) {

            AnimatedVisibility(
                visible = popUp2,
                enter = fadeIn(animationSpec = tween(2000)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(2000)),
                exit = fadeOut() + scaleOut()
            ) {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = flipped,
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                popUp2 = false
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "OK",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (popUp3) {
        Dialog(
            onDismissRequest = { popUp3 = false }
        ) {

            AnimatedVisibility(
                visible = popUp3,
                enter = fadeIn(animationSpec = tween(2000)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(2000)),
                exit = fadeOut() + scaleOut()
            ) {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = "Jeste li sigurni da želite resetirati?",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                lineHeight = (screenWidth.value * 0.08f).sp
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    popUp3 = false
                                },
                                modifier = Modifier
                                    .width(screenWidth / 4)
                                    .height(screenWidth / 6),
                                colors = buttonColors
                            ) {
                                Text(
                                    text = "Ne",
                                    fontSize = (screenWidth.value * 0.06f).sp
                                )
                            }
                            Spacer(modifier = Modifier.width(screenWidth / 10))
                            Button(
                                onClick = {
                                    popUp3 = false
                                    viewModel.resetAll()
                                },
                                modifier = Modifier
                                    .width(screenWidth / 4)
                                    .height(screenWidth / 6),
                                colors = buttonColors
                            ) {
                                Text(
                                    text = "Da",
                                    fontSize = (screenWidth.value * 0.06f).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}