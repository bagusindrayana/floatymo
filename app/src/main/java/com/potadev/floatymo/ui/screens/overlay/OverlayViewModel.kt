package com.potadev.floatymo.ui.screens.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OverlayUiState(
    val overlays: List<ActiveOverlay> = emptyList(),
    val gifs: List<SavedGif> = emptyList(),
    val selectedOverlayId: String? = null,
    val canAddMore: Boolean = true,
    val showGifPicker: Boolean = false,
    val showSettingsSheet: Boolean = false
)

class OverlayViewModel(
    private val gifRepository: GifRepository
) : ViewModel() {

    private val _selectedOverlayId = MutableStateFlow<String?>(null)
    private val _showGifPicker = MutableStateFlow(false)
    private val _showSettingsSheet = MutableStateFlow(false)

    val uiState: StateFlow<OverlayUiState> = combine(
        gifRepository.getActiveOverlays(),
        gifRepository.getAllGifs(),
        _selectedOverlayId,
        _showGifPicker,
        _showSettingsSheet
    ) { overlays, gifs, selectedId, showPicker, showSheet ->
        OverlayUiState(
            overlays = overlays,
            gifs = gifs,
            selectedOverlayId = selectedId,
            canAddMore = gifRepository.canAddMoreOverlays(),
            showGifPicker = showPicker,
            showSettingsSheet = showSheet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverlayUiState()
    )

    fun showGifPicker() {
        _showGifPicker.value = true
    }

    fun hideGifPicker() {
        _showGifPicker.value = false
    }

    fun showSettingsSheet(overlayId: String) {
        _selectedOverlayId.value = overlayId
        _showSettingsSheet.value = true
    }

    fun hideSettingsSheet() {
        _showSettingsSheet.value = false
        _selectedOverlayId.value = null
    }

    fun addOverlay(gifId: Long) {
        viewModelScope.launch {
            val overlay = ActiveOverlay(gifId = gifId)
            gifRepository.addActiveOverlay(overlay)
            _showGifPicker.value = false
        }
    }

    fun removeOverlay(id: String) {
        viewModelScope.launch {
            gifRepository.removeActiveOverlay(id)
            if (_selectedOverlayId.value == id) {
                _showSettingsSheet.value = false
                _selectedOverlayId.value = null
            }
        }
    }

    fun updateOverlay(id: String, size: Float? = null, opacity: Float? = null) {
        viewModelScope.launch {
            val current = gifRepository.getActiveOverlayById(id) ?: return@launch
            val updated = current.copy(
                size = size ?: current.size,
                opacity = opacity ?: current.opacity
            )
            gifRepository.updateActiveOverlay(updated)
        }
    }

    fun updateOverlayPosition(id: String, x: Int, y: Int) {
        viewModelScope.launch {
            val current = gifRepository.getActiveOverlayById(id) ?: return@launch
            gifRepository.updateActiveOverlay(current.copy(x = x, y = y))
        }
    }

    fun resetOverlayPosition(id: String) {
        viewModelScope.launch {
            val current = gifRepository.getActiveOverlayById(id) ?: return@launch
            gifRepository.updateActiveOverlay(current.copy(x = -1, y = -1))
        }
    }

    fun getGifForOverlay(gifId: Long): SavedGif? {
        return uiState.value.gifs.find { it.id == gifId }
    }

    class Factory(
        private val gifRepository: GifRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OverlayViewModel(gifRepository) as T
        }
    }
}
