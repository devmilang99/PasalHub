package com.psl.pasalhub.auth.forgotpassword.domain

import kotlinx.coroutines.flow.Flow

interface ForgotPasswordRepository {
    suspend fun resetPassword(email: String)
    fun isDarkTheme(): Flow<Boolean>
}
