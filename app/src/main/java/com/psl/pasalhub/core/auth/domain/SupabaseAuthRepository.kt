package com.psl.pasalhub.core.auth.domain

import com.psl.pasalhub.core.database.data.UserEntity
import kotlinx.coroutines.flow.Flow

interface SupabaseAuthRepository {
    val currentUser: Flow<UserEntity?>
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, name: String)
    suspend fun signOut()
    suspend fun googleSignIn(idToken: String)
    suspend fun updateProfile(name: String, address: String, dateOfBirth: String)
    suspend fun updateOnboardingStatus(done: Boolean)
    suspend fun completeGoogleOnboarding(password: String, address: String)
}
