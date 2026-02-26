package com.potadev.floatymo.domain.repository

import com.potadev.floatymo.domain.model.SavedGif
import kotlinx.coroutines.flow.Flow

interface GifRepository {
    fun getAllGifs(): Flow<List<SavedGif>>
    fun getActiveGif(): Flow<SavedGif?>
    suspend fun getActiveGifSync(): SavedGif?
    suspend fun getGifById(id: Long): SavedGif?
    suspend fun saveGif(gif: SavedGif): Long
    suspend fun deleteGif(gif: SavedGif)
    suspend fun setActiveGif(id: Long)
}
