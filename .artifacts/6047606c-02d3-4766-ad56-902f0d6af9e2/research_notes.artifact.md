# Visual Search Model Selection & Implementation Guide

The current visual search implementation in `VisualSearchEngine.kt` is designed for a **MobileCLIP** model but is missing the required `.tflite` file. This guide provides recommendations on which model to use and how to correctly integrate it.

## Recommended Model: MobileCLIP-S0

For an Android e-commerce application like **PasalHub**, I recommend using **MobileCLIP-S0**.

### Why MobileCLIP-S0?
*   **Ultra-Lightweight**: It is the smallest variant in the Apple MobileCLIP family, designed for maximum speed on mobile devices.
*   **512-Dimension Embeddings**: It produces a 512-float vector, which perfectly matches the current `VectorSearchRepository` mock data and simulation logic.
*   **LiteRT (TFLite) Compatibility**: It can be easily converted to or found in `.tflite` format for use with the Google LiteRT library already in your project.

### Where to find it?
You can find pre-converted versions on **Hugging Face**. Search for repositories like `anton96vice/mobileclip2_tflite` or similar TFLite conversions of MobileCLIP.

## Implementation Steps

### 1. File Placement
1.  Navigate to `app/src/main/` in your project.
2.  If it doesn't exist, create a folder named `assets`.
3.  Place your downloaded model file inside and rename it exactly to `mobile_clip.tflite`.
    *   Path: `app/src/main/assets/mobile_clip.tflite`

### 2. Update Normalization Logic
MobileCLIP models typically require specific normalization (ImageNet mean/std). The current `NormalizeOp(0f, 255f)` might not be accurate for the best search results.

In [VisualSearchEngine.kt](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/app/src/main/java/com/example/ai/data/VisualSearchEngine.kt), consider updating the `ImageProcessor` as follows:

```kotlin
// Approximate CLIP Normalization (ImageNet)
val CLIP_MEAN = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)
val CLIP_STD = floatArrayOf(0.26862954f, 0.26130258f, 0.2757771f)

val imageProcessor = ImageProcessor.Builder()
    .add(ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
    // Formula: (pixel - mean) / std
    // Since we need to scale to [0,1] then apply CLIP normalization:
    .add(NormalizeOp(
        CLIP_MEAN.map { it * 255f }.toFloatArray(),
        CLIP_STD.map { it * 255f }.toFloatArray()
    ))
    .build()
```

### 3. Verify Embedding Dimensions
The `VectorSearchRepository` generates mock embeddings of size **512**. If you choose a different model (like MobileCLIP-B) that outputs a different size (e.g., 768 or 1024), you MUST update the `generateMockEmbedding` function in [VectorSearchRepository.kt](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/app/src/main/java/com/example/ai/data/VectorSearchRepository.kt#L64) to match.

> [!TIP]
> Use the **GPU Accelerator** as currently configured in `VisualSearchEngine.kt`. MobileCLIP-S0 is highly optimized for mobile GPUs and will provide near-instant results.
