# Re-integrating Google Sign-In with Supabase

This plan outlines the steps to re-add Google Sign-In functionality to the login screen, using Supabase as the backend instead of Firebase.

## User Review Required

> [!IMPORTANT]
> **Supabase Configuration:** Ensure you have configured Google as an Auth Provider in your Supabase Dashboard and provided the correct **Client ID** from your Google Cloud Console. You will also need to add the `GOOGLE_SERVER_CLIENT_ID` to your `.env` or `local.properties` file if it's not already there.

## Proposed Changes

### 1. Dependencies and Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/gradle/libs.versions.toml)
- Re-add versions and library definitions for `androidx.credentials`, `androidx.credentials.play.services.auth`, and `googleid`.

#### [MODIFY] [build.gradle.kts (app)](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
- Add the Google Sign-in dependencies to the `dependencies` block.

---

### 2. Domain and Data Layer (Supabase Integration)

#### [MODIFY] [SupabaseAuthRepository.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/auth/domain/SupabaseAuthRepository.kt)
- Add `suspend fun googleSignIn(idToken: String)` to the interface.

#### [MODIFY] [SupabaseAuthRepositoryImpl.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/auth/data/SupabaseAuthRepositoryImpl.kt)
- Implement `googleSignIn` using `auth.signInWith(Google)`.
- Ensure `syncUserProfile()` is called after a successful Google Sign-in to populate the local `UserEntity`.

---

### 3. Login Layer

#### [MODIFY] [LoginRepository.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/domain/LoginRepository.kt)
- Add `suspend fun googleSignIn(idToken: String)` to the interface.

#### [MODIFY] [LoginRepositoryImpl.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/data/LoginRepositoryImpl.kt)
- Delegate `googleSignIn` to `SupabaseAuthRepository`.

#### [MODIFY] [LoginViewModel.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/viewmodel/LoginViewModel.kt)
- Add a `googleSignIn` method that triggers the repository call.

---

### 4. UI Layer

#### [MODIFY] [LoginScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/ui/LoginScreen.kt)
- Re-add the "Sign in with Google" button.
- Re-integrate the `CredentialManager` flow to obtain the Google ID token.
- Update the success callback to call `viewModel.googleSignIn(idToken)`.

## Verification Plan

### Automated Tests
- Run `app:assembleDebug` to verify that all dependencies and code changes compile correctly.

### Manual Verification
- Deploy the app and click the "Sign in with Google" button.
- Verify that the Google account picker appears.
- Select an account and verify that the app successfully logs in and navigates to the dashboard.
- Check the Supabase Dashboard to confirm a new user is created/logged in via Google.
