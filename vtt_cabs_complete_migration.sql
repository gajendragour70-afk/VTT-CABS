-- =============================================================================
-- VTT CABS - Complete Production-Ready Database Migration
-- =============================================================================
-- Run this script in your Supabase SQL Editor
-- https://supabase.com/dashboard/project/qqamfbxqmncgjfxqnfvj/sql
-- =============================================================================

-- =============================================================================
-- PART 1: EXTENSIONS
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- PART 2: DROP EXISTING OBJECTS (Clean slate)
-- =============================================================================

-- Drop triggers in correct order
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP TRIGGER IF EXISTS update_profiles_updated_at ON public.profiles;
DROP TRIGGER IF EXISTS update_wallets_updated_at ON public.wallets;
DROP TRIGGER IF EXISTS update_vehicles_updated_at ON public.vehicles;
DROP TRIGGER IF EXISTS update_drivers_updated_at ON public.drivers;
DROP TRIGGER IF EXISTS update_bookings_updated_at ON public.bookings;
DROP TRIGGER IF EXISTS update_rides_updated_at ON public.rides;
DROP TRIGGER IF EXISTS update_payments_updated_at ON public.payments;
DROP TRIGGER IF EXISTS update_driver_stats_trigger ON public.rides;
DROP TRIGGER IF EXISTS update_booking_from_ride_trigger ON public.rides;

-- Drop functions
DROP FUNCTION IF EXISTS public.handle_new_user() CASCADE;
DROP FUNCTION IF EXISTS public.update_updated_at_column() CASCADE;
DROP FUNCTION IF EXISTS public.update_driver_stats() CASCADE;
DROP FUNCTION IF EXISTS public.update_booking_from_ride() CASCADE;

-- Drop tables in reverse foreign key order
DROP TABLE IF EXISTS public.wallet_transactions CASCADE;
DROP TABLE IF EXISTS public.payments CASCADE;
DROP TABLE IF EXISTS public.rides CASCADE;
DROP TABLE IF EXISTS public.bookings CASCADE;
DROP TABLE IF EXISTS public.drivers CASCADE;
DROP TABLE IF EXISTS public.vehicles CASCADE;
DROP TABLE IF EXISTS public.wallets CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;

-- =============================================================================
-- PART 3: CREATE ALL TABLES
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABLE 1: PROFILES (extends auth.users)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TABLE 2: WALLETS (user wallet balance)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE UNIQUE NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- -----------------------------------------------------------------------------
-- TABLE 3: VEHICLES (vehicle fleet)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TABLE 4: DRIVERS (driver profiles)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TABLE 5: BOOKINGS (customer booking requests)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TABLE 6: RIDES (active ride tracking)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TABLE 7: PAYMENTS (payment transactions)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- TABLE 8: WALLET TRANSACTIONS (transaction history)
-- -----------------------------------------------------------------------------
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
-- PART 4: CREATE INDEXES
-- =============================================================================

-- Profiles indexes
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);

-- Wallets indexes
CREATE INDEX IF NOT EXISTS idx_wallets_user ON public.wallets(user_id);

