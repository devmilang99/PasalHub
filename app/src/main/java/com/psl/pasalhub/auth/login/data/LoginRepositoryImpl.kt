package com.psl.pasalhub.auth.login.data

import android.content.Context
import androidx.core.content.edit
import com.psl.pasalhub.auth.login.domain.LoginRepository
import com.psl.pasalhub.core.auth.domain.SupabaseAuthRepository
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesRepository
) : LoginRepository {

    private val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun signIn(email: String, pass: String) {
        supabaseAuthRepository.signIn(email, pass)
    }

    override suspend fun googleSignIn(idToken: String) {
        supabaseAuthRepository.googleSignIn(idToken)
    }

    override fun getLastEmail(): String {
        return prefs.getString("last_email", "") ?: ""
    }

    override fun saveLastEmail(email: String) {
        prefs.edit { putString("last_email", email) }
    }

    override fun isDarkTheme(): Flow<Boolean> = appPrefs.isDarkTheme()
}
