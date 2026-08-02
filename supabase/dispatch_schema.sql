-- Smart Dispatch System Schema Updates
-- Run these AFTER the main schema.sql

-- ==================== DISPATCH CONFIG ====================
CREATE TABLE IF NOT EXISTS dispatch_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    config_key TEXT UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Insert default dispatch config
INSERT INTO dispatch_config (config_key, config_value, description) VALUES
    ('initial_search_radius_km', '5', 'Initial search radius in kilometers'),
    ('max_search_radius_km', '20', 'Maximum search radius in kilometers'),
    ('search_timeout_seconds', '30', 'Seconds before expanding search radius'),
    ('max_rejection_count', '3', 'Max driver rejections before manual assignment'),
    ('booking_offer_validity_seconds', '60', 'Seconds before offer expires')
ON CONFLICT (config_key) DO NOTHING;

-- ==================== BOOKING OFFERS TABLE ====================
CREATE TABLE IF NOT EXISTS booking_offers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id) ON DELETE CASCADE,
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected', 'expired', 'cancelled')),
    offered_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    responded_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    distance_km DECIMAL(10,2),
    eta_minutes INTEGER,
    UNIQUE(booking_id, driver_id)
);

-- Add columns to bookings table for smart dispatch
DO $$ BEGIN
    ALTER TABLE bookings ADD COLUMN IF NOT EXISTS vehicle_category TEXT DEFAULT 'sedan' CHECK (vehicle_category IN ('auto', 'hatchback', 'sedan', 'suv', 'luxury'));
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE bookings ADD COLUMN IF NOT EXISTS current_search_radius_km DECIMAL(10,2) DEFAULT 5.00;
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE bookings ADD COLUMN IF NOT EXISTS search_attempts INTEGER DEFAULT 0;
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE bookings ADD COLUMN IF NOT EXISTS rejection_count INTEGER DEFAULT 0;
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE bookings ADD COLUMN IF NOT EXISTS last_driver_notified_at TIMESTAMP WITH TIME ZONE;
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE bookings ADD COLUMN IF NOT EXISTS timeout_at TIMESTAMP WITH TIME ZONE;
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE drivers ADD COLUMN IF NOT EXISTS current_booking_id UUID;
EXCEPTION
    WHEN duplicate_column THEN NULL;
END $$;

-- Update booking status to include new statuses
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_status_check CHECK (status IN (
    'new',                    -- Just created
    'searching_driver',        -- Actively searching for drivers
    'driver_assigned',        -- Driver assigned, waiting acceptance
    'driver_accepted',        -- Driver accepted, heading to pickup
    'driver_rejected',        -- Driver rejected, searching next
    'driver_arrived',         -- Driver arrived at pickup
    'otp_verified',           -- OTP verified, trip ready to start
    'trip_started',           -- Trip in progress
    'trip_completed',         -- Trip finished
    'cancelled',              -- Booking cancelled
    'payment_pending',        -- Waiting for payment
    'payment_completed'       -- Payment received
));

-- ==================== INDEXES ====================
CREATE INDEX IF NOT EXISTS idx_booking_offers_booking ON booking_offers(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_offers_driver ON booking_offers(driver_id);
CREATE INDEX IF NOT EXISTS idx_booking_offers_status ON booking_offers(status) WHERE status = 'pending';
CREATE INDEX IF NOT EXISTS idx_bookings_searching ON bookings(status, current_search_radius_km) WHERE status = 'searching_driver';
CREATE INDEX IF NOT EXISTS idx_drivers_current_booking ON drivers(current_booking_id);
CREATE INDEX IF NOT EXISTS idx_drivers_location ON drivers(current_latitude, current_longitude);

-- ==================== RLS POLICIES ====================
ALTER TABLE dispatch_config ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Admins can view dispatch config" ON dispatch_config FOR SELECT USING (true);
CREATE POLICY "Admins can update dispatch config" ON dispatch_config FOR UPDATE USING (true);

ALTER TABLE booking_offers ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Drivers can view own offers" ON booking_offers FOR SELECT USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid()));
CREATE POLICY "Drivers can respond to offers" ON booking_offers FOR UPDATE USING (driver_id IN (SELECT id FROM drivers WHERE drivers.auth_user_id = auth.uid())) WITH CHECK (true);
CREATE POLICY "System can create offers" ON booking_offers FOR INSERT WITH CHECK (true);
CREATE POLICY "Admins can view all offers" ON booking_offers FOR SELECT USING (true);

-- Update booking history to track old status
ALTER TABLE booking_status_history DROP COLUMN IF EXISTS status;
ALTER TABLE booking_status_history ADD COLUMN IF NOT EXISTS old_status TEXT;
ALTER TABLE booking_status_history ADD COLUMN IF NOT EXISTS new_status TEXT NOT NULL;

-- ==================== FUNCTIONS ====================

-- Function to calculate distance between two points (Haversine formula)
CREATE OR REPLACE FUNCTION calculate_distance(lat1 DOUBLE PRECISION, lon1 DOUBLE PRECISION, lat2 DOUBLE PRECISION, lon2 DOUBLE PRECISION)
RETURNS DOUBLE PRECISION AS $$
DECLARE
    R DOUBLE PRECISION := 6371;
    dlat DOUBLE PRECISION;
    dlon DOUBLE PRECISION;
    a DOUBLE PRECISION;
    c DOUBLE PRECISION;
BEGIN
    dlat := radians(lat2 - lat1);
    dlon := radians(lon2 - lon1);
    a := sin(dlat/2) * sin(dlat/2) + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon/2) * sin(dlon/2);
    c := 2 * atan2(sqrt(a), sqrt(1-a));
    RETURN R * c;
