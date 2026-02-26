package com.potadev.floatymo.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.domain.model.AppSettings
import com.potadev.floatymo.domain.model.OverlaySize
import com.potadev.floatymo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings()
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.getSettings()
        .map { SettingsUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun updateSize(size: OverlaySize) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(overlaySize = size.scale))
        }
    }

    fun updateOpacity(opacity: Float) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(overlayOpacity = opacity))
        }
    }

    fun updateOpacityRealTime(opacity: Float) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(overlayOpacity = opacity))
        }
    }

    fun resetPosition() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(overlayX = -1, overlayY = -1))
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository) as T
        }
    }
}
