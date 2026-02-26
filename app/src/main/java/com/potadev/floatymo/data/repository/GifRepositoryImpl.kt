package com.potadev.floatymo.data.repository

import com.potadev.floatymo.data.local.LocalStorage
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GifRepositoryImpl(
    private val localStorage: LocalStorage
) : GifRepository {

    override fun getAllGifs(): Flow<List<SavedGif>> {
        return localStorage.getAllGifs()
    }

    override fun getActiveGif(): Flow<SavedGif?> {
        return localStorage.getAllGifs().map { gifs ->
            gifs.find { it.isActive }
        }
    }

    override suspend fun getActiveGifSync(): SavedGif? {
        return localStorage.getActiveGif()
    }

    override suspend fun getGifById(id: Long): SavedGif? {
        return localStorage.getGifById(id)
    }

    override suspend fun saveGif(gif: SavedGif): Long {
        return localStorage.saveGif(gif)
    }

    override suspend fun deleteGif(gif: SavedGif) {
        localStorage.deleteGif(gif)
    }

    override suspend fun setActiveGif(id: Long) {
        localStorage.setActiveGif(id)
    }

    override fun getActiveOverlays(): Flow<List<ActiveOverlay>> {
        return localStorage.getActiveOverlays()
    }

    override fun getActiveOverlaysList(): List<ActiveOverlay> {
        return localStorage.getActiveOverlaysList()
    }

    override fun getActiveOverlayById(id: String): ActiveOverlay? {
        return localStorage.getActiveOverlayById(id)
    }

    override fun canAddMoreOverlays(): Boolean {
        return localStorage.canAddMoreOverlays()
    }

    override suspend fun addActiveOverlay(overlay: ActiveOverlay): Boolean {
        return localStorage.addActiveOverlay(overlay)
    }

    override suspend fun updateActiveOverlay(overlay: ActiveOverlay) {
        localStorage.updateActiveOverlay(overlay)
    }

    override suspend fun removeActiveOverlay(id: String) {
        localStorage.removeActiveOverlay(id)
    }

    override suspend fun clearAllActiveOverlays() {
        localStorage.clearAllActiveOverlays()
    }
}
