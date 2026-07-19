# Implementation Plan: Enhancing Gemini Search with Real Product Data

This plan aims to update the Gemini function calling flow so that the model can see and describe the products it finds, rather than just returning a generic "success" message.

## User Review Required

> [!NOTE]
> I will refactor the filtering logic in `AiSearchViewModel` into a reusable helper function. This ensures that the UI and the AI model always see the same results for a given set of search parameters.

## Proposed Changes

### AI Presentation Layer

#### [MODIFY] [AiSearchViewModel.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/example/ai/presentation/AiSearchViewModel.kt)

1.  **Extract Filtering Logic**: Create a private function `applySearchFilters(products: List<ProductDto>, fields: SearchFields?): List<ProductDto>` that encapsulates the current filtering logic (keywords, color, price, brand, rating).
2.  **Update `aiProductsState`**: Use the new `applySearchFilters` function within the `flatMapLatest` flow.
3.  **Enhance `handleFunctionCall`**:
    - For `search_products`:
        - Fetch products from the repository (handling the Flow/Resource structure).
        - Apply `applySearchFilters`.
        - Sort the results based on `sort_by`.
        - Build a `JsonObject` response containing a summary of the top 5 products (id, title, price, category).
        - Update `_aiSearchIntent.value` to sync the UI.

## Verification Plan

### Automated Tests
- I will verify the build to ensure no syntax errors were introduced.

### Manual Verification
- **Search Query**: Ask Gemini for "Red shoes under $50".
- **Expected Behavior**: Gemini should respond with specific details like "I found several options for you, including [Product Name] for $[Price]."
- **UI Sync**: The product list on the screen should match the items Gemini describes.
