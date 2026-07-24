package com.psl.pasalhub.core.application.domain

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun isDarkTheme(): Flow<Boolean>
    fun isThemeSet(): Flow<Boolean>
    suspend fun toggleTheme()
    suspend fun setTheme(isDark: Boolean)

    fun getNotificationEvent(): Flow<String>
    suspend fun emitNotification(message: String)

    fun getFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(productId: Int)

    fun getGlobalError(): Flow<AppError?>
    suspend fun emitGlobalError(error: AppError?)

    fun getLastProductsSyncTime(): Long
    suspend fun setLastProductsSyncTime(timestamp: Long)
}