-- Vehicles indexes
CREATE INDEX IF NOT EXISTS idx_vehicles_driver ON public.vehicles(driver_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_type ON public.vehicles(vehicle_type);
CREATE INDEX IF NOT EXISTS idx_vehicles_available ON public.vehicles(is_available);

-- Drivers indexes
CREATE INDEX IF NOT EXISTS idx_drivers_user ON public.drivers(user_id);
CREATE INDEX IF NOT EXISTS idx_drivers_available ON public.drivers(is_available);
CREATE INDEX IF NOT EXISTS idx_drivers_verified ON public.drivers(is_verified);

-- Bookings indexes
CREATE INDEX IF NOT EXISTS idx_bookings_user ON public.bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_driver ON public.bookings(driver_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON public.bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_date ON public.bookings(pickup_date);
CREATE INDEX IF NOT EXISTS idx_bookings_vehicle_type ON public.bookings(vehicle_type);

-- Rides indexes
CREATE INDEX IF NOT EXISTS idx_rides_booking ON public.rides(booking_id);
CREATE INDEX IF NOT EXISTS idx_rides_driver ON public.rides(driver_id);
CREATE INDEX IF NOT EXISTS idx_rides_status ON public.rides(status);

-- Payments indexes
CREATE INDEX IF NOT EXISTS idx_payments_booking ON public.payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_user ON public.payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON public.payments(payment_status);

-- Wallet transactions indexes
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet ON public.wallet_transactions(wallet_id);

-- =============================================================================
-- PART 5: CREATE FUNCTIONS
-- =============================================================================

-- -----------------------------------------------------------------------------
-- FUNCTION: Auto-create profile and wallet on user signup
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- Create profile
    INSERT INTO public.profiles (id, email, full_name, phone)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', ''),
        COALESCE(NEW.raw_user_meta_data->>'phone', '')
    )
    ON CONFLICT (id) DO UPDATE SET
        email = EXCLUDED.email,
        updated_at = NOW();

    -- Create wallet for new user
    INSERT INTO public.wallets (user_id, balance)
    VALUES (NEW.id, 0.00)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- -----------------------------------------------------------------------------
-- FUNCTION: Auto-update updated_at timestamp
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- -----------------------------------------------------------------------------
-- FUNCTION: Update driver stats when ride completes
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.update_driver_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'completed' AND (OLD.status IS NULL OR OLD.status != 'completed') THEN
        UPDATE public.drivers
        SET 
            total_rides = total_rides + 1,
            rating = (
                SELECT COALESCE(AVG(driver_rating), 0)
                FROM public.rides
                WHERE driver_id = NEW.driver_id AND driver_rating IS NOT NULL
            )
        WHERE id = NEW.driver_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- -----------------------------------------------------------------------------
-- FUNCTION: Update booking status from ride status
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.update_booking_from_ride()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status IN ('assigned', 'accepted', 'arrived', 'started') THEN
        UPDATE public.bookings
        SET status = 'confirmed', updated_at = NOW()
        WHERE id = NEW.booking_id AND status = 'pending';
    ELSIF NEW.status = 'completed' THEN
        UPDATE public.bookings
        SET 
            status = 'completed',
            final_price = COALESCE(NEW.actual_price, bookings.final_price),
            updated_at = NOW()
        WHERE id = NEW.booking_id;
    ELSIF NEW.status = 'cancelled' THEN
        UPDATE public.bookings
        SET status = 'cancelled', updated_at = NOW()
        WHERE id = NEW.booking_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =============================================================================
-- PART 6: CREATE TRIGGERS
-- =============================================================================

-- Trigger: Create profile and wallet on new user signup
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Trigger: Updated_at for profiles
CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Updated_at for wallets
CREATE TRIGGER update_wallets_updated_at
    BEFORE UPDATE ON public.wallets
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Updated_at for vehicles
CREATE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON public.vehicles
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Updated_at for drivers
CREATE TRIGGER update_drivers_updated_at
    BEFORE UPDATE ON public.drivers
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Updated_at for bookings
CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Updated_at for rides
CREATE TRIGGER update_rides_updated_at
    BEFORE UPDATE ON public.rides
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Updated_at for payments
CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON public.payments
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger: Update driver stats on ride completion
CREATE TRIGGER update_driver_stats_trigger
    AFTER UPDATE ON public.rides
    FOR EACH ROW EXECUTE FUNCTION public.update_driver_stats();

-- Trigger: Update booking status from ride status
CREATE TRIGGER update_booking_from_ride_trigger
    AFTER UPDATE ON public.rides
    FOR EACH ROW EXECUTE FUNCTION public.update_booking_from_ride();

-- =============================================================================
-- PART 7: ENABLE ROW LEVEL SECURITY
-- =============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.drivers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rides ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallet_transactions ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- PART 8: CREATE RLS POLICIES
-- =============================================================================

-- -----------------------------------------------------------------------------
-- POLICIES FOR: profiles
-- -----------------------------------------------------------------------------

-- Users can view their own profile
CREATE POLICY "profiles_select_own" ON public.profiles
    FOR SELECT USING (auth.uid() = id);

-- Users can update their own profile
CREATE POLICY "profiles_update_own" ON public.profiles
    FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- Users can insert their own profile
CREATE POLICY "profiles_insert_own" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- Anyone can view driver profiles (for booking flow)
CREATE POLICY "profiles_select_drivers_public" ON public.profiles
    FOR SELECT USING (role = 'driver' AND is_active = true);

-- Admin can view all profiles
CREATE POLICY "profiles_select_all_admin" ON public.profiles
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- Admin can update any profile
CREATE POLICY "profiles_update_all_admin" ON public.profiles
    FOR UPDATE USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- -----------------------------------------------------------------------------
-- POLICIES FOR: wallets
-- -----------------------------------------------------------------------------

-- Users can view their own wallet
CREATE POLICY "wallets_select_own" ON public.wallets
    FOR SELECT USING (user_id = auth.uid());

-- Users can update their own wallet
CREATE POLICY "wallets_update_own" ON public.wallets
    FOR UPDATE USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());

