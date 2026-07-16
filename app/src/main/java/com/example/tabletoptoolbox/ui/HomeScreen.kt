package com.example.tabletoptoolbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.tabletoptoolbox.data.model.MenuItem

/**
 * Prikazuje početni ekran za biranje igre
 */
@Composable
fun HomeScreen(navController: NavController, buttonColors: ButtonColors) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val items = listOf(
        MenuItem("belaMenu", "Bela kalkulator"),
        MenuItem("unoMenu", "Uno Kalkulator"),
        MenuItem("boardGames", "Društvene Igre"),
        MenuItem("dartsMenu", "Pikado kalkulator"),
        MenuItem("gradDrzavaMenu", "Država Grad"),
    )

    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { menuItem ->
            Button(
                onClick = { navController.navigate(menuItem.route) },
                modifier = Modifier
                    .padding(16.dp)
                    .width(screenWidth / 1.1f)
                    .height(screenWidth / 4),
                colors = buttonColors
            ) {
                Text(
                    text = menuItem.label,
                    fontSize = (screenWidth.value * 0.08f).sp
                )
            }
        }
    }
}