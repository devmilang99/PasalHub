package com.psl.pasalhub.auth.forgotpassword.data

import com.psl.pasalhub.auth.forgotpassword.domain.ForgotPasswordRepository
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ForgotPasswordRepositoryImpl @Inject constructor(
    private val appPrefs: AppPreferencesRepository
) : ForgotPasswordRepository {
    override suspend fun resetPassword(email: String) {
        // Simulate password reset request
    }

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()
}
