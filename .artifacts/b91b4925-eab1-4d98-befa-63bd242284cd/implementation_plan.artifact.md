# Fix LiteRT Namespace Conflict

The project is experiencing a build failure because multiple LiteRT dependencies (`litert` and `litert-api`) share the same namespace `com.google.ai.edge.litert`. This is strictly prohibited by Android Gradle Plugin 9.0+.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
- Add a dependency exclusion for `litert-api` from the `litert` dependency. Since `litert` (the implementation) often bundles or requires these classes, but the `litert-api` AAR causes a manifest collision, excluding the AAR and relying on the implementation artifact (which typically contains the classes) is a common workaround.
- Alternatively, if `litert-api` is required for compilation but `litert` is required for runtime, we might need to handle them differently. However, the most direct fix for the namespace collision is to ensure only one artifact with that namespace is present in the build.

```kotlin
  implementation(libs.litert) {
      exclude(group = "com.google.ai.edge.litert", module = "litert-api")
  }
```

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build succeeds without namespace errors.
