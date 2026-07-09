package com.example.initial.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.data.UserEntity
import com.example.initial.domain.InitialRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InitialViewModel(
    private val repository: InitialRepository
) : ViewModel() {

    val onboardingCompleted: StateFlow<Boolean> = repository.isOnboardingCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isThemeSet: StateFlow<Boolean> = repository.isThemeSet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val locationPermissionGranted: StateFlow<Boolean> = repository.getLocationPermission()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val cameraPermissionGranted: StateFlow<Boolean> = repository.getCameraPermission()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val storagePermissionGranted: StateFlow<Boolean> = repository.getStoragePermission()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationPermissionGranted: StateFlow<Boolean> = repository.getNotificationPermission()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isFlowCompleted: StateFlow<Boolean> = repository.isFlowCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.setTheme(isDark)
        }
    }

    fun setLocationPermission(granted: Boolean) {
        viewModelScope.launch {
            repository.setLocationPermission(granted)
        }
    }

    fun setCameraPermission(granted: Boolean) {
        viewModelScope.launch {
            repository.setCameraPermission(granted)
        }
    }

    fun setStoragePermission(granted: Boolean) {
        viewModelScope.launch {
            repository.setStoragePermission(granted)
        }
    }

    fun setNotificationPermission(granted: Boolean) {
        viewModelScope.launch {
            repository.setNotificationPermission(granted)
        }
    }
}