-- Users can insert their own wallet
CREATE POLICY "wallets_insert_own" ON public.wallets
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- -----------------------------------------------------------------------------
-- POLICIES FOR: wallet_transactions
-- -----------------------------------------------------------------------------

-- Users can view their own wallet transactions
CREATE POLICY "wallet_transactions_select_own" ON public.wallet_transactions
    FOR SELECT USING (wallet_id IN (SELECT id FROM public.wallets WHERE user_id = auth.uid()));

-- Users can insert wallet transactions
CREATE POLICY "wallet_transactions_insert_own" ON public.wallet_transactions
    FOR INSERT WITH CHECK (wallet_id IN (SELECT id FROM public.wallets WHERE user_id = auth.uid()));

-- -----------------------------------------------------------------------------
-- POLICIES FOR: vehicles
-- -----------------------------------------------------------------------------

-- Anyone can view available verified vehicles
CREATE POLICY "vehicles_select_available" ON public.vehicles
    FOR SELECT USING (is_available = true AND is_verified = true);

-- Drivers can view their own vehicles
CREATE POLICY "vehicles_select_own" ON public.vehicles
    FOR SELECT USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can insert their own vehicles
CREATE POLICY "vehicles_insert_own" ON public.vehicles
    FOR INSERT WITH CHECK (driver_id IN (SELECT id FROM public.profiles WHERE id = auth.uid()));

-- Drivers can update their own vehicles
CREATE POLICY "vehicles_update_own" ON public.vehicles
    FOR UPDATE USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Admin can view all vehicles
CREATE POLICY "vehicles_select_all_admin" ON public.vehicles
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- Admin can manage all vehicles
CREATE POLICY "vehicles_manage_all_admin" ON public.vehicles
    FOR ALL USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- -----------------------------------------------------------------------------
-- POLICIES FOR: drivers
-- -----------------------------------------------------------------------------

-- Anyone can view verified available drivers
CREATE POLICY "drivers_select_verified" ON public.drivers
    FOR SELECT USING (is_verified = true AND is_available = true);

-- Drivers can view their own profile
CREATE POLICY "drivers_select_own" ON public.drivers
    FOR SELECT USING (user_id = auth.uid());

-- Users can create driver profile
CREATE POLICY "drivers_insert_own" ON public.drivers
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Drivers can update their own profile
CREATE POLICY "drivers_update_own" ON public.drivers
    FOR UPDATE USING (user_id = auth.uid());

-- Admin can view all drivers
CREATE POLICY "drivers_select_all_admin" ON public.drivers
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- Admin can manage all drivers
CREATE POLICY "drivers_manage_all_admin" ON public.drivers
    FOR ALL USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- -----------------------------------------------------------------------------
-- POLICIES FOR: bookings
-- -----------------------------------------------------------------------------

-- Users can view their own bookings
CREATE POLICY "bookings_select_own" ON public.bookings
    FOR SELECT USING (user_id = auth.uid());

-- Users can create bookings
CREATE POLICY "bookings_insert_own" ON public.bookings
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Users can update their pending bookings
CREATE POLICY "bookings_update_own_pending" ON public.bookings
    FOR UPDATE USING (user_id = auth.uid() AND status = 'pending');

-- Drivers can view assigned bookings
CREATE POLICY "bookings_select_driver" ON public.bookings
    FOR SELECT USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can update assigned bookings
CREATE POLICY "bookings_update_driver" ON public.bookings
    FOR UPDATE USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Admin can view all bookings
