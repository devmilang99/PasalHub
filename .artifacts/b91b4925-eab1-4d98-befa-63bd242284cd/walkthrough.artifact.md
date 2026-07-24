# Walkthrough: LiteRT Namespace Fix

I have resolved the namespace conflict between `litert` and `litert-api` which was preventing the project from building.

## Changes Made

### Build Configuration

#### [app/build.gradle.kts](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
- Modified the `litert` dependency to exclude `litert-api`.
- This resolves the "Duplicate Namespace" error in Android Gradle Plugin 9.x+ while retaining the functionality provided by the LiteRT implementation.

```kotlin
  implementation(libs.litert) {
    exclude(group = "com.google.ai.edge.litert", module = "litert-api")
  }
```

## Verification Results

### Automated Tests
- Executed `./gradlew :app:assembleDebug`.
- **Result:** Build finished successfully.

> [!NOTE]
> This is a known packaging issue in LiteRT 2.1.6. Excluding the API AAR works because the implementation artifact typically includes the necessary classes, and the conflict is specifically caused by the `AndroidManifest.xml` in the API AAR.
