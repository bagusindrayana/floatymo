package com.potadev.floatymo.ui.screens.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.potadev.floatymo.BuildConfig
import com.potadev.floatymo.data.remote.GiphyApi
import com.potadev.floatymo.data.remote.GiphyGif
import com.potadev.floatymo.domain.model.GifSource
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

data class SearchUiState(
    val query: String = "",
    val results: List<GiphyGif> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val offset: Int = 0
)

class SearchViewModel(
    private val giphyApi: GiphyApi,
    private val gifRepository: GifRepository,
    private val context: Context
) : ViewModel() {

    companion object {
        private val GIPHY_API_KEY = BuildConfig.GIPHY_API_KEY
        private const val PAGE_SIZE = 25
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            if (query.isNotBlank()) {
                search(query, reset = true)
            }
        }
    }

    fun search(query: String, reset: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.isLoading) return@launch

            _uiState.value = currentState.copy(
                isLoading = true,
                error = null,
                offset = if (reset) 0 else currentState.offset
            )

            try {
                val response = giphyApi.searchGifs(
                    apiKey = GIPHY_API_KEY,
                    query = query,
                    limit = PAGE_SIZE,
                    offset = if (reset) 0 else currentState.offset
                )

                val newResults = if (reset) {
                    response.data
                } else {
                    currentState.results + response.data
                }

                _uiState.value = _uiState.value.copy(
                    results = newResults,
                    isLoading = false,
                    hasMore = response.data.size >= PAGE_SIZE,
                    offset = currentState.offset + response.data.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to search"
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoading && state.hasMore && state.query.isNotBlank()) {
            search(state.query, reset = false)
        }
    }

    fun downloadAndSelect(gif: GiphyGif) {
        viewModelScope.launch {
            try {
                val gifUrl = gif.images.fixedWidth?.url ?: gif.images.original?.url
                    ?: return@launch

                val fileName = "giphy_${gif.id}_${System.currentTimeMillis()}.gif"
                val file = File(context.filesDir, fileName)

                scope.launch {
                    try {
                        URL(gifUrl).openStream().use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }

                        val savedGif = SavedGif(
                            name = gif.title.ifBlank { "GIPHY GIF" },
                            source = GifSource.GIPHY,
                            filePath = file.absolutePath,
                            isActive = true
                        )

                        val id = gifRepository.saveGif(savedGif)
                        gifRepository.setActiveGif(id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to download: ${e.message}"
                )
            }
        }
    }

    class Factory(
        private val giphyApi: GiphyApi,
        private val gifRepository: GifRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(giphyApi, gifRepository, context) as T
        }
    }
}
