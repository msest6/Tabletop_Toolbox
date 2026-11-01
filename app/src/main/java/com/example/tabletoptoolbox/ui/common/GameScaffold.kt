package com.example.tabletoptoolbox.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Prikazuje gornju navigacijsku traku
 * @param title Ime igre
 * @param onArrowClick String za navController u slučaju pritiska strelice
 * @param settingsRoute Ruta postavki za ovu igru. Default je dijeljeni "settings" ekran;
 *                      igra može proslijediti vlastitu rutu kad zatreba.
 * @param infoText Tekst uputa za "i" pop-up. Ako je prazan, gumb "i" je onemogućen.
 * @param content Lambda funkcija koja poziva funkciju igre
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScaffold(
    navController: NavController,
    buttonColors: ButtonColors,
    title: String,
    onArrowClick: String,
    settingsRoute: String = "settings",
    infoText: String = "",
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = title, fontSize = (screenWidth.value * 0.06f).sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(onArrowClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showInfoDialog = true },
                        enabled = infoText.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Upute"
                        )
                    }
                    IconButton(onClick = { navController.navigate(settingsRoute) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Postavke"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            content()
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(text = "Info") },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    Text(
                        text = infoText,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("U redu")
                }
            }
        )
    }
}