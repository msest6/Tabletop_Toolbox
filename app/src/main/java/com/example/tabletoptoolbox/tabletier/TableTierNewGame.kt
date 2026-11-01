package com.example.tabletoptoolbox.tabletier

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tabletoptoolbox.data.boardGames.ImageStorage
import java.io.File

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TableTierNewGame(
    navController: NavController,
    buttonColors: ButtonColors,
    viewModel: TableTierNewGameViewModel,
    gameId: Long? = null // proslijeđen kad se otvara za UREĐIVANJE, null za novu igru
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.resetForm()
        gameId?.let { viewModel.loadGameForEditing(it) }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cleanupUnsavedImage() }
    }

    // Nova igra spremljena -> idi na elo quiz
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { id ->
            navController.navigate("eloQuiz?gameId=$id") {
                popUpTo("tableTierNewGame") { inclusive = true }
            }
        }
    }

    // Uređivanje spremljeno ili igra izbrisana -> vrati se natrag
    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect {
            navController.popBackStack()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val tempPath = ImageStorage.compressToTempFile(context, it)
            viewModel.onImageSelected(tempPath)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let {
                val tempPath = ImageStorage.compressToTempFile(context, it)
                viewModel.onImageSelected(tempPath)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ImageStorage.createCameraCaptureUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = ImageStorage.createCameraCaptureUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullHeight = maxHeight           // puna visina, bez tipkovnice

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()                          // viewport završava iznad tipkovnice
                .verticalScroll(rememberScrollState()) // sadržaj se skrola
                .heightIn(min = fullHeight)            // sadržaj je uvijek visok kao cijeli ekran
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagePickerButton(
                    imagePath = formState.tempImagePath ?: formState.existingImagePath,
                    onClick = { showImageSourceDialog = true },
                    modifier = Modifier.size(110.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = formState.title,
                        onValueChange = viewModel::onTitleChanged,
                        label = { Text("Naziv igre") },
                        singleLine = true,
                        isError = formState.titleError != null,
                        supportingText = {
                            Text(text = formState.titleError ?: "${formState.title.length}/30")
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = formState.ratingText,
                        onValueChange = viewModel::onRatingChanged,
                        label = { Text("Ocjena (0-10)") },
                        singleLine = true,
                        isError = formState.ratingError != null,
                        supportingText = formState.ratingError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.playTimeText,
                    onValueChange = viewModel::onPlayTimeChanged,
                    label = { Text("Trajanje (min)") },
                    singleLine = true,
                    isError = formState.playTimeError != null,
                    supportingText = formState.playTimeError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = formState.minPlayersText,
                    onValueChange = viewModel::onMinPlayersChanged,
                    label = { Text("Igrača od") },
                    singleLine = true,
                    isError = formState.minPlayersError != null,
                    supportingText = formState.minPlayersError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = formState.maxPlayersText,
                    onValueChange = viewModel::onMaxPlayersChanged,
                    label = { Text("do") },
                    placeholder = { Text("br. ili +") },
                    singleLine = true,
                    isError = formState.maxPlayersError != null,
                    supportingText = formState.maxPlayersError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Opis igre",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChanged,
                placeholder = { Text("Opiši pravila, dojmove, sve što ti je bitno...") },
                supportingText = { Text("${formState.description.length}/1000") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (formState.isEditMode) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = !formState.isSaving && !formState.isDeleting,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (formState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Izbriši")
                        }
                    }
                }

                ElevatedButton(
                    colors = buttonColors,
                    onClick = { viewModel.saveGame(context) },
                    enabled = !formState.isSaving && !formState.isDeleting,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    if (formState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = buttonColors.contentColor
                        )
                    } else {
                        Text("Spremi")
                    }
                }
            }
        }
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraSelected = {
                showImageSourceDialog = false
                launchCamera() },
            onGallerySelected = {
                showImageSourceDialog = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Izbriši igru?") },
            text = { Text("Ova radnja je trajna i ne može se poništiti.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    viewModel.deleteGame()
                }) {
                    Text("Izbriši", color = MaterialTheme.colorScheme.error)
                } },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
}

@Composable
private fun ImagePickerButton(
    imagePath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Odabrana slika igre",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Dodaj sliku",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Slika", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Odaberi izvor slike") },
        text = { Text("Slikaj novu fotografiju ili odaberi postojeću iz galerije.") },
        confirmButton = {
            TextButton(onClick = onCameraSelected) { Text("Fotoaparat") }
        },
        dismissButton = {
            TextButton(onClick = onGallerySelected) { Text("Galerija") }
        }
    )
}