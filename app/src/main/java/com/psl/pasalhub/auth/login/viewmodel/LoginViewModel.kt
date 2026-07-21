package com.psl.pasalhub.auth.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.auth.login.domain.LoginRepository
import com.psl.pasalhub.core.database.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
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

    suspend fun signIn(email: String, pass: String) {
        repository.signIn(email, pass)
        repository.saveLastEmail(email)
        _lastEmail.value = email
    }

    suspend fun googleSignIn(idToken: String) {
        repository.googleSignIn(idToken)
    }

    suspend fun completeOnboarding(password: String, address: String) {
        repository.completeGoogleOnboarding(password, address)
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
}
