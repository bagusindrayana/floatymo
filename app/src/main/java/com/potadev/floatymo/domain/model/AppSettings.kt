package com.potadev.floatymo.domain.model

data class AppSettings(
    val id: Int = 1,
    val overlaySize: Float = 1.0f,
    val overlayOpacity: Float = 1.0f,
    val overlayX: Int = -1,
    val overlayY: Int = -1,
    val isEditingPosition: Boolean = false
)

enum class OverlaySize(val scale: Float, val displayName: String) {
    SMALL(0.5f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.5f, "Large"),
    EXTRA_LARGE(2.0f, "Extra Large")
}
