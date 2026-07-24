# Implementation Plan - Fix Visual Search Model Asset Loading

The application fails to initialize the `VisualSearchEngine` because the required TFLite model file `mobile_clip.tflite` is located in the project root's `assets/` folder, which is not included in the application's assets by default.

Instead of moving the large (400MB) binary file, I will configure the Android Gradle plugin to include the root `assets/` folder in the application's assets.

## User Review Required

> [!NOTE]
> I am choosing to update the Gradle configuration to include the existing root `assets` folder rather than moving the model file. This is safer for large binary files and maintains your current directory structure.

## Proposed Changes

### [Component Name] Build Configuration

#### [MODIFY] [build.gradle.kts](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/app/build.gradle.kts)
- Add `sourceSets` configuration to include `../assets` in the `main` source set's assets.

### [Component Name] AI Search Engine

#### [MODIFY] [VisualSearchEngine.kt](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/app/src/main/java/com/example/ai/data/VisualSearchEngine.kt)
- Update the normalization logic to use CLIP-specific mean and standard deviation for better search accuracy, as suggested in the project's research notes.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build configuration is correct.
- Verify that the model is accessible by checking Logcat for initialization success logs.

### Manual Verification
- Deploy the app and verify the `VisualSearchEngine` tag in Logcat shows success.
