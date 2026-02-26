package com.potadev.floatymo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val gifs: List<SavedGif> = emptyList(),
    val selectedGifIds: Set<Long> = emptySet(),
    val isServiceRunning: Boolean = false,
    val canAddMore: Boolean = true
)

class HomeViewModel(
    private val gifRepository: GifRepository
) : ViewModel() {

    private val _selectedGifIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isServiceRunning = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        gifRepository.getAllGifs(),
        gifRepository.getActiveOverlays(),
        _selectedGifIds,
        _isServiceRunning
    ) { gifs, overlays, selectedIds, isRunning ->
        val activeGifIds = overlays.map { it.gifId }.toSet()
        HomeUiState(
            gifs = gifs,
            selectedGifIds = activeGifIds,
            isServiceRunning = isRunning,
            canAddMore = gifRepository.canAddMoreOverlays()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleGifSelection(gifId: Long) {
        viewModelScope.launch {
            val currentOverlays = gifRepository.getActiveOverlaysList()
            val existing = currentOverlays.find { it.gifId == gifId }

            if (existing != null) {
                gifRepository.removeActiveOverlay(existing.id)
            } else if (currentOverlays.size < 5) {
                gifRepository.addActiveOverlay(
                    com.potadev.floatymo.domain.model.ActiveOverlay(gifId = gifId)
                )
            }
        }
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    class Factory(
        private val gifRepository: GifRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(gifRepository) as T
        }
    }
}
