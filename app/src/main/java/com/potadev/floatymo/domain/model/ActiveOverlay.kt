package com.potadev.floatymo.domain.model

data class ActiveOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val gifId: Long,
    val x: Int = -1,
    val y: Int = -1,
    val size: Float = 1.0f,
    val opacity: Float = 1.0f
)
