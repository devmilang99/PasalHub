# Fix AI Search Interaction and Visual Search Visibility

The user is unable to give instructions to the AI because the search text field is disabled when the visual search model is not ready. Additionally, the visual search selector (camera icon) is hidden for the same reason. This is caused by a missing `mobile_clip.tflite` model file and overly restrictive UI logic that couples text search readiness with visual model initialization.

## User Review Required

> [!IMPORTANT]
> The visual search feature requires a TFLite model file (`mobile_clip.tflite`) in the `assets` folder. Since this file is currently missing, visual search will remain disabled even after the UI fix, but text-based instructions will become functional.

## Proposed Changes

### [Component] AI Search Presentation

I will decouple the text search interaction from the visual model readiness. This ensures that users can still use Gemini-powered text search even if the visual search engine fails to initialize.

#### [MODIFY] [AISearchScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/ai/presentation/AISearchScreen.kt)

- Update `AISearchScreen` to pass both `isAiProcessing` and `isModelReady` to `SearchInputBar`.
- Refactor `SearchInputBar` to:
    - Enable the text field as long as the AI isn't currently processing a request.
    - Show/Enable the camera icon only when the visual search model is actually ready.
    - Maintain the pulsing border effect only during active AI processing.

## Verification Plan

### Manual Verification
1.  **Verify Text Search**: Open the AI Search screen. The text field should now be enabled despite the missing model. Type a query and verify it sends correctly.
2.  **Verify Visual Search Selector**: Observe that the camera icon is correctly hidden (since the model is missing) but doesn't prevent text input.
3.  **Verify Processing State**: Ensure the text field and send button correctly disable during an active search (`isAiProcessing == true`).
