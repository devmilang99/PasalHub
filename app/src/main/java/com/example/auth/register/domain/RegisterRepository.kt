package com.example.auth.register.domain

import com.example.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    suspend fun saveUser(user: UserEntity)
    fun isDarkTheme(): Flow<Boolean>
}
