package com.psl.pasalhub.initial.data

import android.content.Context
import androidx.core.content.edit
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.initial.domain.InitialRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InitialRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val userDao: UserDao,
    private val appPrefs: AppPreferencesRepository
) : InitialRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_done", false))

    private val _locationGranted = MutableStateFlow(prefs.getBoolean("perm_location", false))
    private val _cameraGranted = MutableStateFlow(prefs.getBoolean("perm_camera", false))
    private val _storageGranted = MutableStateFlow(prefs.getBoolean("perm_storage", false))
    private val _notificationGranted =
        MutableStateFlow(prefs.getBoolean("perm_notification", false))

    override fun isOnboardingCompleted(): Flow<Boolean> = _onboardingCompleted.asStateFlow()

    override suspend fun completeOnboarding() {
        prefs.edit { putBoolean("onboarding_done", true) }
        _onboardingCompleted.value = true
    }

    override fun isThemeSet(): Flow<Boolean> = appPrefs.isThemeSet()
    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()

    override suspend fun setTheme(isDark: Boolean) {
        appPrefs.setTheme(isDark)
    }

    override fun getLocationPermission(): Flow<Boolean> = _locationGranted.asStateFlow()
    override fun getCameraPermission(): Flow<Boolean> = _cameraGranted.asStateFlow()
    override fun getStoragePermission(): Flow<Boolean> = _storageGranted.asStateFlow()
    override fun getNotificationPermission(): Flow<Boolean> = _notificationGranted.asStateFlow()

    override suspend fun setLocationPermission(granted: Boolean) {
        prefs.edit { putBoolean("perm_location", granted) }
        _locationGranted.value = granted
    }

    override suspend fun setCameraPermission(granted: Boolean) {
        prefs.edit { putBoolean("perm_camera", granted) }
        _cameraGranted.value = granted
    }

    override suspend fun setStoragePermission(granted: Boolean) {
        prefs.edit { putBoolean("perm_storage", granted) }
        _storageGranted.value = granted
    }

    override suspend fun setNotificationPermission(granted: Boolean) {
        prefs.edit { putBoolean("perm_notification", granted) }
        _notificationGranted.value = granted
    }

    override fun isFlowCompleted(): Flow<Boolean> = combine(
        _onboardingCompleted,
        appPrefs.isThemeSet(),
        _locationGranted,
        _cameraGranted,
        _storageGranted,
        _notificationGranted
    ) { values ->
        values.all { it as Boolean }
    }

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()
}
