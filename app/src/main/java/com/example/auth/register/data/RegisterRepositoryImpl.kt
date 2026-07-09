package com.example.auth.register.data

import android.content.Context
import com.example.auth.register.domain.RegisterRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RegisterRepositoryImpl(
    private val userDao: UserDao,
    private val context: Context
) : RegisterRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    override suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override fun isDarkTheme(): Flow<Boolean> {
        // This is a bit of a hack since we're splitting everything.
        // Ideally this comes from a SettingsRepository.
        return MutableStateFlow(prefs.getBoolean("dark_theme", true))
    }
}
