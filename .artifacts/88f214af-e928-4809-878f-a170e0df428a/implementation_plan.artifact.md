# Implementation Plan - Remove TFLite Related Features

The goal is to remove all TFLite-related features, specifically the `mobile_clip` model and its associated classes (Visual Search engine, Vector Search repository, and the background worker that generates embeddings).

## User Review Required

> [!IMPORTANT]
> This will permanently remove the **Visual Search** capability from the app. Text-based AI search (powered by Gemini) will remain functional.

## Proposed Changes

### [Component] AI & Search

#### [DELETE] [mobile_clip.tflite](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/assets/mobile_clip.tflite)
The heavy model file.

#### [DELETE] [VisualSearchEngine.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/ai/data/VisualSearchEngine.kt)
The core TFLite engine.

#### [DELETE] [VectorSearchRepository.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/ai/data/VectorSearchRepository.kt)
Repository for finding similar products using embeddings.

#### [DELETE] [VectorSearchRepositoryTest.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/test/java/com/psl/pasalhub/ai/data/VectorSearchRepositoryTest.kt)
Unit tests for the vector search repository.

#### [MODIFY] [AiSearchViewModel.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/ai/presentation/AiSearchViewModel.kt)
- Remove `VisualSearchEngine` and `VectorSearchRepository` dependencies.
- Remove `performVisualSearch` function.
- Remove `isModelReady` state.

#### [MODIFY] [AISearchScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/ai/presentation/AISearchScreen.kt)
- Remove Camera and Gallery launchers for visual search.
- Remove `showImageSourceDialog` and its trigger from the search bar.
- Remove `isModelReady` check from the search bar.

---

### [Component] Data & Sync

#### [DELETE] [VisualEmbeddingWorker.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/dashboard/products/sync/VisualEmbeddingWorker.kt)
Worker that extracted embeddings for products.

#### [MODIFY] [SupabaseSyncWorker.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/dashboard/products/sync/SupabaseSyncWorker.kt)
Remove the call to enqueue `VisualEmbeddingWorker`.

#### [MODIFY] [ProductDto.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/networking/remote/ProductDto.kt)
Remove the `embedding` field and update `equals`/`hashCode`.

#### [MODIFY] [ProductEntity.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/database/data/ProductEntity.kt)
Remove the `embedding` column and update `equals`/`hashCode`.

#### [MODIFY] [ProductDao.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/database/data/ProductDao.kt)
Remove `getProductsWithoutEmbedding` and `updateProductEmbedding` queries.

---

### [Component] Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/gradle/libs.versions.toml)
Remove TFLite related versions and libraries.

#### [MODIFY] [build.gradle.kts](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/build.gradle.kts)
Remove TFLite library dependency.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project builds successfully without TFLite.
- Run unit tests for `AiSearchViewModel` (if any) to ensure text search still works.

### Manual Verification
- Open the AI Search screen and verify that the camera icon is gone.
- Verify that text-based search still works correctly.
- Check logs during product sync to ensure no attempts are made to extract embeddings.
