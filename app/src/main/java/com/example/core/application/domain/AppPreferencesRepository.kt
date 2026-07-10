package com.example.core.application.domain

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun isDarkTheme(): Flow<Boolean>
    fun isThemeSet(): Flow<Boolean>
    suspend fun toggleTheme()
    suspend fun setTheme(isDark: Boolean)
    
    fun getNotificationEvent(): Flow<String>
    suspend fun emitNotification(message: String)
}
