-- =============================================================================
-- VTT CABS - Complete Supabase Schema
-- =============================================================================
-- Run this script in your Supabase SQL Editor
-- https://supabase.com/dashboard/project/qqamfbxqmncgjfxqnfvj/sql
-- =============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- TABLES
-- =============================================================================

-- 1. PROFILES TABLE (extends auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    email TEXT,
    full_name TEXT,
    phone TEXT,
    avatar_url TEXT,
    role TEXT DEFAULT 'customer' CHECK (role IN ('customer', 'driver', 'admin')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. VEHICLES TABLE
CREATE TABLE IF NOT EXISTS public.vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    vehicle_number TEXT NOT NULL UNIQUE,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('sedan', 'ertiga', 'suv', 'tempo')),
    vehicle_model TEXT NOT NULL,
    vehicle_color TEXT,
    seats INTEGER NOT NULL DEFAULT 4,
    is_available BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    image_url TEXT,
    rc_number TEXT,
    insurance_expiry DATE,
    pollution_cert_expiry DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. DRIVERS TABLE
CREATE TABLE IF NOT EXISTS public.drivers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE UNIQUE NOT NULL,
    license_number TEXT UNIQUE NOT NULL,
    license_expiry DATE,
    is_available BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    current_location_lat DOUBLE PRECISION,
    current_location_lng DOUBLE PRECISION,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    total_rides INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. BOOKINGS TABLE
CREATE TABLE IF NOT EXISTS public.bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL NOT NULL,
    driver_id UUID REFERENCES public.drivers(id) ON DELETE SET NULL,
    vehicle_id UUID REFERENCES public.vehicles(id) ON DELETE SET NULL,
    pickup_location TEXT NOT NULL,
    pickup_lat DOUBLE PRECISION,
    pickup_lng DOUBLE PRECISION,
    drop_location TEXT NOT NULL,
    drop_lat DOUBLE PRECISION,
    drop_lng DOUBLE PRECISION,
    pickup_date DATE NOT NULL,
    pickup_time TIME NOT NULL,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('sedan', 'ertiga', 'suv', 'tempo')),
    trip_type TEXT DEFAULT 'oneway' CHECK (trip_type IN ('oneway', 'roundtrip')),
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'in_progress', 'completed', 'cancelled')),
    estimated_distance DECIMAL(10, 2),
    estimated_time INTEGER,
    estimated_price DECIMAL(10, 2),
    final_price DECIMAL(10, 2),
    coupon_code TEXT,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. RIDES TABLE (actual ride tracking)
CREATE TABLE IF NOT EXISTS public.rides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES public.bookings(id) ON DELETE CASCADE UNIQUE NOT NULL,
    driver_id UUID REFERENCES public.drivers(id) ON DELETE SET NULL NOT NULL,
    vehicle_id UUID REFERENCES public.vehicles(id) ON DELETE SET NULL NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    actual_pickup_time TIMESTAMP WITH TIME ZONE,
    actual_drop_time TIMESTAMP WITH TIME ZONE,
    actual_distance DECIMAL(10, 2),
    actual_time INTEGER,
    actual_price DECIMAL(10, 2),
    pickup_lat DOUBLE PRECISION,
    pickup_lng DOUBLE PRECISION,
    drop_lat DOUBLE PRECISION,
    drop_lng DOUBLE PRECISION,
    status TEXT DEFAULT 'assigned' CHECK (status IN ('assigned', 'accepted', 'arrived', 'started', 'completed', 'cancelled')),
    otp TEXT,
    cancellation_reason TEXT,
    driver_rating INTEGER CHECK (driver_rating >= 1 AND driver_rating <= 5),
    driver_feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. PAYMENTS TABLE
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL NOT NULL,
    ride_id UUID REFERENCES public.rides(id) ON DELETE SET NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method TEXT DEFAULT 'cash' CHECK (payment_method IN ('cash', 'card', 'upi', 'wallet')),
    payment_status TEXT DEFAULT 'pending' CHECK (payment_status IN ('pending', 'completed', 'failed', 'refunded')),
    transaction_id TEXT,
    payment_gateway TEXT,
    gateway_response JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. WALLETS TABLE
