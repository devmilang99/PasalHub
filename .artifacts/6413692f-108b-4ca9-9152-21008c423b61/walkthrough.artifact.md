# Walkthrough - IllegalAccessError Fix

I have synchronized the Ktor and Supabase dependencies to resolve the `java.lang.IllegalAccessError` related to Ktor 3 value classes.

## Changes

### Build Configuration

#### [libs.versions.toml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/gradle/libs.versions.toml)
- Added `supabase-bom` to manage Supabase module versions centrally.
- Removed explicit versions from individual Supabase libraries to let the BOM handle them.

#### [app/build.gradle.kts](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
- Implemented `implementation(platform(libs.supabase.bom))` to ensure all Supabase modules (Auth, Postgrest, Realtime) are binary-compatible.
- Added an exclusion for `io.ktor` in the `generativeai` dependency. This prevents Ktor 2 (which `generativeai` might transitively depend on) from conflicting with the forced Ktor 3 version used by Supabase.

## Verification Results

### Automated Tests
- Ran `./gradlew app:assembleDebug`: **SUCCESS**. The project compiles correctly with the new dependency configuration.
- Gradle Sync: **SUCCESS**.

### Manual Verification Required
- [ ] Deploy the application to a device.
- [ ] Attempt to sign in (Email/Password or Google).
- [ ] Confirm the app no longer crashes with `IllegalAccessError`.

> [!TIP]
> If you encounter any further `NoSuchMethodError` or `IllegalAccessError` related to Ktor, it might be necessary to check if other libraries also need Ktor exclusions.
