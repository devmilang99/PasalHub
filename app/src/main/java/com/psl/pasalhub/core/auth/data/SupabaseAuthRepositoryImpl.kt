package com.psl.pasalhub.core.auth.data

import com.psl.pasalhub.core.auth.domain.SupabaseAuthRepository
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val userDao: UserDao,
    private val postgrest: Postgrest
) : SupabaseAuthRepository {

    private val auth: Auth = supabaseClient.auth

    override val currentUser: Flow<UserEntity?> = userDao.getUser()

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        syncUserProfile()
    }

    override suspend fun signUp(email: String, password: String, name: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // Create profile in Supabase
        val userId = auth.currentUserOrNull()?.id ?: return
        val profile = mapOf(
            "id" to userId,
            "email" to email,
            "name" to name,
            "address" to "",
            "date_of_birth" to ""
        )
        postgrest["profiles"].insert(profile)
        syncUserProfile()
    }

    override suspend fun signOut() {
        auth.signOut()
        userDao.clearUser()
    }

    override suspend fun googleSignIn(idToken: String) {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
        syncUserProfile()
    }

    override suspend fun updateProfile(name: String, address: String, dateOfBirth: String) {
        val user = auth.currentUserOrNull() ?: return
        val profile = mapOf(
            "name" to name,
            "address" to address,
            "date_of_birth" to dateOfBirth
        )
        postgrest["profiles"].update(profile) {
            filter {
                eq("id", user.id)
            }
        }
        syncUserProfile()
    }

    private suspend fun syncUserProfile() {
        val user = auth.currentUserOrNull() ?: return

        try {
            // Fetch profile from Postgrest
            val profile = postgrest["profiles"]
                .select {
                    filter {
                        eq("id", user.id)
                    }
                }
                .decodeSingle<Map<String, String?>>()

            val userEntity = UserEntity(
                id = user.id,
                email = user.email ?: "",
                name = profile["name"] ?: "",
                dateOfBirth = profile["date_of_birth"] ?: "",
                address = profile["address"] ?: "",
                isRemembered = true,
                isGoogleUser = false,
                profileImage = user.userMetadata?.get("avatar_url")?.toString()
            )
            userDao.insertUser(userEntity)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if profile doesn't exist yet
            val userEntity = UserEntity(
                id = user.id,
                email = user.email ?: "",
                name = user.userMetadata?.get("full_name")?.toString() ?: "",
                dateOfBirth = "",
                address = "",
                isRemembered = true,
                isGoogleUser = false,
                profileImage = user.userMetadata?.get("avatar_url")?.toString()
            )
            userDao.insertUser(userEntity)
        }
    }
}
