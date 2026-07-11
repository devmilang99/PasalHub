package com.example.auth.register.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.register.domain.RegisterRepository
import com.example.core.database.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: RegisterRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun registerUser(
        context: Context,
        name: String,
        email: String,
        dateOfBirth: String,
        address: String,
        rememberMe: Boolean,
        isGoogleUser: Boolean = false,
        profileImage: String? = null
    ) {
        viewModelScope.launch {
            val user = UserEntity(
                email = email,
                name = name,
                dateOfBirth = dateOfBirth,
                address = address,
                isRemembered = rememberMe,
                isGoogleUser = isGoogleUser,
                profileImage = profileImage
            )
            repository.saveUser(user)
        }
    }
}