CREATE POLICY "bookings_select_all_admin" ON public.bookings
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- Admin can manage all bookings
CREATE POLICY "bookings_manage_all_admin" ON public.bookings
    FOR ALL USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- -----------------------------------------------------------------------------
-- POLICIES FOR: rides
-- -----------------------------------------------------------------------------

-- Users can view rides for their bookings
CREATE POLICY "rides_select_own_booking" ON public.rides
    FOR SELECT USING (booking_id IN (SELECT id FROM public.bookings WHERE user_id = auth.uid()));

-- Drivers can view their rides
CREATE POLICY "rides_select_driver" ON public.rides
    FOR SELECT USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can create rides
CREATE POLICY "rides_insert_driver" ON public.rides
    FOR INSERT WITH CHECK (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Drivers can update their rides
CREATE POLICY "rides_update_driver" ON public.rides
    FOR UPDATE USING (driver_id IN (SELECT id FROM public.drivers WHERE user_id = auth.uid()));

-- Admin can view all rides
CREATE POLICY "rides_select_all_admin" ON public.rides
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- Admin can manage all rides
CREATE POLICY "rides_manage_all_admin" ON public.rides
    FOR ALL USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- -----------------------------------------------------------------------------
-- POLICIES FOR: payments
-- -----------------------------------------------------------------------------

-- Users can view their own payments
CREATE POLICY "payments_select_own" ON public.payments
    FOR SELECT USING (user_id = auth.uid());

-- Users can create payments
CREATE POLICY "payments_insert_own" ON public.payments
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Users can update their pending payments
CREATE POLICY "payments_update_own_pending" ON public.payments
    FOR UPDATE USING (user_id = auth.uid() AND payment_status = 'pending');

-- Admin can view all payments
CREATE POLICY "payments_select_all_admin" ON public.payments
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- Admin can manage all payments
CREATE POLICY "payments_manage_all_admin" ON public.payments
    FOR ALL USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role = 'admin'));

-- =============================================================================
-- PART 9: GRANT PERMISSIONS
-- =============================================================================

-- Grant schema usage
GRANT USAGE ON SCHEMA public TO anon, authenticated;

-- Grant all on all tables to authenticated users
GRANT ALL ON public.profiles TO authenticated;
GRANT ALL ON public.wallets TO authenticated;
GRANT ALL ON public.vehicles TO authenticated;
GRANT ALL ON public.drivers TO authenticated;
GRANT ALL ON public.bookings TO authenticated;
GRANT ALL ON public.rides TO authenticated;
GRANT ALL ON public.payments TO authenticated;
GRANT ALL ON public.wallet_transactions TO authenticated;

-- Grant select on public tables to anon (for public reads)
GRANT SELECT ON public.profiles TO anon;
GRANT SELECT ON public.vehicles TO anon;
GRANT SELECT ON public.drivers TO anon;

-- Grant function execution
GRANT EXECUTE ON FUNCTION public.handle_new_user() TO authenticated;
GRANT EXECUTE ON FUNCTION public.update_updated_at_column() TO authenticated;
GRANT EXECUTE ON FUNCTION public.update_driver_stats() TO authenticated;
GRANT EXECUTE ON FUNCTION public.update_booking_from_ride() TO authenticated;

-- =============================================================================
-- PART 10: BACKFILL EXISTING DATA
-- =============================================================================

-- Backfill profiles from existing auth.users
INSERT INTO public.profiles (id, email, full_name, phone, role)
SELECT 
    id,
    email,
    COALESCE(raw_user_meta_data->>'full_name', ''),
    COALESCE(raw_user_meta_data->>'phone', ''),
    'customer'
FROM auth.users
WHERE NOT EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.users.id)
ON CONFLICT (id) DO NOTHING;

-- Backfill wallets for existing profiles
INSERT INTO public.wallets (user_id, balance)
SELECT id, 0.00 FROM public.profiles
WHERE NOT EXISTS (SELECT 1 FROM public.wallets WHERE user_id = public.profiles.id)
ON CONFLICT (user_id) DO NOTHING;

-- =============================================================================
-- PART 11: VERIFICATION QUERIES
-- =============================================================================

