package com.example.tabletoptoolbox.tabletier

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun EloScreen(navController: NavController, buttonColors: ButtonColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Procjena omiljenih igara",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ovo je kratka anketa u kojoj biraš koja ti se igra više sviđa " +
                    "između dvije ponuđene. Na temelju tvojih odabira igara " +
                    "izračunava preciznije rangiranje igara - što više odgovaraš, " +
                    "to je poredak točniji.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedButton(
            colors = buttonColors,
            onClick = { navController.navigate("eloQuiz") }
        ) {
            Text("Nastavi")
        }
    }
}