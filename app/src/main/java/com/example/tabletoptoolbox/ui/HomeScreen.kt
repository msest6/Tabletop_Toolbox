package com.example.tabletoptoolbox.ui

import androidx.activity.SystemBarStyle
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
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
        MenuItem("dartsMenu", "Pikado kalkulator"),
        MenuItem("wheelOfNames", "Wheel Of Names"),
        MenuItem("gradDrzavaMenu", "Država Grad"),
        MenuItem("boardGames", "Društvene Igre"),
        MenuItem("tableTier", "Table Tier")
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = buttonColors.contentColor)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tabletop Toolbox",
                        fontSize = (screenWidth.value * 0.1f).sp,
                        color = buttonColors.containerColor
                    )
                }
            }
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
}