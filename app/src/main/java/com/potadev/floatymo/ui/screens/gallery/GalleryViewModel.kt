package com.potadev.floatymo.ui.screens.gallery

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.domain.model.GifSource
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class BundleGif(
    val id: String,
    val name: String,
    val resourceName: String
)

data class GalleryUiState(
    val bundleGifs: List<BundleGif> = emptyList(),
    val savedGifs: List<SavedGif> = emptyList(),
    val activeGifId: Long? = null
)

sealed class GalleryUiEvent {
    data class ShowSnackbar(val message: String) : GalleryUiEvent()
}

class GalleryViewModel(
    private val gifRepository: GifRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<GalleryUiEvent>()
    val uiEvent: SharedFlow<GalleryUiEvent> = _uiEvent.asSharedFlow()

    private val bundleGifs = listOf(
        // BundleGif("1", "Bleach Anime Lion Sticker", "bleach_anime_lion_sticker"),
        // BundleGif("2", "Chibi Cap Sticker", "chibi_cap_sticker"),
        // BundleGif("3", "Chibi Umamusume Sticker 1", "chibi_umamusume_sticker_1"),
        // BundleGif("4", "Chibi Umamusume Sticker 2", "chibi_umamusume_sticker_2"),
        // BundleGif("5", "Chibi Umamusume Sticker", "chibi_umamusume_sticker"),
        // BundleGif("6", "Dance Dancing Sticker", "dance_dancing_sticker"),
        // BundleGif("7", "Funny Dance Sticker", "funny_dance_sticker"),
        // BundleGif("8", "Umamusume GIF", "umamusume_gif"),
        // BundleGif("9", "Girl Horse Sticker", "girl_horse_sticker"),
        // BundleGif("10", "Happy Stepping Out", "happy_stepping_out_sticker_by_kennysgifs"),
        // BundleGif("11", "Mexico Pasa Sticker", "mexico_pasa_sticker"),
        // BundleGif("12", "Pet Sakura Sticker", "pet_sakura_sticker"),
        // BundleGif("13", "Pet Silence Sticker", "pet_silence_sticker"),
        // BundleGif("14", "Pet Week Sticker", "pet_week_sticker"),
        // BundleGif("15", "Red Button Girl Sticker", "red_button_girl_sticker"),
        BundleGif("16", "Lunar 1", "dailyfina_lunar_1"),
        BundleGif("17", "Lunar 2", "dailyfina_lunar_2"),
        BundleGif("18", "Lantern", "lantern"),
        BundleGif("19", "Sun", "sun"),
        BundleGif("20", "Party Disc", "party_disc"),
        BundleGif("21", "Queen Card", "queen"),
        BundleGif("22", "Flame/Fire", "flame")
    )

    val uiState: StateFlow<GalleryUiState> = combine(
        gifRepository.getAllGifs(),
        gifRepository.getActiveGif()
    ) { savedGifs, activeGif ->
        GalleryUiState(
            bundleGifs = bundleGifs,
            savedGifs = savedGifs,
            activeGifId = activeGif?.id
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GalleryUiState(bundleGifs = bundleGifs)
    )

    fun selectBundleGif(bundleGif: BundleGif) {
        viewModelScope.launch {
            val resourceId = context.resources.getIdentifier(
                bundleGif.resourceName,
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                val savedGif = SavedGif(
                    name = bundleGif.name,
                    source = GifSource.BUNDLE,
                    filePath = "android.resource://${context.packageName}/$resourceId",
                    isActive = true
                )
                val id = gifRepository.saveGif(savedGif)
                gifRepository.setActiveGif(id)
                _uiEvent.emit(GalleryUiEvent.ShowSnackbar("Added ${bundleGif.name} to My GIFs"))
            }
        }
    }

    fun selectSavedGif(gif: SavedGif) {
        viewModelScope.launch {
            gifRepository.setActiveGif(gif.id)
        }
    }

    fun deleteSavedGif(gif: SavedGif) {
        viewModelScope.launch {
            if (gif.source == GifSource.GIPHY || gif.source == GifSource.USER) {
                try {
                    val file = File(gif.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                    if (gif.thumbnailPath != null) {
                        val thumbFile = File(gif.thumbnailPath)
                        if (thumbFile.exists()) {
                            thumbFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            gifRepository.deleteGif(gif)
        }
    }

    fun importGifFromGallery(uri: Uri) {
        viewModelScope.launch {
            try {
                val maxSize = 3 * 1024 * 1024L // 3MB fixed

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch

                if (bytes.size > maxSize) {
                    _uiEvent.emit(GalleryUiEvent.ShowSnackbar("File too large (max 3MB)"))
                    return@launch
                }

                val fileName = "user_gif_${System.currentTimeMillis()}.gif"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { it.write(bytes) }

                val savedGif = SavedGif(
                    name = "Imported GIF",
                    source = GifSource.USER,
                    filePath = file.absolutePath,
                    isActive = true
                )
                val id = gifRepository.saveGif(savedGif)
                gifRepository.setActiveGif(id)
                _uiEvent.emit(GalleryUiEvent.ShowSnackbar("GIF imported successfully"))
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.emit(GalleryUiEvent.ShowSnackbar("Failed to import GIF"))
            }
        }
    }

    class Factory(
        private val gifRepository: GifRepository,
        private val settingsRepository: SettingsRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(gifRepository, settingsRepository, context) as T
        }
    }
}
