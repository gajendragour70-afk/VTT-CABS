-- VTT-CABS Supabase Database Schema
-- Run this in your Supabase SQL Editor

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==================== ADMINS TABLE ====================
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT,
    role TEXT DEFAULT 'admin',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== CUSTOMERS TABLE ====================
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    profile_image_url TEXT,
    wallet_balance DECIMAL(10,2) DEFAULT 0.00,
    total_rides INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    fcm_token TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== DRIVERS TABLE ====================
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    profile_image_url TEXT,
    date_of_birth DATE,
    gender TEXT,
    address TEXT,
    city TEXT,
    state TEXT,
    pincode TEXT,
    bank_account_number TEXT,
    bank_ifsc TEXT,
    bank_name TEXT,
    upi_id TEXT,
    emergency_contact_name TEXT,
    emergency_contact_phone TEXT,
    emergency_contact_relation TEXT,
    aadhaar_front_url TEXT,
    aadhaar_back_url TEXT,
    pan_card_url TEXT,
    driving_license_front_url TEXT,
    driving_license_back_url TEXT,
    rc_doc_url TEXT,
    insurance_url TEXT,
    pollution_certificate_url TEXT,
    driver_selfie_url TEXT,
    status TEXT DEFAULT 'draft' CHECK (status IN ('draft', 'submitted', 'pending_verification', 'approved', 'rejected', 'suspended', 'active')),
    is_online BOOLEAN DEFAULT false,
    is_available BOOLEAN DEFAULT false,
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION,
    last_location_update TIMESTAMP WITH TIME ZONE,
    current_vehicle_id UUID,
    total_trips INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    wallet_balance DECIMAL(10,2) DEFAULT 0.00,
    pending_payout DECIMAL(10,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT true,
    fcm_token TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== VEHICLES TABLE ====================
CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES drivers(id) ON DELETE SET NULL,
    vehicle_number TEXT UNIQUE NOT NULL,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('auto', 'hatchback', 'sedan', 'suv', 'luxury')),
    vehicle_model TEXT NOT NULL,
    vehicle_brand TEXT NOT NULL,
    vehicle_color TEXT,
    vehicle_year INTEGER,
    rc_front_url TEXT,
    rc_back_url TEXT,
    insurance_url TEXT,
    pollution_certificate_url TEXT,
    vehicle_front_url TEXT,
    vehicle_back_url TEXT,
    vehicle_left_url TEXT,
    vehicle_right_url TEXT,
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    verification_status TEXT DEFAULT 'pending' CHECK (verification_status IN ('pending', 'verified', 'rejected')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== BOOKINGS TABLE ====================
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_number TEXT UNIQUE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    driver_id UUID REFERENCES drivers(id) ON DELETE SET NULL,
    vehicle_id UUID REFERENCES vehicles(id) ON DELETE SET NULL,
    booking_type TEXT NOT NULL CHECK (booking_type IN ('local', 'rental', 'airport', 'outstation', 'one_way', 'round_trip')),
    pickup_address TEXT NOT NULL,
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    drop_address TEXT,
    drop_latitude DOUBLE PRECISION,
    drop_longitude DOUBLE PRECISION,
    waypoints JSONB DEFAULT '[]',
    trip_date TIMESTAMP WITH TIME ZONE NOT NULL,
    trip_time TIME,
    estimated_pickup_time TIMESTAMP WITH TIME ZONE,
    actual_pickup_time TIMESTAMP WITH TIME ZONE,
    actual_drop_time TIMESTAMP WITH TIME ZONE,
    estimated_distance_km DECIMAL(10,2),
    actual_distance_km DECIMAL(10,2),
    estimated_duration_minutes INTEGER,
    actual_duration_minutes INTEGER,
    base_fare DECIMAL(10,2) DEFAULT 0.00,
    distance_fare DECIMAL(10,2) DEFAULT 0.00,
    time_fare DECIMAL(10,2) DEFAULT 0.00,
    surge_multiplier DECIMAL(3,2) DEFAULT 1.00,
    coupon_discount DECIMAL(10,2) DEFAULT 0.00,
    toll_charges DECIMAL(10,2) DEFAULT 0.00,
    total_fare DECIMAL(10,2) NOT NULL,
    driver_earnings DECIMAL(10,2) DEFAULT 0.00,
    platform_fee DECIMAL(10,2) DEFAULT 0.00,
    status TEXT DEFAULT 'requested' CHECK (status IN ('requested', 'confirmed', 'driver_assigned', 'otp_verified', 'trip_started', 'trip_completed', 'cancelled', 'payment_pending', 'payment_completed')),
    trip_otp TEXT,
    driver_current_latitude DOUBLE PRECISION,
    driver_current_longitude DOUBLE PRECISION,
    cancelled_by TEXT,
    cancellation_reason TEXT,
    cancellation_time TIMESTAMP WITH TIME ZONE,
    customer_notes TEXT,
    driver_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== BOOKING STATUS HISTORY ====================
CREATE TABLE IF NOT EXISTS booking_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    changed_by UUID,
    changed_by_type TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== PAYMENTS TABLE ====================
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    driver_id UUID REFERENCES drivers(id) ON DELETE SET NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method TEXT CHECK (payment_method IN ('cash', 'card', 'upi', 'wallet', 'online')),
    payment_status TEXT DEFAULT 'pending' CHECK (payment_status IN ('pending', 'processing', 'completed', 'failed', 'refunded')),
    transaction_id TEXT,
    gateway_transaction_id TEXT,
    gateway_response JSONB,
    driver_share DECIMAL(10,2) DEFAULT 0.00,
    platform_share DECIMAL(10,2) DEFAULT 0.00,
    coupon_id UUID,
    coupon_discount DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== RATINGS TABLE ====================
CREATE TABLE IF NOT EXISTS ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE CASCADE,
    from_user_id UUID NOT NULL,
    from_user_type TEXT NOT NULL CHECK (from_user_type IN ('customer', 'driver')),
    to_user_id UUID NOT NULL,
    to_user_type TEXT NOT NULL CHECK (to_user_type IN ('customer', 'driver')),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    quick_tags JSONB DEFAULT '[]',
    is_visible BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== NOTIFICATIONS TABLE ====================
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    user_type TEXT NOT NULL CHECK (user_type IN ('customer', 'driver', 'admin')),
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    data JSONB DEFAULT '{}',
    notification_type TEXT CHECK (notification_type IN ('booking', 'payment', 'alert', 'promotion', 'system', 'document')),
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== SOS ALERTS TABLE ====================
CREATE TABLE IF NOT EXISTS sos_alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    driver_id UUID REFERENCES drivers(id) ON DELETE SET NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address TEXT,
    alert_type TEXT DEFAULT 'emergency',
    description TEXT,
    status TEXT DEFAULT 'active' CHECK (status IN ('active', 'acknowledged', 'resolved', 'false_alarm')),
    responded_by UUID,
    response_notes TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== DRIVER DOCUMENTS TABLE ====================
CREATE TABLE IF NOT EXISTS driver_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    document_type TEXT NOT NULL CHECK (document_type IN ('aadhaar_front', 'aadhaar_back', 'pan_card', 'license_front', 'license_back', 'rc_front', 'rc_back', 'insurance', 'pollution', 'selfie')),
    document_number TEXT,
    expiry_date DATE,
    document_url TEXT NOT NULL,
    verification_status TEXT DEFAULT 'pending' CHECK (verification_status IN ('pending', 'verified', 'rejected', 'expired')),
    verified_by UUID,
    verified_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== DRIVER EARNINGS TABLE ====================
CREATE TABLE IF NOT EXISTS driver_earnings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    gross_amount DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) DEFAULT 0.00,
    gst DECIMAL(10,2) DEFAULT 0.00,
    toll_charges DECIMAL(10,2) DEFAULT 0.00,
    incentive DECIMAL(10,2) DEFAULT 0.00,
    penalty DECIMAL(10,2) DEFAULT 0.00,
    net_earning DECIMAL(10,2) NOT NULL,
    trip_date TIMESTAMP WITH TIME ZONE NOT NULL,
    settlement_status TEXT DEFAULT 'pending' CHECK (settlement_status IN ('pending', 'processing', 'settled')),
    settled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== DRIVER WALLET TABLE ====================
