package com.example.auth.forgotpassword.data

import com.example.auth.forgotpassword.domain.ForgotPasswordRepository
import javax.inject.Inject

class ForgotPasswordRepositoryImpl @Inject constructor() : ForgotPasswordRepository {
    override suspend fun resetPassword(email: String) {
        // Simulate password reset request
    }
}
