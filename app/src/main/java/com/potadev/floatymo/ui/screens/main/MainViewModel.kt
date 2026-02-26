package com.potadev.floatymo.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import com.potadev.floatymo.service.FloatingOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainUiState(
    val isOverlayRunning: Boolean = false,
    val activeGif: SavedGif? = null,
    val overlaySize: Float = 1.0f,
    val overlayOpacity: Float = 1.0f
)

class MainViewModel(
    private val gifRepository: GifRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isOverlayRunning = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> = combine(
        _isOverlayRunning,
        gifRepository.getActiveGif(),
        settingsRepository.getSettings()
    ) { isRunning, activeGif, settings ->
        MainUiState(
            isOverlayRunning = isRunning,
            activeGif = activeGif,
            overlaySize = settings.overlaySize,
            overlayOpacity = settings.overlayOpacity
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun updateOverlayStatus(isRunning: Boolean) {
        _isOverlayRunning.value = isRunning
    }

    class Factory(
        private val gifRepository: GifRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(gifRepository, settingsRepository) as T
        }
    }
}
