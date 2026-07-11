package com.example.auth.login.data

import android.content.Context
import androidx.core.content.edit
import com.example.auth.login.domain.LoginRepository
import com.example.core.application.domain.AppPreferencesRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesRepository
) : LoginRepository {
    
    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override fun isValidatedUser(email: String, pass: String): Boolean {
        val validUsers = mapOf(
            "admin@pasalhub.com" to "admin123",
            "premium@pasalhub.com" to "luxury",
            "test@example.com" to "password"
        )
        return validUsers[email] == pass
    }

    override fun getLastEmail(): String {
        return prefs.getString("last_email", "") ?: ""
    }

    override fun saveLastEmail(email: String) {
        prefs.edit { putString("last_email", email) }
    }

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()
}
