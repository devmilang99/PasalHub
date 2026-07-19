# Implementation Plan - Fix IllegalAccessError (Ktor 3 Compatibility)

The application is crashing with a `java.lang.IllegalAccessError` when performing Supabase authentication. This is caused by a binary incompatibility between Ktor 2 and Ktor 3. Specifically, `io.ktor.http.HttpMethod` was changed from a regular class to a `value class` in Ktor 3, which breaks libraries compiled against Ktor 2.

## User Review Required

> [!IMPORTANT]
> This fix involves upgrading Ktor and Supabase versions to their latest stable releases and ensuring all modules are synchronized. This may require minor code adjustments if there are breaking changes in newer Ktor/Supabase versions.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/gradle/libs.versions.toml)
- Update `ktor` version to `3.5.1` (latest stable).
- Ensure `supabase` is at `3.6.0` (already is).
- Add Supabase BOM entry for better dependency management.
- Verify and potentially update `kotlin` version to a stable release if `2.4.0` is causing issues with value class visibility. (We'll keep it for now but be ready to downgrade to `2.1.0` if needed).

#### [MODIFY] [app/build.gradle.kts](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
- Implement Supabase BOM.
- Update `resolutionStrategy` to use the new Ktor version.
- Exclude transitive Ktor 2 dependencies from libraries that might still be using them (e.g., `generativeai`).

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project still compiles.
- Run existing unit tests: `./gradlew test`.

### Manual Verification
- Deploy the app to a device/emulator.
- Perform a Login operation (Email/Password and Google Sign-In) to verify the `IllegalAccessError` is resolved.
