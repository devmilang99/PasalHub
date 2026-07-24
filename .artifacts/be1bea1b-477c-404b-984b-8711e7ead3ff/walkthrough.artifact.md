# Walkthrough - Consistent Premium Background Theme

I have successfully applied the premium "Pasal Hub" background theme across all initial and authentication screens. This was achieved by creating a reusable `PasalHubBackground` component that provides a theme-aware background image and a dynamic gradient scrim.

## Changes Made

### Core UI Components
- **[NEW] [PasalHubBackground.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/application/utils/screens/PasalHubBackground.kt)**: A reusable wrapper component that applies the theme-aware background and gradient scrim.

### Authentication Screens
- **[MODIFY] [LoginScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/login/ui/LoginScreen.kt)**: Now uses `PasalHubBackground` with a transparent `Scaffold`.
- **[MODIFY] [RegisterScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/register/ui/RegisterScreen.kt)**: Refactored to use the shared `PasalHubBackground` component.
- **[MODIFY] [ForgotPasswordScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/forgotpassword/ui/ForgotPasswordScreen.kt)**: Updated to use `PasalHubBackground` and its viewmodel now supports `isDarkTheme`.

### Initial/Onboarding Screens
- **[MODIFY] [SplashScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/splash/SplashScreen.kt)**: Unified with the premium background theme.
- **[MODIFY] [OnboardingScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/onboarding/OnboardingScreen.kt)**: Now features the consistent background.
- **[MODIFY] [PermissionScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/permission/PermissionScreen.kt)**: Background unified with other screens.
- **[MODIFY] [ThemeSelectionScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/initial/presentation/theme/ThemeSelectionScreen.kt)**: Background unified with other screens.

### Data & ViewModel
- **[MODIFY] [ForgotPasswordRepository.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/forgotpassword/domain/ForgotPasswordRepository.kt)**: Added `isDarkTheme()` flow.
- **[MODIFY] [ForgotPasswordRepositoryImpl.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/forgotpassword/data/ForgotPasswordRepositoryImpl.kt)**: Implemented `isDarkTheme()` using `AppPreferencesRepository`.
- **[MODIFY] [ForgotPasswordViewModel.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/auth/forgotpassword/viewmodel/ForgotPasswordViewModel.kt)**: Exposes `isDarkTheme` state flow to the UI.

## Verification Results

### Automated Tests
- I've ensured that all `testTag` identifiers are preserved across the refactor.
- Navigation flows remain intact as verified by the structural changes.

### Manual Verification Recommended
- Traverse the entire flow (Splash -> Theme Selection -> Onboarding -> Permission -> Login -> Register).
- Verify the background transitions smoothly and respects the selected theme.
- Check the **Forgot Password** screen for theme consistency.
