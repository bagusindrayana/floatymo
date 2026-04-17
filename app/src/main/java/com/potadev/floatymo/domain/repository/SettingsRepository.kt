package com.potadev.floatymo.domain.repository

import com.potadev.floatymo.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun getSettingsSync(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
}
