package com.example.tabletoptoolbox

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tabletoptoolbox.bela.Bela
import com.example.tabletoptoolbox.bela.BelaMenu
import com.example.tabletoptoolbox.bela.BelaNewGame
import com.example.tabletoptoolbox.bela.BelaNewRound
import com.example.tabletoptoolbox.bela.BelaViewModel
import com.example.tabletoptoolbox.bela.BelaViewModelFactory
import com.example.tabletoptoolbox.boardgames.BoardGameViewModel
import com.example.tabletoptoolbox.boardgames.BoardGameViewModelFactory
import com.example.tabletoptoolbox.boardgames.BoardGames
import com.example.tabletoptoolbox.darts.Darts
import com.example.tabletoptoolbox.darts.DartsMenu
import com.example.tabletoptoolbox.darts.DartsMovesList
import com.example.tabletoptoolbox.darts.DartsNewGame
import com.example.tabletoptoolbox.darts.DartsViewModel
import com.example.tabletoptoolbox.darts.DartsViewModelFactory
import com.example.tabletoptoolbox.data.BelaRepository
import com.example.tabletoptoolbox.data.BoardGameRepository
import com.example.tabletoptoolbox.data.DartsRepository
import com.example.tabletoptoolbox.data.GradDrzavaRepository
import com.example.tabletoptoolbox.data.PreferencesDataStore
import com.example.tabletoptoolbox.data.ThemeRepository
import com.example.tabletoptoolbox.data.UnoRepository
import com.example.tabletoptoolbox.data.model.CheckoutTable
import com.example.tabletoptoolbox.data.model.ThemeMode
import com.example.tabletoptoolbox.graddrzava.GradDrzava
import com.example.tabletoptoolbox.graddrzava.GradDrzavaMenu
import com.example.tabletoptoolbox.graddrzava.GradDrzavaNewGame
import com.example.tabletoptoolbox.graddrzava.GradDrzavaViewModel
import com.example.tabletoptoolbox.graddrzava.GradDrzavaViewModelFactory
import com.example.tabletoptoolbox.ui.HomeScreen
import com.example.tabletoptoolbox.ui.SettingsScreen
import com.example.tabletoptoolbox.ui.common.GameScaffold
import com.example.tabletoptoolbox.ui.theme.GameHubTheme
import com.example.tabletoptoolbox.uno.Uno
import com.example.tabletoptoolbox.uno.UnoMenu
import com.example.tabletoptoolbox.uno.UnoNewGame
import com.example.tabletoptoolbox.uno.UnoNewRound
import com.example.tabletoptoolbox.uno.UnoRoundsList
import com.example.tabletoptoolbox.uno.UnoViewModel
import com.example.tabletoptoolbox.uno.UnoViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Repozitoriji - jedina mjesta koja znaju za DataStore
            val prefs = remember { PreferencesDataStore(context) }
            val themeRepository = remember { ThemeRepository(prefs) }
            val dartsRepository = remember { DartsRepository(prefs) }
            val boardGameRepository = remember { BoardGameRepository(prefs) }
            val gradDrzavaRepository = remember { GradDrzavaRepository(prefs) }
            val belaRepository = remember { BelaRepository(prefs) }
            val unoRepository = remember { UnoRepository(prefs) }
            val inputStream = resources.openRawResource(R.raw.checkout_table)
            val json = inputStream.bufferedReader().use { it.readText() }
            val checkoutTable = remember { Json.decodeFromString<List<CheckoutTable>>(json) }
            val dartsViewModel: DartsViewModel = viewModel(
                factory = DartsViewModelFactory(dartsRepository, checkoutTable)
            )
            val gradDrzavaViewModel: GradDrzavaViewModel = viewModel(
                factory = GradDrzavaViewModelFactory(gradDrzavaRepository)
            )
            val belaViewModel: BelaViewModel = viewModel(
                factory = BelaViewModelFactory(belaRepository)
            )
            val unoViewModel: UnoViewModel = viewModel(
                factory = UnoViewModelFactory(unoRepository)
            )

            var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }

            LaunchedEffect(Unit) {
                themeMode = themeRepository.getThemeMode()

                gradDrzavaViewModel.loadIfNeeded()
                dartsViewModel.loadIfNeeded()
                belaViewModel.loadIfNeeded()
                unoViewModel.loadIfNeeded()
            }

            GameHubTheme(themeMode) {
                val navController = rememberNavController()
                val buttonColors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary,
                    disabledContentColor = MaterialTheme.colorScheme.tertiary
                )
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(navController, buttonColors)
                    }

                    composable("settings") {
                        SettingsScreen(
                            navController,
                            buttonColors,
                            themeMode = themeMode,
                            onThemeChange = { newTheme ->
                                themeMode = newTheme
                                scope.launch {
                                    themeRepository.setThemeMode(newTheme)
                                }
                            })
                    }

                    composable("dartsMenu") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado",
                            onArrowClick = "home",
                            content = { DartsMenu(navController, buttonColors) }
                        )
                    }

                    composable("dartsNewGame") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado",
                            onArrowClick = "dartsMenu",
                            content = {
                                DartsNewGame(navController, buttonColors, dartsViewModel)
                            }
                        )
                    }

                    composable("darts") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado: ${dartsViewModel.targetScore}",
                            onArrowClick = "dartsMenu",
                            content = {
                                Darts(navController, buttonColors, loadGame = true, viewModel = dartsViewModel)
                            }
                        )
                    }
                    composable("dartsNew") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado: ${dartsViewModel.targetScore}",
                            onArrowClick = "dartsMenu",
                            content = {
                                Darts(navController, buttonColors, loadGame = false, viewModel = dartsViewModel)
                            }
                        )
                    }
                    composable("dartsGameDisplay") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado Potezi",
                            onArrowClick = "darts",
                            content = {
                                DartsMovesList(navController, buttonColors, dartsViewModel)
                            }
                        )
                    }
                    composable("boardGames") {
                        val boardGameViewModel: BoardGameViewModel = viewModel(
                            factory = BoardGameViewModelFactory(boardGameRepository)
                        )
                        GameScaffold(
                            navController = navController,
                            buttonColors = buttonColors,
                            title = "Igre na Ploči",
                            onArrowClick = "home",
                            content = {
                                BoardGames(navController, buttonColors, boardGameViewModel)
                            }
                        )
                    }
                    composable("gradDrzavaMenu") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Država Grad",
                            onArrowClick = "home",
                            content = { GradDrzavaMenu(navController, buttonColors) }
                        )
                    }
                    composable("gradDrzavaNewGame") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Država Grad",
                            onArrowClick = "gradDrzavaMenu",
                            content = {
                                GradDrzavaNewGame(navController, buttonColors, gradDrzavaViewModel)
                            }
                        )
                    }
                    composable("gradDrzava") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Grad Država",
                            onArrowClick = "gradDrzavaMenu",
                            content = {
                                GradDrzava(navController, buttonColors, gradDrzavaViewModel)
                            }
                        )
                    }
                    composable("belaMenu") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Bela",
                            onArrowClick = "home",
                            content = { BelaMenu(navController, buttonColors) }
                        )
                    }
                    composable("belaNewGame") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Bela",
                            onArrowClick = "belaMenu",
                            content = {
                                BelaNewGame(navController, buttonColors, belaViewModel)
                            }
                        )
                    }
                    composable("bela") {
                        Bela(navController, buttonColors, belaViewModel)
                    }
                    composable("belaNovaRunda") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Bela",
                            onArrowClick = "bela",
                            content = {
                                BelaNewRound(navController, buttonColors, belaViewModel)
                            }
                        )
                    }
                    composable("uno"){
                        Uno(navController, buttonColors, unoViewModel)
                    }
                    composable("unoMenu"){
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Uno",
                            onArrowClick = "home",
                            content = { UnoMenu(navController, buttonColors) }
                        )
                    }
                    composable("unoNewGame"){
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Uno",
                            onArrowClick = "unoMenu",
                            content = { UnoNewGame(navController, buttonColors, unoViewModel) }
                        )
                    }
                    composable("unoNewRound"){
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Uno",
                            onArrowClick = "uno",
                            content = { UnoNewRound(navController, buttonColors, unoViewModel) }
                        )
                    }
                    composable("unoRoundsList"){
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Uno",
                            onArrowClick = "uno",
                            content = { UnoRoundsList(navController, buttonColors, unoViewModel) }
                        )
                    }
                }
            }
        }
    }
}