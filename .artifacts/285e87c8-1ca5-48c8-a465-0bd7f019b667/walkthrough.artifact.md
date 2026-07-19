# Walkthrough - Fixing Gemini AI SDK Build Errors

I have fixed the build errors related to the `com.google.ai.client.generativeai` SDK upgrade (v0.9.0). The errors were caused by breaking changes in the `FunctionDeclaration` API and the `FunctionCallPart`/`FunctionResponsePart` types.

## Changes Made

### Gemini Search Router

#### [GeminiSearchRouter.kt](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/app/src/main/java/com/example/ai/data/GeminiSearchRouter.kt)

- **Migrated to `defineFunction`**: Switched from the `FunctionDeclaration` constructor to the `defineFunction` helper, which is the recommended way to define tools in the new SDK version.
- **Fixed `Schema` API Usage**:
    - Renamed `Schema.string` to `Schema.str`.
    - Renamed `Schema.integer` to `Schema.int`.
    - Added the required `name` parameter to all `Schema` factory calls.
- **Updated `parameters` format**: Changed the parameters definition from a `Map` to a `List<Schema>`, as required by the new API.
- **Added `requiredParameters`**: Explicitly marked `product_id` as a required parameter for the `get_product_details` tool.

### AI Search View Model

#### [AiSearchViewModel.kt](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/app/src/main/java/com/example/ai/presentation/AiSearchViewModel.kt)

- **Updated `FunctionCallPart` Handling**: Refactored argument parsing to handle `Map<String, String?>` instead of the previous `JsonElement` based structure. Used `toDoubleOrNull()` and `toIntOrNull()` for numeric parameters.
- **Updated `FunctionResponsePart` Handling**: Switched from `kotlinx.serialization`'s `JsonObject` to `org.json.JSONObject` for returning tool results, as required by the `FunctionResponsePart` constructor in v0.9.0.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin`
- **Result**: `Build finished successfully.`

### Manual Verification
- The project now compiles correctly with the latest Gemini AI SDK.
