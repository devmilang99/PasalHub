-- Pasal Hub: Supabase Schema and RLS Policies

-- Enable Vector extension for AI Search
CREATE EXTENSION IF NOT EXISTS vector;

-------------------------------------------------------------------------------
-- 1. Tables Definition
-------------------------------------------------------------------------------

-- Profiles Table (Extends Supabase Auth)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
    name TEXT,
    email TEXT,
    date_of_birth DATE,
    address TEXT,
    profile_image TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Products Table
-- Matches com.psl.pasalhub.core.database.data.ProductEntity
CREATE TABLE IF NOT EXISTS public.products (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    description TEXT,
    category TEXT,
    image TEXT, -- Column named 'image' as per ProductEntity
    embedding VECTOR(384), -- Adjusted for typical mobile embedding models
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Cart Table
-- Matches com.psl.pasalhub.dashboard.cart.sync.CartSyncWorker
CREATE TABLE IF NOT EXISTS public.cart (
    user_id UUID REFERENCES auth.users ON DELETE CASCADE,
    product_id INT REFERENCES public.products ON DELETE CASCADE,
    quantity INT DEFAULT 1,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, product_id)
);

-- Orders Table
-- Matches com.psl.pasalhub.dashboard.order.sync.OrderSyncWorker
CREATE TABLE IF NOT EXISTS public.orders (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE,
    total_amount DECIMAL(12, 2) NOT NULL,
    items_summary TEXT NOT NULL, -- Format: "Title x Qty, Title x Qty"
    status TEXT DEFAULT 'Placed', -- 'Placed', 'Packing', 'Shipping', 'Delivered', 'Completed', 'Cancelled'
    address TEXT,
    date BIGINT NOT NULL, -- Stored as Unix timestamp for sync compatibility
    quantity INT DEFAULT 1,
    seller TEXT DEFAULT 'Pasal Hub',
    cancelled_reason TEXT,
    rating INT DEFAULT 0,
    review TEXT,
    progress INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    -- Unique constraint for sync logic in OrderSyncWorker.kt
    UNIQUE (user_id, date)
);

-- Favorites Table
-- Matches com.psl.pasalhub.core.database.data.FavoriteEntity
CREATE TABLE IF NOT EXISTS public.favorites (
    user_id UUID REFERENCES auth.users ON DELETE CASCADE,
    product_id INT REFERENCES public.products ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, product_id)
);

-------------------------------------------------------------------------------
-- 2. Row Level Security (RLS)
-------------------------------------------------------------------------------

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cart ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- Products Policies
CREATE POLICY "Products are viewable by everyone" ON public.products
    FOR SELECT USING (TRUE);
-- Only Admin (Service Role) can modify products (No public policy for INSERT/UPDATE/DELETE)

-- Cart Policies
CREATE POLICY "Users can manage own cart" ON public.cart
    FOR ALL USING (auth.uid() = user_id);

-- Orders Policies
CREATE POLICY "Users can view own orders" ON public.orders
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can place orders" ON public.orders
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own orders" ON public.orders
    FOR UPDATE USING (auth.uid() = user_id);

-- Favorites Policies
CREATE POLICY "Users can manage own favorites" ON public.favorites
    FOR ALL USING (auth.uid() = user_id);

-------------------------------------------------------------------------------
-- 3. Functions and Triggers
-------------------------------------------------------------------------------

-- Automatically create a profile when a new user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, name)
    VALUES (NEW.id, NEW.email, NEW.raw_user_meta_data->>'name');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
