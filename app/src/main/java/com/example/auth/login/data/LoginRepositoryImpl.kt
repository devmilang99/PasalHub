package com.example.auth.login.data

import android.content.Context
import androidx.core.content.edit
import com.example.auth.login.domain.LoginRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

class LoginRepositoryImpl(
    private val userDao: UserDao,
    private val context: Context
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
}
