package com.example.tabletoptoolbox.bela

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BelaMenu(navController: NavController, buttonColors: ButtonColors) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { navController.navigate("belaNewGame") },
            modifier = Modifier
                .padding(8.dp)
                .width(screenWidth / 1.1f),
            colors = buttonColors
        ) {
            Text(
                text = "Nova Igra",
                fontSize = (screenWidth.value * 0.08f).sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        Button(
            onClick = { navController.navigate("bela") },
            modifier = Modifier
                .padding(8.dp)
                .width(screenWidth / 1.1f),
            colors = buttonColors
        ) {
            Text(
                text = "Nastavi Igru",
                fontSize = (screenWidth.value * 0.08f).sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}