CREATE TABLE IF NOT EXISTS driver_wallet (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE UNIQUE,
    balance DECIMAL(10,2) DEFAULT 0.00,
    pending_balance DECIMAL(10,2) DEFAULT 0.00,
    lifetime_earnings DECIMAL(10,2) DEFAULT 0.00,
    lifetime_payouts DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== PAYOUT REQUESTS TABLE ====================
CREATE TABLE IF NOT EXISTS payout_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method TEXT DEFAULT 'bank_transfer',
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'completed', 'failed')),
    transaction_id TEXT,
    failure_reason TEXT,
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE
);

-- ==================== DRIVER LOCATIONS TABLE ====================
CREATE TABLE IF NOT EXISTS driver_locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    bearing DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    accuracy DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== INDEXES ====================
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_drivers_email ON drivers(email);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(status);
CREATE INDEX IF NOT EXISTS idx_drivers_is_online ON drivers(is_online);
CREATE INDEX IF NOT EXISTS idx_vehicles_driver_id ON vehicles(driver_id);
CREATE INDEX IF NOT EXISTS idx_bookings_customer_id ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_driver_id ON bookings(driver_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_sos_alerts_status ON sos_alerts(status);

-- ==================== ROW LEVEL SECURITY ====================
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE drivers ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehicles ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE booking_status_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE ratings ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE sos_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE driver_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE driver_earnings ENABLE ROW LEVEL SECURITY;
ALTER TABLE driver_wallet ENABLE ROW LEVEL SECURITY;
ALTER TABLE payout_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE driver_locations ENABLE ROW LEVEL SECURITY;

-- Basic RLS Policies
CREATE POLICY "Admins can view all admins" ON admins FOR SELECT USING (true);
CREATE POLICY "Customers can view own profile" ON customers FOR SELECT USING (auth.uid() = auth_user_id);
CREATE POLICY "Customers can update own profile" ON customers FOR UPDATE USING (auth.uid() = auth_user_id);
CREATE POLICY "Drivers can view own profile" ON drivers FOR SELECT USING (auth.uid() = auth_user_id);
CREATE POLICY "Drivers can update own profile" ON drivers FOR UPDATE USING (auth.uid() = auth_user_id);
CREATE POLICY "Drivers can update own location" ON drivers FOR UPDATE USING (auth.uid() = auth_user_id) WITH CHECK (true);
CREATE POLICY "Admins can view all drivers" ON drivers FOR SELECT USING (true);
CREATE POLICY "Admins can update drivers" ON drivers FOR UPDATE USING (true);
CREATE POLICY "Drivers can view own vehicles" ON vehicles FOR SELECT USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Drivers can manage own vehicles" ON vehicles FOR ALL USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Admins can view all vehicles" ON vehicles FOR SELECT USING (true);
CREATE POLICY "Customers can view own bookings" ON bookings FOR SELECT USING (customer_id IN (SELECT id FROM customers WHERE customers.auth_user_id = auth.uid()));
CREATE POLICY "Drivers can view assigned bookings" ON bookings FOR SELECT USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Admins can view all bookings" ON bookings FOR SELECT USING (true);
CREATE POLICY "Admins can update bookings" ON bookings FOR UPDATE USING (true);
CREATE POLICY "Users can view own notifications" ON notifications FOR SELECT USING (user_id IN (SELECT id FROM customers WHERE customers.auth_user_id = auth.uid()) OR user_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Admins can manage all notifications" ON notifications FOR ALL USING (true);
CREATE POLICY "Users can create SOS alerts" ON sos_alerts FOR INSERT WITH CHECK (customer_id IN (SELECT id FROM customers WHERE customers.auth_user_id = auth.uid()) OR driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Admins can view all SOS alerts" ON sos_alerts FOR SELECT USING (true);
CREATE POLICY "Drivers can view own documents" ON driver_documents FOR SELECT USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Drivers can manage own documents" ON driver_documents FOR ALL USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Drivers can view own earnings" ON driver_earnings FOR SELECT USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Drivers can update own location" ON driver_locations FOR INSERT WITH CHECK (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));

-- Enable Realtime
ALTER PUBLICATION supabase_realtime ADD TABLE bookings;
ALTER PUBLICATION supabase_realtime ADD TABLE drivers;
ALTER PUBLICATION supabase_realtime ADD TABLE notifications;
ALTER PUBLICATION supabase_realtime ADD TABLE sos_alerts;
ALTER PUBLICATION supabase_realtime ADD TABLE driver_locations;