END;
$$ LANGUAGE plpgsql;

-- Function to find nearby available drivers
CREATE OR REPLACE FUNCTION find_nearby_drivers(
    pickup_lat DOUBLE PRECISION,
    pickup_lon DOUBLE PRECISION,
    radius_km DOUBLE PRECISION,
    vehicle_category TEXT DEFAULT NULL,
    exclude_driver_id UUID DEFAULT NULL
)
RETURNS TABLE (
    driver_id UUID,
    driver_name TEXT,
    distance_km DOUBLE PRECISION,
    rating DECIMAL,
    vehicle_type TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.id as driver_id,
        d.full_name as driver_name,
        calculate_distance(pickup_lat, pickup_lon, d.current_latitude, d.current_longitude) as distance_km,
        d.average_rating,
        v.vehicle_type
    FROM drivers d
    LEFT JOIN vehicles v ON d.current_vehicle_id = v.id
    WHERE d.is_online = true
        AND d.is_available = true
        AND d.status = 'active'
        AND d.current_booking_id IS NULL
        AND d.current_latitude IS NOT NULL
        AND d.current_longitude IS NOT NULL
        AND d.is_active = true
        AND calculate_distance(pickup_lat, pickup_lon, d.current_latitude, d.current_longitude) <= radius_km
        AND (vehicle_category IS NULL OR v.vehicle_type = vehicle_category)
        AND (exclude_driver_id IS NULL OR d.id != exclude_driver_id)
    ORDER BY distance_km ASC
    LIMIT 10;
END;
$$ LANGUAGE plpgsql;

-- Function to atomically accept booking (prevents duplicate acceptance)
CREATE OR REPLACE FUNCTION accept_booking(
    p_booking_id UUID,
    p_driver_id UUID,
    p_offer_id UUID
)
RETURNS BOOLEAN AS $$
DECLARE
    v_booking_status TEXT;
    v_current_driver_id UUID;
BEGIN
    SELECT status, driver_id INTO v_booking_status, v_current_driver_id
    FROM bookings WHERE id = p_booking_id FOR UPDATE;
    
    IF v_current_driver_id IS NOT NULL THEN
        RETURN FALSE;
    END IF;
    
    IF v_booking_status NOT IN ('searching_driver', 'driver_rejected', 'driver_assigned') THEN
        RETURN FALSE;
    END IF;
    
    UPDATE bookings SET
        driver_id = p_driver_id,
        status = 'driver_accepted',
        updated_at = NOW()
    WHERE id = p_booking_id;
    
    UPDATE drivers SET
        is_available = false,
        current_booking_id = p_booking_id,
        updated_at = NOW()
    WHERE id = p_driver_id;
    
    UPDATE booking_offers SET
        status = 'accepted',
        responded_at = NOW()
    WHERE id = p_offer_id;
    
    UPDATE booking_offers SET
        status = 'cancelled'
    WHERE booking_id = p_booking_id AND id != p_offer_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Enable Realtime for new tables
ALTER PUBLICATION supabase_realtime ADD TABLE booking_offers;
