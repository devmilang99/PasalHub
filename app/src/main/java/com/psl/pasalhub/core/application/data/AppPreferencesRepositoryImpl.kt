package com.psl.pasalhub.core.application.data

import android.content.Context
import androidx.core.content.edit
import com.psl.pasalhub.core.application.domain.AppError
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.FavoriteDao
import com.psl.pasalhub.core.database.data.FavoriteEntity
import com.psl.pasalhub.core.sync.SyncManager
import com.psl.pasalhub.core.sync.SyncType
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteDao: FavoriteDao,
    private val supabaseClient: SupabaseClient,
    private val syncManager: SyncManager
) : AppPreferencesRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    private val _isThemeSet = MutableStateFlow(prefs.getBoolean("theme_set", false))
    private val _notificationEvent = MutableSharedFlow<String>()
    private val _globalError = MutableStateFlow<AppError?>(null)

    init {
        // Schedule periodic sync for background updates
        syncManager.schedulePeriodicSync()

        // Initial sync of all data if logged in to ensure latest data is shown
        CoroutineScope(Dispatchers.IO).launch {
            if (supabaseClient.auth.currentUserOrNull() != null) {
                syncManager.triggerAllSyncs(fetch = true)
            }
        }
    }

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

    override fun getFavoriteIds(): Flow<Set<Int>> = favoriteDao.getFavorites()
        .map { it.map { fav -> fav.productId }.toSet() }

    override suspend fun toggleFavorite(productId: Int) {
        val user = supabaseClient.auth.currentUserOrNull()
        val userId = user?.id ?: "anonymous"

        val favorites = favoriteDao.getFavorites().first()
        val existing = favorites.find { it.productId == productId }

        if (existing != null) {
            favoriteDao.removeFavorite(existing)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(productId, userId))
        }

        if (user != null) {
            syncManager.triggerSync(SyncType.FAVORITES, immediate = true, fetch = false)
        }
    }

    override fun getGlobalError(): Flow<AppError?> = _globalError.asStateFlow()

    override suspend fun emitGlobalError(error: AppError?) {
        _globalError.value = error
    }
}
