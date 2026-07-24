# Google Sign-In Error Investigation [28444]

## Error Analysis
The error message **"Google Sign-In failed: [28444] Developer console is not set up correctly"** is a common configuration issue when using `CredentialManager` for Google Sign-In.

### Identified Potential Causes:
1.  **Invalid Web Client ID**: The `serverClientId` passed to the Google Sign-In request is likely incorrect or a placeholder.
    *   In `LoginScreen.kt`, the ID is fetched from `BuildConfig.GOOGLE_SERVER_CLIENT_ID`.
    *   This field is populated by the `secrets` Gradle plugin from the `.env` file.
    *   The `.env` file is currently missing in the root directory, so it might be falling back to `.env.example` values: `your_google_server_client_id_here`.
2.  **SHA-1 Mismatch**: The SHA-1 certificate fingerprint of the app (especially for debug builds) must be registered in the Firebase/Google Cloud Console.
    *   `google-services.json` contains one SHA-1: `ae06d9146f9cb4cf5fd2b30cd15a5feaa9f56356`.
    *   If the user's local debug certificate differs from this, authentication will fail.
3.  **OAuth Consent Screen**: The Google Cloud project must have a configured OAuth Consent Screen.

## Findings in Codebase
- **Package Name**: `com.aistudio.ecommerceshop.pqwzxl` (Matches `google-services.json`).
- **Web Client ID in `google-services.json`**: `1079515205022-ig535etmdi6l9sc98hrj1ojb610sgc7p.apps.googleusercontent.com`.
- **Implementation**: Uses `CredentialManager` with `GetGoogleIdOption`.

## Recommended Actions
1.  **Configure `.env` file**: Create a `.env` file in the root directory with the correct values.
2.  **Verify SHA-1**: Ensure the local debug SHA-1 is added to the Firebase/Google Cloud project.
3.  **Check OAuth Consent Screen**: Ensure it's published or the user is added as a test user.
