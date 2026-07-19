# Implementation Plan - Fix Supabase Network Error

This plan addresses the "No API key found in request" error when communicating with Supabase.

## User Review Required

> [!IMPORTANT]
> The issue is likely caused by the double quotes around values in the `.env` file. The `secrets-gradle-plugin` treats these quotes as part of the value, leading to invalid headers (e.g., `apikey: "your_key"` instead of `apikey: your_key`).

## Proposed Changes

### Configuration

#### [MODIFY] [.env](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/.env)
- Remove double quotes from `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `GOOGLE_SERVER_CLIENT_ID`, and `GEMINI_API_KEY`.

### Core Dagger/Hilt Module

#### [MODIFY] [SupabaseModule.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/di/SupabaseModule.kt)
- Add a check to trim any accidental whitespace or quotes from the `BuildConfig` values, just in case.

## Verification Plan

### Manual Verification
- Rebuild the app and verify that network requests to Supabase (like fetching products or syncing cart) no longer return the "No API key found" error.
- You can check the logs for any `PostgrestException` or `RestException`.
