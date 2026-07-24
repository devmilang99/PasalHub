# Fix Google Sign-In: "Could not retrieve Google account information"

The error "Could not retrieve Google account information" occurs because the app is failing to correctly parse the credential returned by the Android `CredentialManager`.

## Problem Analysis
In `LoginScreen.kt`, the code uses a `when` expression to check if the returned credential is an instance of `GoogleIdTokenCredential`:

```kotlin
val idToken = when (val credential = result.credential) {
    is GoogleIdTokenCredential -> credential.idToken
    else -> null
}
```

However, the Android `CredentialManager` returns a `CustomCredential` object when using the Google ID Token provider. The `is GoogleIdTokenCredential` check fails because the actual object is a wrapper, and it falls into the `else` block, resulting in the reported error message.

## Proposed Changes

### 1. Update Credential Handling Logic
Modify the Google Sign-In result handling in `LoginScreen.kt` to correctly extract the ID Token from a `CustomCredential`.

- **[MODIFY] [LoginScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/login/ui/LoginScreen.kt)**:
    - Import `androidx.credentials.CustomCredential`.
    - Update the `idToken` extraction logic to use `GoogleIdTokenCredential.createFrom(credential.data)` for `CustomCredential` types.

#### Correct Implementation Pattern:
```kotlin
val idToken = when (val credential = result.credential) {
    is CustomCredential -> {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else null
    }
    else -> null
}
```

## Verification Plan

### Manual Verification
1. Build and run the app.
2. Tap "Sign in with Google".
3. Select an account from the Google account picker.
4. Verify that the app successfully retrieves the ID token and proceeds to Supabase authentication (or shows a different error if Supabase configuration is still pending, but *not* the "Could not retrieve Google account information" error).
