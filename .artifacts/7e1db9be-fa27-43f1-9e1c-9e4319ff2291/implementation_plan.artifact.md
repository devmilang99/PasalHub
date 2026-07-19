# Fix Stuck Loading State and Multiple API Calls

The application is stuck in a loading state even though API data is received. This is caused by significant data mismatches between the Supabase schemas and the local Room entities/Kotlin classes. These mismatches lead to `SerializationException` during decoding, which triggers worker retries (multiple API calls) while the database remains empty.

## User Review Required

> [!IMPORTANT]
> The Supabase schemas for `products`, `orders`, `favorites`, and `cart` show that many fields can be `NULL` and that the remote tables contain columns (like `created_at`, `updated_at`, `user_id`) that are either missing or differently named in our local `Entities.kt`.
>
> I will configure the Supabase client to ignore unknown keys globally, which is the most robust fix for these discrepancies. I also need to adjust the local entities to handle nulls for fields that the server might not provide.

## Proposed Changes

### Core Data & Networking

#### [MODIFY] [SupabaseModule.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/di/SupabaseModule.kt)
- **CRITICAL**: Configure `SupabaseClient` with a custom `Json` instance: `Json { ignoreUnknownKeys = true; coerceInputValues = true }`. This prevents crashes when Supabase returns columns we haven't defined locally (like `created_at`).

#### [MODIFY] [ProductEntity.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/database/data/ProductEntity.kt) & [ProductDto.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/networking/remote/ProductDto.kt)
- Make `description`, `category`, and `image` nullable (`String?`) to match the Supabase schema.

#### [MODIFY] [Entities.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/core/database/data/Entities.kt)
- **OrderEntity**: Make fields like `itemsSummary`, `status`, and `address` nullable.
- **FavoriteEntity**: Ensure it can be decoded from the remote schema (user_id is UUID string, product_id is Int).
- **CartItem**: Note that the remote `cart` table is very different from the local `CartItem`. I will ensure the sync logic handles this (likely by fetching product details separately or adjusting the sync worker).

---

### Sync & Repository

#### [MODIFY] [SupabaseSyncWorker.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/dashboard/products/sync/SupabaseSyncWorker.kt)
- Add explicit logging for `SerializationException` to pinpoint any remaining field mismatches.

#### [MODIFY] [ProductRepository.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/dashboard/products/repository/ProductRepository.kt)
- Update `toDto()` to handle nullable fields safely.

---

### ViewModels & UI

#### [MODIFY] [HomeScreen.kt](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/app/src/main/java/com/psl/pasalhub/dashboard/home/ui/HomeScreen.kt)
- Update `ProductCardItem` to handle null images or titles gracefully.

## Verification Plan

### Automated Tests
- Build the project to verify that all type changes (especially nullability) are handled in the UI.

### Manual Verification
- Check Logcat for "Product sync successful".
- Verify that the Home screen correctly displays products after the loading state.
- Verify that clicking "Add to Cart" or "Favorite" doesn't crash if the remote response has extra fields.
