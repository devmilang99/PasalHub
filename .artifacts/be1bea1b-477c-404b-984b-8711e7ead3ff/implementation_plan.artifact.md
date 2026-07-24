# Implementation Plan - Apply Consistent Background Theme Across Initial Screens

The goal is to apply the "Pasal Hub" background theme (theme-aware image + gradient scrim) to all initial and authentication screens for a consistent premium look. This pattern is already present in `RegisterScreen`.

## User Review Required

> [!IMPORTANT]
> This change will affect the visual identity of the entire onboarding and authentication flow. The solid backgrounds will be replaced with the theme-aware `image_bg_dark`/`image_bg_light` and a gradient scrim that matches the current theme's background color.

## Proposed Changes

The following screens will be updated to include the background image and gradient scrim:

### Authentication & Initial Screens

#### [MODIFY] [PermissionScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/permission/PermissionScreen.kt)
- Move background image outside `Scaffold`.
- Add gradient scrim.
- Wrap in root `Box`.
- Adjust image `alpha`.

#### [MODIFY] [ThemeSelectionScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/theme/ThemeSelectionScreen.kt)
- Move background image outside `Scaffold`.
- Add gradient scrim.
- Wrap in root `Box`.
- Adjust image `alpha`.

#### [MODIFY] [LoginScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/login/ui/LoginScreen.kt)
- Add background image and gradient scrim as the base layer of the root `Box`.
- Set `Scaffold` container color to `Transparent`.

#### [MODIFY] [ForgotPasswordScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/forgotpassword/ui/ForgotPasswordScreen.kt)
- Replace `img_splash_bg` with the theme-aware background (`image_bg_dark`/`image_bg_light`).
- Update the gradient scrim to use the theme's background color instead of hardcoded `Color.Black`.
- Wrap in root `Box`.

#### [MODIFY] [OnboardingScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/onboarding/OnboardingScreen.kt)
- Add background image and gradient scrim.
- Set `Scaffold` container color to `Transparent`.

#### [MODIFY] [SplashScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/splash/SplashScreen.kt)
- Add background image and gradient scrim.
- Adjust existing gradient logic to be consistent with other screens.

## Verification Plan

### Manual Verification
- Deploy the app.
- Traverse the entire initial flow: Splash -> Theme Selection -> Onboarding -> Permission -> Login -> Register -> Forgot Password.
- Verify that the background remains consistent across all these transitions.
- Test in both Light and Dark themes.

### Automated Tests
- Run existing UI tests to ensure layout tags and navigation still work as expected.
- Screens updated: `splash_screen`, `theme_selection_screen`, `onboarding_screen`, `permission_screen`, `login_screen`, `register_screen`.
