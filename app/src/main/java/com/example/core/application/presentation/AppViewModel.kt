package com.example.core.application.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.application.domain.AppPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: AppPreferencesRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationEvent = repository.getNotificationEvent()

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
}
