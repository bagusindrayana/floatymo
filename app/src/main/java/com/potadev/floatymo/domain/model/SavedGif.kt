package com.potadev.floatymo.domain.model

enum class GifSource {
    BUNDLE,
    GIPHY,
    USER
}

data class SavedGif(
    val id: Long = 0,
    val name: String,
    val source: GifSource,
    val filePath: String,
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)
