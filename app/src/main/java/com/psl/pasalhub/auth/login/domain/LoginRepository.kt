package com.psl.pasalhub.auth.login.domain

import com.psl.pasalhub.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun getUser(): Flow<UserEntity?>
    suspend fun saveUser(user: UserEntity)
    suspend fun signIn(email: String, pass: String)
    suspend fun googleSignIn(idToken: String)
    fun getLastEmail(): String
    fun saveLastEmail(email: String)
    fun isDarkTheme(): Flow<Boolean>
}
