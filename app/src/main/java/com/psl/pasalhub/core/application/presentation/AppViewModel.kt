package com.psl.pasalhub.core.application.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: AppPreferencesRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationEvent = repository.getNotificationEvent()

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val globalError: StateFlow<com.psl.pasalhub.core.application.domain.AppError?> =
        repository.getGlobalError()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleTheme() {
        viewModelScope.launch {
            repository.toggleTheme()
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.setTheme(isDark)
        }
    }

    fun postNotification(message: String) {
        viewModelScope.launch {
            repository.emitNotification(message)
        }
    }

    fun setError(error: com.psl.pasalhub.core.application.domain.AppError?) {
        viewModelScope.launch {
            repository.emitGlobalError(error)
        }
    }
}
