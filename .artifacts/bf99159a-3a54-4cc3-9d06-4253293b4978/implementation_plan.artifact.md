# Supabase Schema Implementation Plan for Pasal Hub

This plan outlines the creation of a robust Supabase schema and RLS policies based on the features identified in the Pasal Hub application.

## User Review Required

> [!IMPORTANT]
> The `orders` table uses a `date` column for uniqueness in the current sync logic (`OrderSyncWorker.kt`). In a production environment, using a dedicated `remote_id` or `uuid` is recommended to avoid collisions. I will include a `uuid` primary key and keep the `date` field for sync compatibility.

## Proposed Changes

### Database Schema

#### [NEW] [schema.sql](file:///D:/For%20Portfolio/Android%20Porfolio%20Projects/PasalHub/supabase/schema.sql)
Create a comprehensive SQL file containing:
- Table definitions (`profiles`, `products`, `cart`, `orders`, `favorites`).
- Foreign key constraints.
- RLS Policies for each table.
- Indexing for performance.

### Components Analysis

#### Profiles
- Linked to Supabase Auth `users` table via a trigger or manual insertion.
- Fields: `id` (UUID), `name`, `email`, `date_of_birth`, `address`, `profile_image`.

#### Products
- Stores the catalog.
- Fields: `id` (Serial), `title`, `price`, `description`, `category`, `image_url`.
- Support for vector embeddings if AI search is enabled.

#### Cart
- Junction table between Users and Products.
- Fields: `user_id`, `product_id`, `quantity`.
- Sync logic: `onConflict = "user_id, product_id"`.

#### Orders
- Comprehensive order tracking.
- Fields: `id` (UUID/Serial), `user_id`, `total_amount`, `status`, `address`, `items_summary`, `date`, `quantity`, `seller`, `cancelled_reason`, `rating`, `review`, `progress`.
- Sync logic: `onConflict = "user_id, date"`.

#### Favorites
- Simple wishlist.
- Fields: `user_id`, `product_id`.

## Verification Plan

### Automated Tests
- I will verify the SQL syntax matches Supabase's PostgreSQL flavor.
- Ensure all fields mentioned in `Entities.kt`, `CartSyncWorker.kt`, and `OrderSyncWorker.kt` are mapped correctly.

### Manual Verification
- Review the schema against the `ProductDetailScreen` and `OrderComponents` to ensure all UI data is represented.
