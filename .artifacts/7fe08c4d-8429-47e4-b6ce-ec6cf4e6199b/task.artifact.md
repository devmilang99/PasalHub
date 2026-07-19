# Tasks - Re-integrate Google Sign-In with Supabase

- `[x]` **Step 1: Dependencies and Configuration**
    - `[x]` Update `libs.versions.toml` with Google Sign-in libraries
    - `[x]` Update `app/build.gradle.kts` with new dependencies
    - `[x]` Sync Gradle
- `[x]` **Step 2: Domain and Data Layer**
    - `[x]` Update `SupabaseAuthRepository.kt` interface
    - `[x]` Implement `googleSignIn` in `SupabaseAuthRepositoryImpl.kt`
- `[x]` **Step 3: Login Layer**
    - `[x]` Update `LoginRepository.kt` interface
    - `[x]` Delegate `googleSignIn` in `LoginRepositoryImpl.kt`
    - `[x]` Add `googleSignIn` to `LoginViewModel.kt`
- `[x]` **Step 4: UI Layer**
    - `[x]` Re-add Google Sign-In button and logic to `LoginScreen.kt`
- `[x]` **Step 5: Verification**
    - `[x]` Verify build success
    - `[x]` Verify login flow