CREATE TABLE IF NOT EXISTS public.wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE UNIQUE NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 8. WALLET TRANSACTIONS TABLE
CREATE TABLE IF NOT EXISTS public.wallet_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id UUID REFERENCES public.wallets(id) ON DELETE CASCADE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_type TEXT NOT NULL CHECK (transaction_type IN ('credit', 'debit')),
    transaction_method TEXT DEFAULT 'payment' CHECK (transaction_method IN ('payment', 'refund', 'topup', 'bonus', 'withdrawal')),
    description TEXT,
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =============================================================================
-- INDEXES
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);
CREATE INDEX IF NOT EXISTS idx_vehicles_driver ON public.vehicles(driver_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_type ON public.vehicles(vehicle_type);
CREATE INDEX IF NOT EXISTS idx_drivers_user ON public.drivers(user_id);
CREATE INDEX IF NOT EXISTS idx_drivers_available ON public.drivers(is_available);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON public.bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_driver ON public.bookings(driver_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON public.bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_date ON public.bookings(pickup_date);
CREATE INDEX IF NOT EXISTS idx_rides_booking ON public.rides(booking_id);
CREATE INDEX IF NOT EXISTS idx_rides_driver ON public.rides(driver_id);
CREATE INDEX IF NOT EXISTS idx_rides_status ON public.rides(status);
CREATE INDEX IF NOT EXISTS idx_payments_booking ON public.payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_user ON public.payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON public.payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet ON public.wallet_transactions(wallet_id);

-- =============================================================================
-- FUNCTIONS
-- =============================================================================

-- Function to handle new user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, phone)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', ''),
        COALESCE(NEW.raw_user_meta_data->>'phone', '')
    );
    
    -- Create wallet for new user
    INSERT INTO public.wallets (user_id, balance)
    VALUES (NEW.id, 0.00);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on signup
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- TRIGGERS FOR UPDATED_AT
-- =============================================================================

CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

CREATE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON public.vehicles
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

CREATE TRIGGER update_drivers_updated_at
    BEFORE UPDATE ON public.drivers
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

CREATE TRIGGER update_rides_updated_at
    BEFORE UPDATE ON public.rides
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON public.payments
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

CREATE TRIGGER update_wallets_updated_at
    BEFORE UPDATE ON public.wallets
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- =============================================================================
-- ROW LEVEL SECURITY (RLS)
-- =============================================================================

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.drivers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rides ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallet_transactions ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- RLS POLICIES FOR PROFILES
-- =============================================================================

-- Users can view their own profile
CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

-- Users can update their own profile
CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- Anyone can view driver profiles
CREATE POLICY "Anyone can view driver profiles"
    ON public.profiles FOR SELECT
    USING (role = 'driver' AND is_active = true);

-- Admin can view all profiles
CREATE POLICY "Admins can view all profiles"
    ON public.profiles FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Admin can update any profile
CREATE POLICY "Admins can update any profile"
    ON public.profiles FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- =============================================================================
-- RLS POLICIES FOR VEHICLES
-- =============================================================================

-- Anyone can view available vehicles
CREATE POLICY "Anyone can view available vehicles"
    ON public.vehicles FOR SELECT
    USING (is_available = true);

