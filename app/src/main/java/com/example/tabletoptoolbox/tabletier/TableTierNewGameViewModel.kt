package com.example.tabletoptoolbox.tabletier

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabletoptoolbox.data.boardGames.GameRepository
import com.example.tabletoptoolbox.data.boardGames.ImageStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewGameFormState(
    val editingGameId: Long? = null,
    val title: String = "",
    val ratingText: String = "",
    val description: String = "",
    val tempImagePath: String? = null,
    val existingImagePath: String? = null,
    val playTimeText: String = "",
    val minPlayersText: String = "",
    val maxPlayersText: String = "", // prazno = "ništa", "+" ili broj
    val titleError: String? = null,
    val ratingError: String? = null,
    val playTimeError: String? = null,
    val minPlayersError: String? = null,
    val maxPlayersError: String? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false
) {
    val isEditMode: Boolean get() = editingGameId != null
}

class TableTierNewGameViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(NewGameFormState())
    val formState: StateFlow<NewGameFormState> = _formState.asStateFlow()

    private val _navigationEvent = Channel<Long>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _finishEvent = Channel<Unit>(Channel.BUFFERED)
    val finishEvent = _finishEvent.receiveAsFlow()

    private var gameSaved = false

    fun onTitleChanged(value: String) {
        if (value.length <= 30) {
            _formState.update { it.copy(title = value, titleError = null) }
        }
    }

    fun onRatingChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,2}(\\.\\d{0,1})?$"))) {
            _formState.update { it.copy(ratingText = value, ratingError = null) }
        }
    }

    fun onDescriptionChanged(value: String) {
        if (value.length <= 1000) {
            _formState.update { it.copy(description = value) }
        }
    }

    /** Vrijeme igranja u minutama - samo znamenke, do 3 znamenke (max 999 min). */
    fun onPlayTimeChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,3}$"))) {
            _formState.update { it.copy(playTimeText = value, playTimeError = null) }
        }
    }

    /** "Od" - samo znamenke, do 2 znamenke. */
    fun onMinPlayersChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,2}$"))) {
            _formState.update { it.copy(minPlayersText = value, minPlayersError = null) }
        }
    }

    /** "Do" - prazno, znamenke (do 2), ili "+". */
    fun onMaxPlayersChanged(value: String) {
        if (value.isEmpty() || value == "+" || value.matches(Regex("^\\d{0,2}$"))) {
            _formState.update { it.copy(maxPlayersText = value, maxPlayersError = null) }
        }
    }

    fun onImageSelected(tempPath: String) {
        _formState.value.tempImagePath?.let { ImageStorage.deleteImage(it) }
        _formState.update { it.copy(tempImagePath = tempPath) }
    }

    fun loadGameForEditing(gameId: Long) {
        viewModelScope.launch {
            val game = repository.getGameById(gameId) ?: return@launch
            _formState.update {
                it.copy(
                    editingGameId = game.id,
                    title = game.name,
                    ratingText = formatRating(game.userRating),
                    description = game.description,
                    existingImagePath = game.imagePath,
                    playTimeText = game.playTimeMinutes.toString(),
                    minPlayersText = game.minPlayers.toString(),
                    maxPlayersText = game.maxPlayers ?: ""
                )
            }
        }
    }

    private fun formatRating(rating: Float): String =
        if (rating == rating.toInt().toFloat()) rating.toInt().toString() else rating.toString()

    fun saveGame(context: Context) {
        val state = _formState.value

        val trimmedTitle = state.title.trim()
        val ratingValue = state.ratingText.toFloatOrNull()
        val playTimeValue = state.playTimeText.toIntOrNull()
        val minPlayersValue = state.minPlayersText.toIntOrNull()
        val maxPlayersRaw = state.maxPlayersText.trim()

        var titleError: String? = null
        var ratingError: String? = null
        var playTimeError: String? = null
        var minPlayersError: String? = null
        var maxPlayersError: String? = null

        if (trimmedTitle.isEmpty()) {
            titleError = "Naziv igre je obavezan"
        }
        if (ratingValue == null) {
            ratingError = "Ocjena je obavezna"
        } else if (ratingValue < 0f || ratingValue > 10f) {
            ratingError = "Ocjena mora biti između 0 i 10"
        }
        if (playTimeValue == null || playTimeValue <= 0) {
            playTimeError = "Vrijeme igranja je obavezno"
        }
        if (minPlayersValue == null || minPlayersValue <= 0) {
            minPlayersError = "Broj igrača (od) je obavezan"
        }
        // maxPlayers smije biti prazan; ako je broj, mora biti >= minPlayers
        if (maxPlayersRaw.isNotEmpty() && maxPlayersRaw != "+") {
            val maxValue = maxPlayersRaw.toIntOrNull()
            if (maxValue == null) {
                maxPlayersError = "Neispravan unos"
            } else if (minPlayersValue != null && maxValue < minPlayersValue) {
                maxPlayersError = "\"Do\" mora biti veće ili jednako \"od\""
            }
        }

        if (titleError != null || ratingError != null || playTimeError != null ||
            minPlayersError != null || maxPlayersError != null
        ) {
            _formState.update {
                it.copy(
                    titleError = titleError,
                    ratingError = ratingError,
                    playTimeError = playTimeError,
                    minPlayersError = minPlayersError,
                    maxPlayersError = maxPlayersError
                )
            }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val finalImagePath = state.tempImagePath?.let { tempPath ->
                ImageStorage.finalizeImage(context, tempPath)
            } ?: state.existingImagePath

            val maxPlayersToSave: String? = if (maxPlayersRaw.isEmpty()) null else maxPlayersRaw

            val editingId = state.editingGameId
            if (editingId == null) {
                val newId = repository.addGame(
                    name = trimmedTitle,
                    description = state.description,
                    imagePath = finalImagePath,
                    userRating = ratingValue!!,
                    playTimeMinutes = playTimeValue!!,
                    minPlayers = minPlayersValue!!,
                    maxPlayers = maxPlayersToSave
                )
                gameSaved = true
                _formState.update { it.copy(isSaving = false) }
                _navigationEvent.send(newId)
            } else {
                if (state.tempImagePath != null && state.existingImagePath != null &&
                    state.existingImagePath != finalImagePath
                ) {
                    ImageStorage.deleteImage(state.existingImagePath)
                }
                repository.getGameById(editingId)?.let { existing ->
                    repository.updateGame(
                        existing.copy(
                            name = trimmedTitle,
                            description = state.description,
                            imagePath = finalImagePath,
                            userRating = ratingValue!!,
                            playTimeMinutes = playTimeValue!!,
                            minPlayers = minPlayersValue!!,
                            maxPlayers = maxPlayersToSave
                        )
                    )
                }
                gameSaved = true
                _formState.update { it.copy(isSaving = false) }
                _finishEvent.send(Unit)
            }
        }
    }

    fun deleteGame() {
        val state = _formState.value
        val editingId = state.editingGameId ?: return

        _formState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            repository.getGameById(editingId)?.let { existing ->
                repository.deleteGame(existing)
                existing.imagePath?.let { ImageStorage.deleteImage(it) }
            }
            state.tempImagePath?.let { ImageStorage.deleteImage(it) }
            gameSaved = true
            _formState.update { it.copy(isDeleting = false) }
            _finishEvent.send(Unit)
        }
    }

    fun cleanupUnsavedImage() {
        if (!gameSaved) {
            _formState.value.tempImagePath?.let { ImageStorage.deleteImage(it) }
        }
    }

    fun resetForm() {
        _formState.value = NewGameFormState()
        gameSaved = false
    }
}