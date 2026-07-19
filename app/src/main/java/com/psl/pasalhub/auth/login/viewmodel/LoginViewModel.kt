package com.psl.pasalhub.auth.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.auth.login.domain.LoginRepository
import com.psl.pasalhub.core.database.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
}
