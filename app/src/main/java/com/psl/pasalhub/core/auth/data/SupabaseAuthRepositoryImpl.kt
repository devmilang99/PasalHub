package com.psl.pasalhub.core.auth.data

import com.psl.pasalhub.core.auth.domain.SupabaseAuthRepository
import com.psl.pasalhub.core.database.data.UserDao
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.sync.SyncManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val userDao: UserDao,
    private val postgrest: Postgrest,
    private val syncManager: SyncManager
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
        val profile = buildJsonObject {
            put("id", userId)
            put("email", email)
            put("name", name)
            put("address", "")
            put("date_of_birth", "")
        }
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
        val profile = buildJsonObject {
            put("name", name)
            put("address", address)
            put("date_of_birth", dateOfBirth)
        }
        postgrest["profiles"].update(profile) {
            filter {
                eq("id", user.id)
            }
        }
        syncUserProfile()
    }

    override suspend fun updateOnboardingStatus(done: Boolean) {
        val user = auth.currentUserOrNull() ?: return
        val profile = buildJsonObject {
            put("onboarding_done", done)
        }
        postgrest["profiles"].update(profile) {
            filter {
                eq("id", user.id)
            }
        }
        syncUserProfile()
    }

    override suspend fun completeGoogleOnboarding(password: String, address: String) {
        val user = auth.currentUserOrNull() ?: return

        val name = user.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: ""
        val email = user.email ?: ""

        val profileData = buildJsonObject {
            put("id", user.id)
            put("email", email)
            put("name", name)
            put("address", address)
            put("is_profile_complete", true)
            put("password_hint", password)
        }

        postgrest["profiles"].upsert(profileData)
        auth.updateUser {
            this.password = password
        }
        syncUserProfile()
    }

    override suspend fun updatePassword(newPassword: String) {
        auth.updateUser {
            password = newPassword
        }
    }

    override fun currentUserEmail(): String? {
        return auth.currentUserOrNull()?.email
    }

    private fun syncUserProfile() {
        // Trigger the sequential multi-step sync chain for login
        syncManager.triggerLoginSync()
    }
}
