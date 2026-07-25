package com.example.tabletoptoolbox.tabletier

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tabletoptoolbox.data.boardGames.DatabaseTransfer
import com.example.tabletoptoolbox.data.boardGames.GameRepository
import com.example.tabletoptoolbox.data.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Postavke specifične za Table Tier: tema (Light/Dark) + izvoz/uvoz baze igara.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableTierSettingsScreen(
    navController: NavController,
    buttonColors: ButtonColors,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    repository: GameRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var isBusy by remember { mutableStateOf(false) }

    // --- UVOZ: file picker -> importFromUri ---
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            when (val result = DatabaseTransfer.importFromUri(context, uri, repository)) {
                is DatabaseTransfer.ImportResult.Success ->
                    Toast.makeText(
                        context,
                        "Uvezeno igara: ${result.importedCount}",
                        Toast.LENGTH_LONG
                    ).show()

                is DatabaseTransfer.ImportResult.Error ->
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            isBusy = false
        }
    }

    // --- IZVOZ: exportToCache -> ACTION_SEND ---
    fun onExport() {
        scope.launch {
            val games = repository.getAllGames().first()
            if (games.isEmpty()) {
                Toast.makeText(context, "Nema igara za izvoz.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            isBusy = true
            try {
                val uri = DatabaseTransfer.exportToCache(context, games)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Podijeli kolekciju"))
            } catch (e: Exception) {
                Toast.makeText(context, "Izvoz nije uspio: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isBusy = false
            }
        }
    }

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            // --- Tema ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                        onThemeChange(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                    },
                    modifier = Modifier.size(screenWidth / 4)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Baza igara",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                colors = buttonColors,
                enabled = !isBusy,
                onClick = { onExport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Izvezi bazu")
            }

            Spacer(modifier = Modifier.height(12.dp))

            ElevatedButton(
                colors = buttonColors,
                enabled = !isBusy,
                onClick = {
                    importLauncher.launch(
                        arrayOf("application/octet-stream", "application/zip", "*/*")
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = buttonColors.contentColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uvezi bazu")
                }
            }
        }
    }
}