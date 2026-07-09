package com.example.auth.forgotpassword.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.forgotpassword.domain.ForgotPasswordRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: ForgotPasswordRepository
) : ViewModel() {

    fun resetPassword(email: String) {
        viewModelScope.launch {
            repository.resetPassword(email)
        }
    }
}