-- Drivers can view their own vehicles
CREATE POLICY "Drivers can view own vehicles"
    ON public.vehicles FOR SELECT
    USING (driver_id IN (SELECT user_id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can insert their own vehicles
CREATE POLICY "Drivers can insert own vehicles"
    ON public.vehicles FOR INSERT
    WITH CHECK (driver_id IN (SELECT id FROM public.profiles WHERE id = auth.uid()));

-- Drivers can update their own vehicles
CREATE POLICY "Drivers can update own vehicles"
    ON public.vehicles FOR UPDATE
    USING (driver_id IN (SELECT user_id FROM public.drivers WHERE user_id = auth.uid()));

-- Admin can view all vehicles
CREATE POLICY "Admins can view all vehicles"
    ON public.vehicles FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Admin can manage all vehicles
CREATE POLICY "Admins can manage all vehicles"
    ON public.vehicles FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- =============================================================================
-- RLS POLICIES FOR DRIVERS
-- =============================================================================

-- Anyone can view verified drivers
CREATE POLICY "Anyone can view verified drivers"
    ON public.drivers FOR SELECT
    USING (is_verified = true AND is_available = true);

-- Drivers can view their own driver profile
CREATE POLICY "Drivers can view own profile"
    ON public.drivers FOR SELECT
    USING (user_id = auth.uid());

-- Users can insert as driver (signup as driver)
CREATE POLICY "Users can create driver profile"
    ON public.drivers FOR INSERT
    WITH CHECK (user_id = auth.uid());

-- Drivers can update their own profile
CREATE POLICY "Drivers can update own profile"
    ON public.drivers FOR UPDATE
    USING (user_id = auth.uid());

-- Admin can view all drivers
CREATE POLICY "Admins can view all drivers"
    ON public.drivers FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Admin can manage all drivers
CREATE POLICY "Admins can manage all drivers"
    ON public.drivers FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- =============================================================================
-- RLS POLICIES FOR BOOKINGS
-- =============================================================================

-- Users can view their own bookings
CREATE POLICY "Users can view own bookings"
    ON public.bookings FOR SELECT
    USING (user_id = auth.uid());

-- Users can create bookings
CREATE POLICY "Users can create bookings"
    ON public.bookings FOR INSERT
    WITH CHECK (user_id = auth.uid());

-- Users can update their pending bookings
CREATE POLICY "Users can update own pending bookings"
    ON public.bookings FOR UPDATE
    USING (user_id = auth.uid() AND status = 'pending');

-- Drivers can view assigned bookings
CREATE POLICY "Drivers can view assigned bookings"
    ON public.bookings FOR SELECT
    USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can update assigned bookings
CREATE POLICY "Drivers can update assigned bookings"
    ON public.bookings FOR UPDATE
    USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Admin can view all bookings
CREATE POLICY "Admins can view all bookings"
    ON public.bookings FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Admin can manage all bookings
CREATE POLICY "Admins can manage all bookings"
    ON public.bookings FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- =============================================================================
-- RLS POLICIES FOR RIDES
-- =============================================================================

-- Users can view rides for their bookings
CREATE POLICY "Users can view their booking rides"
    ON public.rides FOR SELECT
    USING (
        booking_id IN (
            SELECT id FROM public.bookings WHERE user_id = auth.uid()
        )
    );

-- Drivers can view their rides
CREATE POLICY "Drivers can view their rides"
    ON public.rides FOR SELECT
    USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can create rides
CREATE POLICY "Drivers can create rides"
    ON public.rides FOR INSERT
    WITH CHECK (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can update their rides
CREATE POLICY "Drivers can update their rides"
    ON public.rides FOR UPDATE
    USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Admin can view all rides
CREATE POLICY "Admins can view all rides"
    ON public.rides FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Admin can manage all rides
CREATE POLICY "Admins can manage all rides"
    ON public.rides FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- =============================================================================
-- RLS POLICIES FOR PAYMENTS
-- =============================================================================

-- Users can view their own payments
CREATE POLICY "Users can view own payments"
    ON public.payments FOR SELECT
    USING (user_id = auth.uid());

-- Users can create payments
CREATE POLICY "Users can create payments"
    ON public.payments FOR INSERT
    WITH CHECK (user_id = auth.uid());

-- Users can update their pending payments
CREATE POLICY "Users can update own pending payments"
    ON public.payments FOR UPDATE
    USING (user_id = auth.uid() AND payment_status = 'pending');

-- Admin can view all payments
CREATE POLICY "Admins can view all payments"
    ON public.payments FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Admin can manage all payments
CREATE POLICY "Admins can manage all payments"
    ON public.payments FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- =============================================================================
-- RLS POLICIES FOR WALLETS
-- =============================================================================

-- Users can view their own wallet
CREATE POLICY "Users can view own wallet"
    ON public.wallets FOR SELECT
    USING (user_id = auth.uid());

-- Users can update their own wallet
CREATE POLICY "Users can update own wallet"
    ON public.wallets FOR UPDATE
    USING (user_id = auth.uid());

-- =============================================================================
-- RLS POLICIES FOR WALLET TRANSACTIONS
-- =============================================================================

-- Users can view their own wallet transactions
CREATE POLICY "Users can view own wallet transactions"
    ON public.wallet_transactions FOR SELECT
    USING (
        wallet_id IN (
            SELECT id FROM public.wallets WHERE user_id = auth.uid()
        )
    );

-- System can insert wallet transactions
CREATE POLICY "System can insert wallet transactions"
    ON public.wallet_transactions FOR INSERT
    WITH CHECK (
        wallet_id IN (SELECT id FROM public.wallets WHERE user_id = auth.uid())
    );

-- =============================================================================
-- GRANT PERMISSIONS
-- =============================================================================

-- Grant usage on schemas
GRANT USAGE ON SCHEMA public TO anon, authenticated;

-- Grant all on all tables to authenticated users
GRANT ALL ON public.profiles TO authenticated;
GRANT ALL ON public.vehicles TO authenticated;
GRANT ALL ON public.drivers TO authenticated;
GRANT ALL ON public.bookings TO authenticated;
GRANT ALL ON public.rides TO authenticated;
GRANT ALL ON public.payments TO authenticated;
GRANT ALL ON public.wallets TO authenticated;
GRANT ALL ON public.wallet_transactions TO authenticated;

-- Grant SELECT on all tables to anon (public)
GRANT SELECT ON public.profiles TO anon;
GRANT SELECT ON public.vehicles TO anon;
GRANT SELECT ON public.drivers TO anon;
GRANT SELECT ON public.bookings TO anon;
GRANT SELECT ON public.rides TO anon;
GRANT SELECT ON public.payments TO anon;
GRANT SELECT ON public.wallets TO anon;
GRANT SELECT ON public.wallet_transactions TO anon;

-- Grant execute on functions
GRANT EXECUTE ON FUNCTION public.handle_new_user() TO authenticated;
GRANT EXECUTE ON FUNCTION public.update_updated_at_column() TO authenticated;

-- =============================================================================
-- SAMPLE DATA (Optional - Uncomment to insert)
-- =============================================================================

-- Insert admin user (replace with your user ID after signup)
-- INSERT INTO public.profiles (id, email, full_name, role)
-- VALUES ('your-user-uuid-here', 'admin@vttcabs.com', 'Admin User', 'admin');

-- =============================================================================
-- FINISH
-- =============================================================================

-- Enable realtime for bookings (optional)
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.bookings;
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.rides;

-- Output success message
SELECT '✅ VTT CABS Schema Created Successfully!' AS status;