SELECT '-- ============================================' AS verification_section;
SELECT '-- VTT CABS Database Verification Results' AS verification_section;
SELECT '-- ============================================' AS verification_section;

-- Check profiles
SELECT '1. Profiles table:' AS check;
SELECT COUNT(*) AS total_profiles FROM public.profiles;
SELECT id, email, full_name, role FROM public.profiles LIMIT 5;

-- Check wallets
SELECT '2. Wallets table:' AS check;
SELECT COUNT(*) AS total_wallets FROM public.wallets;

-- Check auth users match
SELECT '3. Auth users (for comparison):' AS check;
SELECT COUNT(*) AS total_auth_users FROM auth.users;

-- Check vehicles
SELECT '4. Vehicles table:' AS check;
SELECT COUNT(*) AS total_vehicles FROM public.vehicles;

-- Check drivers
SELECT '5. Drivers table:' AS check;
SELECT COUNT(*) AS total_drivers FROM public.drivers;

-- Check bookings
SELECT '6. Bookings table:' AS check;
SELECT COUNT(*) AS total_bookings FROM public.bookings;

-- Check rides
SELECT '7. Rides table:' AS check;
SELECT COUNT(*) AS total_rides FROM public.rides;

-- Check payments
SELECT '8. Payments table:' AS check;
SELECT COUNT(*) AS total_payments FROM public.payments;

-- Check RLS policies
SELECT '9. RLS Policies count:' AS check;
SELECT COUNT(*) AS total_policies FROM pg_policies WHERE schemaname = 'public';

-- List all tables
SELECT '10. All tables created:' AS check;
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name;

-- List all triggers
SELECT '11. All triggers created:' AS check;
SELECT trigger_name, event_object_table FROM information_schema.triggers WHERE trigger_schema = 'public' ORDER BY trigger_name;

-- =============================================================================
-- PART 12: REALTIME CONFIGURATION (Optional - Enable in Dashboard)
-- =============================================================================

-- To enable realtime for bookings and rides:
-- 1. Go to Supabase Dashboard > Database > Replication
-- 2. Enable replication for: bookings, rides, drivers tables
-- 
-- Or run these commands if you have sufficient permissions:
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.bookings;
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.rides;
-- ALTER PUBLICATION supabase_realtime ADD TABLE public.drivers;

-- =============================================================================
-- PART 13: AUTHENTICATION SETUP (Do in Supabase Dashboard)
-- =============================================================================

-- 1. Go to Supabase Dashboard > Authentication > Providers
-- 2. Enable Email provider (default is enabled)
-- 3. Optionally enable Google OAuth provider
-- 4. Configure redirect URLs if needed

-- =============================================================================
-- FINAL SUCCESS MESSAGE
-- =============================================================================

SELECT '================================================================' AS status_line;
SELECT '|' AS status_line;
SELECT '|  VTT CABS Database Migration Completed Successfully!' AS status_line;
SELECT '|' AS status_line;
SELECT '|  Tables Created: 8' AS status_line;
SELECT '|  - profiles, wallets, vehicles, drivers' AS status_line;
SELECT '|  - bookings, rides, payments, wallet_transactions' AS status_line;
SELECT '|' AS status_line;
SELECT '|  Features Enabled:' AS status_line;
SELECT '|  - Auto profile creation on signup' AS status_line;
SELECT '|  - Auto wallet creation on signup' AS status_line;
SELECT '|  - Auto updated_at timestamps' AS status_line;
SELECT '|  - Auto booking status updates from rides' AS status_line;
SELECT '|  - Auto driver stats updates' AS status_line;
SELECT '|  - Row Level Security on all tables' AS status_line;
SELECT '|  - RLS policies for customers, drivers, admins' AS status_line;
SELECT '|' AS status_line;
SELECT '|  Next Steps:' AS status_line;
SELECT '|  1. Test user registration' AS status_line;
SELECT '|  2. Configure auth providers in Dashboard' AS status_line;
SELECT '|  3. Enable realtime in Replication settings' AS status_line;
SELECT '|  4. Deploy frontend to Netlify' AS status_line;
SELECT '|' AS status_line;
SELECT '================================================================' AS status_line;

SELECT '✅ VTT CABS Database Migration Completed Successfully' AS status;
