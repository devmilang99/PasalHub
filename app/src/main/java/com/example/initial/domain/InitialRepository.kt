package com.example.initial.domain

import com.example.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface InitialRepository {
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun completeOnboarding()
    
    fun isThemeSet(): Flow<Boolean>
    fun isDarkTheme(): Flow<Boolean>
    suspend fun setTheme(isDark: Boolean)
    
    fun getLocationPermission(): Flow<Boolean>
    fun getCameraPermission(): Flow<Boolean>
    fun getStoragePermission(): Flow<Boolean>
    fun getNotificationPermission(): Flow<Boolean>
    
    suspend fun setLocationPermission(granted: Boolean)
    suspend fun setCameraPermission(granted: Boolean)
    suspend fun setStoragePermission(granted: Boolean)
    suspend fun setNotificationPermission(granted: Boolean)
    
    fun isFlowCompleted(): Flow<Boolean>
    fun getUser(): Flow<UserEntity?>
}
