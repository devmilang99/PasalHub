# Walkthrough - Re-integrating Google Sign-In with Supabase

I have successfully re-integrated Google Sign-In into your application, now using **Supabase** as the authentication backend.

## Changes Made

### 1. Dependencies and Configuration
- **Libraries:** Re-added `androidx.credentials`, `androidx.credentials-play-services-auth`, and `googleid` to `libs.versions.toml` and `app/build.gradle.kts`.
- **BuildConfig:** Added `GOOGLE_SERVER_CLIENT_ID` to `BuildConfig` so it can be securely loaded from your `.env` file via the Secrets Gradle Plugin.
- **Environment:** Updated `.env.example` to include the required Google Client ID variable.

### 2. Domain and Data Layer
- **[SupabaseAuthRepository](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/auth/domain/SupabaseAuthRepository.kt):** Added `googleSignIn(idToken: String)` to the interface.
- **[SupabaseAuthRepositoryImpl](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/auth/data/SupabaseAuthRepositoryImpl.kt):** Implemented Google Sign-In using the Supabase `IDToken` provider. After a successful sign-in, the user's profile is automatically synced to the local Room database.

### 3. Login Flow
- **[LoginRepository](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/domain/LoginRepository.kt):** Added `googleSignIn` to the interface.
- **[LoginViewModel](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/viewmodel/LoginViewModel.kt):** Added a `googleSignIn` method to bridge the UI and Repository.

### 4. UI Layer
- **[LoginScreen](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/ui/LoginScreen.kt):** Re-added the "Sign in with Google" button and integrated the Android **Credential Manager** flow. It now securely retrieves the Google ID Token and passes it to Supabase.

## Verification Results

### Build Status
> [!NOTE]
> The project builds successfully with `app:assembleDebug`.

### Manual Verification Steps
1.  **Configure Client ID:** Add your Google Web Client ID to your `.env` file:
    ```env
    GOOGLE_SERVER_CLIENT_ID=your_actual_client_id_here
    ```
2.  **Supabase Setup:** Ensure you have added this same Client ID to your Supabase Dashboard under **Authentication -> Providers -> Google**.
3.  **Run App:** Launch the app and click "Sign in with Google".
4.  **Login:** Select your Google account. The app will authenticate with Supabase and navigate to the dashboard upon success.
