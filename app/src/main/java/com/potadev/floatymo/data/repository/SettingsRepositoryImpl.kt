package com.potadev.floatymo.data.repository

import com.potadev.floatymo.data.local.LocalStorage
import com.potadev.floatymo.domain.model.AppSettings
import com.potadev.floatymo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val localStorage: LocalStorage
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return localStorage.getSettings()
    }

    override suspend fun getSettingsSync(): AppSettings {
        return localStorage.getSettingsSync()
    }

    override suspend fun updateSettings(settings: AppSettings) {
        localStorage.updateSettings(settings)
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return localStorage.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localStorage.setOnboardingCompleted(completed)
    }
}
