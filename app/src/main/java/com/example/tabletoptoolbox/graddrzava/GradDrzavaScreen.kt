package com.example.tabletoptoolbox.graddrzava

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradDrzava(
    navController: NavController,
    buttonColors: ButtonColors,
    viewModel: GradDrzavaViewModel
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    // Tekstualno polje za slovo je cisto UI stanje dok se tipka -
    // potvrduje se tek na blur, preko viewModel.trySetCurrentLetter().
    var letterField by remember(viewModel.isLoaded) {
        mutableStateOf(TextFieldValue(viewModel.currentLetter))
    }
    var showError1 by remember { mutableStateOf(false) }
    var popUp1 by remember { mutableStateOf(false) }
    var popUp2 by remember { mutableStateOf(false) }
    var popUp3 by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isLetterFieldFocused by remember { mutableStateOf(false) }

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
                    value = letterField,
                    onValueChange = { newText ->
                        letterField = newText
                    },
                    label = { Text("Trenutno slovo:") },
                    singleLine = true,
                    modifier = Modifier
                        .width(screenWidth / 4f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isLetterFieldFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                letterField = TextFieldValue(letterField.text.uppercase())
                                if (!viewModel.trySetCurrentLetter(letterField.text)) {
                                    letterField = TextFieldValue(viewModel.currentLetter)
                                    showError1 = true
                                } else {
                                    showError1 = false
                                }
                            }
                        }
                )
                LaunchedEffect(isLetterFieldFocused) {
                    if (isLetterFieldFocused) {
                        withFrameNanos { } // pricekaj da Compose odradi svoj default cursor-placement
                        letterField = letterField.copy(selection = TextRange(0, letterField.text.length))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .width(screenWidth / 4)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                popUp3 = true
                            }
                        }
                ) {
                    Text(
                        text = "Bodovi:",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = (-10).dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 2.dp)
                    )

                    Text(
                        text = viewModel.score.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        popUp1 = true
                    },
                    modifier = Modifier.width(screenWidth / 6).height(screenWidth / 6),
                    shape = CircleShape,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text(
                        text = "A",
                        fontSize = (screenWidth.value * 0.08f).sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Unesite svoje odgovore",
                    fontSize = (screenWidth.value * 0.05f).sp,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(
                    colors = buttonColors,
                    onClick = {
                        popUp2 = true
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
                            text = "⚠️ Unos u polje za slovo mora biti jedno slovo abecede!",
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
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.categories.size, key = { index -> index }) { index ->
                    var expanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedTextField(
                            value = viewModel.roundAnswers.getOrElse(index) { "" },
                            onValueChange = { newText: String ->
                                viewModel.updateAnswer(index, newText)
                            },
                            label = { Text(viewModel.categories[index]) },
                            singleLine = true,
                            modifier = Modifier.width(screenWidth / 2)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = viewModel.roundScores.getOrElse(index) { 0 }.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Bodovi:") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(screenWidth / 3f)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                GRAD_DRZAVA_POSSIBLE_SCORES.forEach { possibleScore ->
                                    DropdownMenuItem(
                                        text = { Text(possibleScore.toString()) },
                                        onClick = {
                                            viewModel.setCategoryScore(index, possibleScore)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        colors = buttonColors,
                        onClick = { viewModel.submitRound() }
                    ){
                        Text(
                            text = "Spremi",
                            fontSize = (screenWidth.value * 0.05f).sp
                        )
                    }
                }
            }
        }
    }
    if (popUp1) {
        val randomLetter = remember(popUp1) { viewModel.pickRandomLetter() }
        Dialog(
            onDismissRequest = { }
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
                            text = randomLetter,
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.confirmLetter(randomLetter)
                                letterField = TextFieldValue(randomLetter)
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
                                    popUp2 = false
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
                                    popUp2 = false
                                    viewModel.resetToLetterA()
                                    letterField = TextFieldValue("A")
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
                            text = "Jeste li sigurni da želite resetirati svoje bodove?",
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
                                    viewModel.resetScore()
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