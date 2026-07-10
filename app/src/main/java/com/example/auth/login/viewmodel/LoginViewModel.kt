package com.example.auth.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.login.domain.LoginRepository
import com.example.core.database.data.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    private val _lastEmail = MutableStateFlow(repository.getLastEmail())
    val lastEmail: StateFlow<String> = _lastEmail.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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
            
            // Save last email for autofill
            repository.saveLastEmail(email)
            _lastEmail.value = email
        }
    }

    fun isValidatedUser(email: String, pass: String): Boolean {
        return repository.isValidatedUser(email, pass)
    }
}
