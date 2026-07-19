# Purge Firebase and Consolidate on Supabase for Auth

The goal is to remove all Firebase-related configurations and dependencies, ensuring that **Supabase** is the sole provider for authentication (including Google Sign-In) and migrating the AI features to the standalone Google AI SDK.

## User Review Required

> [!WARNING]
> This will remove the **Firebase App Check** security layer. If you rely on App Check to protect your Gemini API usage, you should consider the implications of using the standalone SDK which uses an API Key instead of App Check tokens.

> [!IMPORTANT]
> You will need a **Google AI API Key** from the [Google AI Studio](https://aistudio.google.com/) to replace the Firebase-managed Gemini access.

## Proposed Changes

### 1. Update Dependencies
We will remove Firebase SDKs and add the standalone Google Generative AI SDK.

#### [MODIFY] [libs.versions.toml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/gradle/libs.versions.toml)
- Remove `firebaseBom`, `firebase-ai`, `firebase-appcheck-recaptcha`.
- Add `generativeai = "0.9.0"` (or latest stable).

#### [MODIFY] [build.gradle.kts](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
- Remove `alias(libs.plugins.google.services)`.
- Remove Firebase-related implementation lines.
- Add `implementation(libs.generativeai)`.

### 2. Migrate AI Components
We need to update the AI logic to use the standard SDK instead of the Firebase wrapper.

#### [MODIFY] [GeminiSearchRouter.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/ai/data/GeminiSearchRouter.kt)
- Change imports from `com.google.firebase.ai` to `com.google.ai.client.generativeai`.
- Update initialization to use an API Key (which we will add to `.env`).

#### [MODIFY] [AiSearchViewModel.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/ai/presentation/AiSearchViewModel.kt)
- Update imports for the new AI SDK.

### 3. Clean up Firebase Configuration
#### [DELETE] [google-services.json](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/google-services.json)
- This file is no longer needed once the Google Services plugin is removed.

#### [MODIFY] [.env](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/.env)
- Ensure `GOOGLE_SERVER_CLIENT_ID` is present for Supabase Google Sign-In.
- [NEW] Add `GEMINI_API_KEY=your_api_key_here`.

## Verification Plan

### Automated Tests
- Run `gradle sync` to ensure dependencies resolve.
- Run `gradle assembleDebug` to verify compilation.

### Manual Verification
1. Verify that Google Sign-In still works (it uses `CredentialManager` + Supabase, which doesn't require Firebase).
2. Verify that the AI Chat works with the new API Key.
