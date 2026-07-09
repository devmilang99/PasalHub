package com.example.auth.login.domain

import com.example.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun getUser(): Flow<UserEntity?>
    suspend fun saveUser(user: UserEntity)
    fun isValidatedUser(email: String, pass: String): Boolean
    fun getLastEmail(): String
    fun saveLastEmail(email: String)
}
