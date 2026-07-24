# Implementation Plan - Use Theme Colors for Backgrounds

The goal is to update the Splash, Login, and Onboarding screens to use the background colors defined in the `PasalHubTheme` (Light/Dark) instead of hardcoded colors or relying solely on background images.

## Proposed Changes

### [Initial & Auth Components]

#### [MODIFY] [SplashScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/initial/presentation/splash/SplashScreen.kt)
- Update the gradient colors in `SplashScreen` to use `PasalHubTheme.colors.background` instead of hardcoded `Color.White` and `Color.Black`.
- Ensure the base background of the `Box` uses `PasalHubTheme.colors.background`.

#### [MODIFY] [LoginScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/auth/login/ui/LoginScreen.kt)
- Set the `containerColor` of the `Scaffold` to `PasalHubTheme.colors.background` (currently `Color.Transparent`).
- Optionally remove or adjust the background `Image` to better integrate with the theme color. I will keep the image but ensure it's on top of the theme background or replace it if preferred. Given the request "use light and dark theme color for background", I'll prioritize the theme's background color.

#### [MODIFY] [OnboardingScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/initial/presentation/onboarding/OnboardingScreen.kt)
- Set the `containerColor` of the `Scaffold` to `PasalHubTheme.colors.background`.
- Adjust the background `Image` usage to align with the theme color.

## Verification Plan

### Automated Tests
- N/A (UI changes)

### Manual Verification
- Deploy the app and toggle between light and dark themes.
- Verify that the Splash, Login, and Onboarding screens reflect the `LightBackground` (`#FFF9F5`) and `DarkBackground` (`#0F0A09`) colors correctly.
- Ensure text legibility is maintained with the new background colors.
