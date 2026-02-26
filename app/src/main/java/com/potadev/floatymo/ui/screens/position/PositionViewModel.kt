package com.potadev.floatymo.ui.screens.position

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.domain.model.AppSettings
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PositionUiState(
    val settings: AppSettings = AppSettings(),
    val gifPath: String? = null,
    val size: Float = 1f,
    val isEditMode: Boolean = false
)

class PositionViewModel(
    private val settingsRepository: SettingsRepository,
    private val gifRepository: GifRepository
) : ViewModel() {

    private var originalOpacity: Float = 1.0f

    val settings: StateFlow<PositionUiState> = combine(
        settingsRepository.getSettings(),
        gifRepository.getActiveGif()
    ) { settings, gif ->
        PositionUiState(
            settings = settings,
            gifPath = gif?.filePath,
            size = settings.overlaySize
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PositionUiState()
    )

    fun enterEditMode() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            originalOpacity = current.overlayOpacity
            // Hide overlay and set to 50% opacity for preview
            settingsRepository.updateSettings(
                current.copy(
                    isEditingPosition = true,
                    overlayOpacity = 0.5f
                )
            )
        }
    }

    fun exitEditMode() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            // Show overlay and restore opacity
            settingsRepository.updateSettings(
                current.copy(
                    isEditingPosition = false,
                    overlayOpacity = originalOpacity
                )
            )
        }
    }

    fun resetPosition() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(overlayX = -1, overlayY = -1))
        }
    }

    fun savePosition(x: Int, y: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(overlayX = x, overlayY = y))
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val gifRepository: GifRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PositionViewModel(settingsRepository, gifRepository) as T
        }
    }
}
