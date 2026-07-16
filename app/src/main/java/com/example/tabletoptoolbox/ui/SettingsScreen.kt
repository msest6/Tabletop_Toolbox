package com.example.tabletoptoolbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import com.example.tabletoptoolbox.data.model.ThemeMode

/**
 * Prikazuje ekran za postavke
 * @param themeMode Trenutna postavljena tema
 * @param onThemeChange Lambda funkcija za spremanje promjene teme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    buttonColors: ButtonColors,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Postavke",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            textAlign = TextAlign.Center,
                            fontSize = (screenWidth.value * 0.08f).sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
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
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        text = "Tema: ${themeMode.name}",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = (screenWidth.value * 0.08f).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = themeMode == ThemeMode.DARK,
                        onCheckedChange = { isDark ->
                            onThemeChange(
                                if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                            )
                        },
                        modifier = Modifier.size(screenWidth / 4)
                    )
                }
            }
        }
    }
}