# Walkthrough - Fixed Google Sign-In Credential Retrieval

I have resolved the "Could not retrieve Google account information" error by updating the logic to correctly extract the ID Token from the Android `CredentialManager` response.

## Changes Made

### Updated Credential Parsing logic
Previously, the code was checking if the credential was an instance of `GoogleIdTokenCredential` directly. However, the Credential Manager returns a `CustomCredential` wrapper when using the Google identity provider.

#### [LoginScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/login/ui/LoginScreen.kt)
- Added import for `androidx.credentials.CustomCredential`.
- Updated the `when` block in the Google Sign-In button logic to handle `CustomCredential`.
- Used `GoogleIdTokenCredential.createFrom(credential.data)` to safely extract the ID token and metadata from the custom credential wrapper.

```kotlin
val idToken = when (val credential = result.credential) {
    is CustomCredential -> {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            null
        }
    }
    // ... handles other types
    else -> null
}
```

## Verification Results

### Automated Tests
- Ran `gradle_build` (`:app:assembleDebug`): Successful.

### Manual Verification Required
- Launch the app.
- Tap "Sign in with Google".
- The Google account picker should now appear, and selecting an account should successfully retrieve the ID token for Supabase authentication.
