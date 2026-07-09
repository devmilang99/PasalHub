package com.example.auth.forgotpassword.data

import com.example.auth.forgotpassword.domain.ForgotPasswordRepository

class ForgotPasswordRepositoryImpl : ForgotPasswordRepository {
    override suspend fun resetPassword(email: String) {
        // Simulate password reset request
    }
}
