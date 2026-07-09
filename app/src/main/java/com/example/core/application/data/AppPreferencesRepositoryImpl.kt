package com.example.core.application.data

import android.content.Context
import androidx.core.content.edit
import com.example.core.application.domain.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferencesRepositoryImpl(private val context: Context) : AppPreferencesRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
    
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    private val _notificationEvent = MutableSharedFlow<String>()

    override fun isDarkTheme(): Flow<Boolean> = _isDarkTheme.asStateFlow()

    override suspend fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        setTheme(newValue)
    }

    override suspend fun setTheme(isDark: Boolean) {
        prefs.edit { putBoolean("dark_theme", isDark) }
        _isDarkTheme.value = isDark
    }

    override fun getNotificationEvent(): Flow<String> = _notificationEvent.asSharedFlow()

    override suspend fun emitNotification(message: String) {
        _notificationEvent.emit(message)
    }
}
