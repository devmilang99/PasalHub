package com.example.core.application.data

import android.content.Context
import androidx.core.content.edit
import com.example.core.application.domain.AppPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AppPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferencesRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
    private val favPrefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)
    
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    private val _isThemeSet = MutableStateFlow(prefs.getBoolean("theme_set", false))
    private val _notificationEvent = MutableSharedFlow<String>()

    private val _favoriteIds = MutableStateFlow(
        favPrefs.getStringSet("fav_set", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    )

    override fun isDarkTheme(): Flow<Boolean> = _isDarkTheme.asStateFlow()

    override fun isThemeSet(): Flow<Boolean> = _isThemeSet.asStateFlow()

    override suspend fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        setTheme(newValue)
    }

    override suspend fun setTheme(isDark: Boolean) {
        prefs.edit {
            putBoolean("dark_theme", isDark)
            putBoolean("theme_set", true)
        }
        _isDarkTheme.value = isDark
        _isThemeSet.value = true
    }

    override fun getNotificationEvent(): Flow<String> = _notificationEvent.asSharedFlow()

    override suspend fun emitNotification(message: String) {
        _notificationEvent.emit(message)
    }

    override fun getFavoriteIds(): Flow<Set<Int>> = _favoriteIds.asStateFlow()

    override suspend fun toggleFavorite(productId: Int) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        favPrefs.edit {
            putStringSet("fav_set", current.map { it.toString() }.toSet())
        }
        _favoriteIds.value = current
    }
}
