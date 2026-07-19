# Optimization and Production Readiness Plan

This plan outlines the steps to optimize the Pasal Hub application, remove unnecessary code, and prepare it for production.

## User Review Required

> [!IMPORTANT]
> **Package Name Change**: I will be renaming the base package from `com.example` to `com.psl.pasalhub`. This is a significant change that affects every file in the project.
> **Dependency Removal**: I will be removing Retrofit and Moshi, as the project already uses Supabase (via Ktor) and Kotlinx Serialization, making them redundant.

## Proposed Changes

### 1. Cleanup of Unused Files & Boilerplate

- **[DELETE]** `SampleData.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/dashboard/products/repository/SampleData.kt): Unused development data.
- **[DELETE]** `ExampleRobolectricTest.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/test/java/com/example/ExampleRobolectricTest.kt): Boilerplate test.
- **[DELETE]** `GreetingScreenshotTest.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/test/java/com/example/GreetingScreenshotTest.kt): Boilerplate test.
- **[DELETE]** `FakeStoreApi.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/networking/remote/FakeStoreApi.kt): Redundant API interface.

### 2. Dependency Optimization

- **[MODIFY]** `build.gradle.kts` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts):
    - Remove Retrofit, Moshi, and their associated KSP/Converter dependencies.
    - Update `applicationId` to `com.psl.pasalhub`.
- **[MODIFY]** `NetworkModule.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/di/NetworkModule.kt): Remove Retrofit and Moshi providers.

### 3. Code Refactoring

- **[MODIFY]** `SearchRoutingModels.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/ai/domain/model/SearchRoutingModels.kt): Replace Moshi annotations with Kotlinx Serialization `@Serializable`.
- **[MODIFY]** `ProductDto.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/networking/remote/ProductDto.kt): Replace Moshi annotations with Kotlinx Serialization `@Serializable`.
- **[MODIFY]** `ProductRepository.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/dashboard/products/repository/ProductRepository.kt): Remove unused `FakeStoreApi` parameter.
- **[MODIFY]** `AppModule.kt` (file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/core/di/AppModule.kt): Remove `FakeStoreApi` usage.

### 4. Package Refactoring (Global)

- Rename all instances of `com.example` to `com.psl.pasalhub` across all Kotlin files, XML manifests, and Gradle scripts.
- Move files to the new directory structure: `app/src/main/java/com/psl/pasalhub/...`.

### 5. Production Polish

- **Logging**: Replace `Log.d/e/v` with a utility that only logs in debug builds, or remove them entirely where not needed.
- **Resource Shrinking**: Ensure `isMinifyEnabled` and `isShrinkResources` are set to `true` in `release` build type (Already done).

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project still builds after refactoring.
- Run existing unit tests (e.g., `VectorSearchRepositoryTest.kt`) after updating their package names.

### Manual Verification
- Deploy the app to a device/emulator to verify that the Supabase sync and AI search functionality still work correctly with the new package name and serialized models.
