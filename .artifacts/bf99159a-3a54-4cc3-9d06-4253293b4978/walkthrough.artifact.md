# Supabase Schema Walkthrough for Pasal Hub

I have completed the research and generation of the Supabase schema and RLS policies required for the Pasal Hub Android application. The schema is designed to work seamlessly with the existing `Room` entities and `Supabase` sync workers in the codebase.

## Changes Summary

### 1. Database Schema
Created a comprehensive SQL script: [schema.sql](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/supabase/schema.sql)

This script includes:
- **Profiles**: Extended user data synced with Supabase Auth.
- **Products**: Full catalog support with `VECTOR` embeddings for AI Search compatibility.
- **Cart**: Synchronized cart items with composite primary keys for efficient upserts.
- **Orders**: Full lifecycle tracking (Placing, Shipping, Delivered, etc.) with unique constraints for offline-first sync.
- **Favorites**: Wishlist management.

### 2. Security (RLS)
Implemented Row Level Security policies for all tables:
- **Public access** for products.
- **Private access** for user-specific data (Cart, Orders, Favorites, Profiles).
- **Automated Profile Creation**: A PostgreSQL trigger automatically creates a entry in the `profiles` table when a new user signs up via Supabase Auth.

## Integration Details

- **Sync Compatibility**: The column names and data types (e.g., `BIGINT` for dates, `DECIMAL` for prices) precisely match the expectations in `CartSyncWorker.kt`, `OrderSyncWorker.kt`, and `SupabaseSyncWorker.kt`.
- **AI Search Ready**: Added the `pgvector` extension and `embedding` column to the `products` table to support the `VectorSearchRepository` and `VisualSearchEngine`.

## Next Steps
To apply this schema:
1. Log in to your [Supabase Dashboard](https://app.supabase.com).
2. Navigate to the **SQL Editor**.
3. Copy the contents of [schema.sql](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/supabase/schema.sql) and run it.
4. Ensure the `pgvector` extension is enabled in your Supabase project (the script attempts to do this automatically).

> [!TIP]
> If you are using Supabase CLI, you can place this file in your `supabase/migrations` folder to manage it via version control.
