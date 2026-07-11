package com.example.auth.register.data

import android.content.Context
import com.example.auth.register.domain.RegisterRepository
import com.example.core.database.data.UserDao
import com.example.core.database.data.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class RegisterRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
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
