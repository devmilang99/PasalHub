package com.example.auth.forgotpassword.domain

interface ForgotPasswordRepository {
    suspend fun resetPassword(email: String)
}
