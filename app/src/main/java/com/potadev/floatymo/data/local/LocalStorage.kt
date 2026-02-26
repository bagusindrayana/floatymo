package com.potadev.floatymo.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.AppSettings
import com.potadev.floatymo.domain.model.SavedGif
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("floatymo_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _gifsFlow = MutableStateFlow<List<SavedGif>>(getAllGifsFromPrefs())
    private val _settingsFlow = MutableStateFlow(getSettingsFromPrefs())
    private val _activeOverlaysFlow = MutableStateFlow<List<ActiveOverlay>>(getActiveOverlaysFromPrefs())

    companion object {
        private const val KEY_GIFS = "saved_gifs"
        private const val KEY_SETTINGS = "app_settings"
        private const val KEY_ACTIVE_OVERLAYS = "active_overlays"
        private const val MAX_OVERLAYS = 5
    }

    // GIF Operations
    fun getAllGifs(): Flow<List<SavedGif>> = _gifsFlow

    fun getAllGifsList(): List<SavedGif> = _gifsFlow.value

    fun getActiveGif(): SavedGif? {
        return _gifsFlow.value.find { it.isActive }
    }

    fun getGifById(id: Long): SavedGif? {
        return _gifsFlow.value.find { it.id == id }
    }

    fun saveGif(gif: SavedGif): Long {
        val currentGifs = _gifsFlow.value.toMutableList()
        val newId = if (gif.id == 0L) (currentGifs.maxOfOrNull { it.id } ?: 0) + 1 else gif.id
        val newGif = gif.copy(id = newId)
        
        val existingIndex = currentGifs.indexOfFirst { it.id == newId }
        if (existingIndex >= 0) {
            currentGifs[existingIndex] = newGif
        } else {
            currentGifs.add(newGif)
        }
        
        saveGifsToPrefs(currentGifs)
        _gifsFlow.value = currentGifs
        return newId
    }

    fun deleteGif(gif: SavedGif) {
        val currentGifs = _gifsFlow.value.filter { it.id != gif.id }
        saveGifsToPrefs(currentGifs)
        _gifsFlow.value = currentGifs
        _activeOverlaysFlow.value = _activeOverlaysFlow.value.filter { it.gifId != gif.id }
        saveActiveOverlaysToPrefs(_activeOverlaysFlow.value)
    }

    fun setActiveGif(id: Long) {
        val currentGifs = _gifsFlow.value.map { it.copy(isActive = it.id == id) }
        saveGifsToPrefs(currentGifs)
        _gifsFlow.value = currentGifs
    }

    private fun getAllGifsFromPrefs(): List<SavedGif> {
        val json = prefs.getString(KEY_GIFS, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedGif>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveGifsToPrefs(gifs: List<SavedGif>) {
        prefs.edit().putString(KEY_GIFS, gson.toJson(gifs)).apply()
    }

    // Active Overlays Operations
    fun getActiveOverlays(): Flow<List<ActiveOverlay>> = _activeOverlaysFlow

    fun getActiveOverlaysList(): List<ActiveOverlay> = _activeOverlaysFlow.value

    fun getActiveOverlayById(id: String): ActiveOverlay? {
        return _activeOverlaysFlow.value.find { it.id == id }
    }

    fun canAddMoreOverlays(): Boolean {
        return _activeOverlaysFlow.value.size < MAX_OVERLAYS
    }

    fun addActiveOverlay(overlay: ActiveOverlay): Boolean {
        if (!canAddMoreOverlays()) return false
        
        val currentOverlays = _activeOverlaysFlow.value.toMutableList()
        currentOverlays.add(overlay)
        saveActiveOverlaysToPrefs(currentOverlays)
        _activeOverlaysFlow.value = currentOverlays
        return true
    }

    fun updateActiveOverlay(overlay: ActiveOverlay) {
        val currentOverlays = _activeOverlaysFlow.value.toMutableList()
        val index = currentOverlays.indexOfFirst { it.id == overlay.id }
        if (index >= 0) {
            currentOverlays[index] = overlay
            saveActiveOverlaysToPrefs(currentOverlays)
            _activeOverlaysFlow.value = currentOverlays
        }
    }

    fun removeActiveOverlay(id: String) {
        val currentOverlays = _activeOverlaysFlow.value.filter { it.id != id }
        saveActiveOverlaysToPrefs(currentOverlays)
        _activeOverlaysFlow.value = currentOverlays
    }

    fun clearAllActiveOverlays() {
        saveActiveOverlaysToPrefs(emptyList())
        _activeOverlaysFlow.value = emptyList()
    }

    private fun getActiveOverlaysFromPrefs(): List<ActiveOverlay> {
        val json = prefs.getString(KEY_ACTIVE_OVERLAYS, null) ?: return emptyList()
        val type = object : TypeToken<List<ActiveOverlay>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveActiveOverlaysToPrefs(overlays: List<ActiveOverlay>) {
        prefs.edit().putString(KEY_ACTIVE_OVERLAYS, gson.toJson(overlays)).apply()
    }

    // Settings Operations
    fun getSettings(): Flow<AppSettings> = _settingsFlow

    fun getSettingsSync(): AppSettings {
        return _settingsFlow.value
    }

    fun updateSettings(settings: AppSettings) {
        prefs.edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply()
        _settingsFlow.value = settings
    }

    private fun getSettingsFromPrefs(): AppSettings {
        val json = prefs.getString(KEY_SETTINGS, null)
        return try {
            json?.let { gson.fromJson(it, AppSettings::class.java) } ?: AppSettings()
        } catch (e: Exception) {
            AppSettings()
        }
    }
}
