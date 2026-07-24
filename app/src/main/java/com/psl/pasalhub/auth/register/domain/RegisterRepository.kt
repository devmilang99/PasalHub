package com.psl.pasalhub.auth.register.domain

import com.psl.pasalhub.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    suspend fun saveUser(user: UserEntity)
    suspend fun signUp(email: String, pass: String, name: String)
    fun isDarkTheme(): Flow<Boolean>
}
