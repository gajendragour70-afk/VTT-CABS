-- VTT CABS - Database Functions
-- Run this after 001_initial_schema.sql

-- ==================== HELPER FUNCTIONS ====================

-- Function to generate booking number
CREATE OR REPLACE FUNCTION generate_booking_number()
RETURNS TEXT AS $$
DECLARE
    new_number TEXT;
    seq_val INT;
BEGIN
    -- Get next sequence value
    seq_val := nextval('booking_number_seq');
    
    -- Format: VTT-YYYYMMDD-XXXXX
    new_number := 'VTT-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(seq_val::TEXT, 5, '0');
    
    RETURN new_number;
END;
$$ LANGUAGE plpgsql;

-- Create sequence for booking numbers
CREATE SEQUENCE IF NOT EXISTS booking_number_seq START 1;

-- ==================== BOOKING FUNCTIONS ====================

-- Function to create a new booking
CREATE OR REPLACE FUNCTION create_booking(
    p_customer_id UUID,
    p_pickup_address TEXT,
    p_pickup_lat DOUBLE PRECISION,
    p_pickup_lng DOUBLE PRECISION,
    p_drop_address TEXT,
    p_drop_lat DOUBLE PRECISION,
    p_drop_lng DOUBLE PRECISION,
    p_vehicle_type TEXT,
    p_booking_type TEXT DEFAULT 'local'
)
RETURNS UUID AS $$
DECLARE
    new_booking_id UUID;
    booking_num TEXT;
BEGIN
    -- Generate booking number
    booking_num := generate_booking_number();
    
    -- Calculate estimated fare based on distance
    DECLARE
        distance_km DECIMAL(10,2);
        estimated_fare DECIMAL(10,2);
    BEGIN
        -- Simple distance calculation (Haversine approximation)
        distance_km := 6371 * acos(
            cos(radians(p_pickup_lat)) * cos(radians(p_drop_lat)) *
            cos(radians(p_drop_lng) - radians(p_pickup_lng)) +
            sin(radians(p_pickup_lat)) * sin(radians(p_drop_lat))
        );
        
        -- Base fare + per km rate
        estimated_fare := 20 + (distance_km * 12);
    END;
    
    -- Insert booking
    INSERT INTO bookings (
        booking_number,
        customer_id,
        pickup_address,
        pickup_latitude,
        pickup_longitude,
        drop_address,
        drop_latitude,
        drop_longitude,
        estimated_distance_km,
        total_fare,
        trip_date,
        status
    ) VALUES (
        booking_num,
        p_customer_id,
        p_pickup_address,
        p_pickup_lat,
        p_pickup_lng,
        p_drop_address,
        p_drop_lat,
        p_drop_lng,
        distance_km,
        estimated_fare,
        NOW(),
        'requested'
    ) RETURNING id INTO new_booking_id;
    
    -- Generate trip OTP
    UPDATE bookings 
    SET trip_otp = LPAD(FLOOR(RANDOM() * 999999)::TEXT, 6, '0')
    WHERE id = new_booking_id;
    
    RETURN new_booking_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to assign driver to booking
CREATE OR REPLACE FUNCTION assign_driver_to_booking(
    p_booking_id UUID,
    p_driver_id UUID
)
RETURNS BOOLEAN AS $$
DECLARE
    v_driver_uuid UUID;
    v_vehicle_uuid UUID;
BEGIN
    -- Get driver's current vehicle
    SELECT current_vehicle_id INTO v_vehicle_uuid
    FROM drivers
    WHERE id = p_driver_id;
    
    -- Update booking
    UPDATE bookings
    SET 
        driver_id = p_driver_id,
        vehicle_id = v_vehicle_uuid,
        status = 'driver_assigned',
        updated_at = NOW()
    WHERE id = p_booking_id;
    
    -- Update driver status
    UPDATE drivers
    SET 
        is_available = false,
        updated_at = NOW()
    WHERE id = p_driver_id;
    
    -- Create status history
    INSERT INTO booking_status_history (booking_id, status, changed_by, changed_by_type)
    VALUES (p_booking_id, 'driver_assigned', p_driver_id, 'driver');
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to update booking status
CREATE OR REPLACE FUNCTION update_booking_status(
    p_booking_id UUID,
    p_new_status TEXT,
    p_changed_by UUID,
    p_changed_by_type TEXT,
    p_notes TEXT DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE bookings
    SET 
        status = p_new_status,
        updated_at = NOW(),
        actual_pickup_time = CASE WHEN p_new_status = 'trip_started' THEN NOW() ELSE actual_pickup_time END,
        actual_drop_time = CASE WHEN p_new_status = 'trip_completed' THEN NOW() ELSE actual_drop_time END
    WHERE id = p_booking_id;
    
    -- Create status history
    INSERT INTO booking_status_history (booking_id, status, changed_by, changed_by_type, notes)
    VALUES (p_booking_id, p_new_status, p_changed_by, p_changed_by_type, p_notes);
    
    -- If trip completed, update driver availability
    IF p_new_status = 'trip_completed' THEN
        UPDATE drivers
        SET is_available = true, updated_at = NOW()
        WHERE id = p_changed_by;
    END IF;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==================== DRIVER FUNCTIONS ====================

-- Function to update driver location
CREATE OR REPLACE FUNCTION update_driver_location(
    p_driver_id UUID,
    p_latitude DOUBLE PRECISION,
    p_longitude DOUBLE PRECISION,
    p_bearing DOUBLE PRECISION DEFAULT NULL,
    p_speed DOUBLE PRECISION DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    -- Update driver current location
    UPDATE drivers
    SET 
        current_latitude = p_latitude,
        current_longitude = p_longitude,
        last_location_update = NOW(),
        updated_at = NOW()
    WHERE id = p_driver_id;
    
    -- Log to driver_locations history
    INSERT INTO driver_locations (driver_id, latitude, longitude, bearing, speed)
    VALUES (p_driver_id, p_latitude, p_longitude, p_bearing, p_speed);
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to toggle driver online status
CREATE OR REPLACE FUNCTION toggle_driver_online_status(
    p_driver_id UUID,
    p_is_online BOOLEAN
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE drivers
    SET 
        is_online = p_is_online,
        is_available = CASE WHEN p_is_online THEN true ELSE false END,
        updated_at = NOW()
    WHERE id = p_driver_id;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get nearby available drivers
CREATE OR REPLACE FUNCTION get_nearby_drivers(
    p_latitude DOUBLE PRECISION,
    p_longitude DOUBLE PRECISION,
    p_radius_km DOUBLE PRECISION DEFAULT 10,
    p_vehicle_type TEXT DEFAULT NULL
)
RETURNS TABLE (
    driver_id UUID,
    distance_km DOUBLE PRECISION,
    driver_name TEXT,
    vehicle_type TEXT,
    vehicle_model TEXT,
    rating DECIMAL(3,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.id as driver_id,
        (6371 * acos(
            cos(radians(p_latitude)) * cos(radians(d.current_latitude)) *
            cos(radians(d.current_longitude) - radians(p_longitude)) +
            sin(radians(p_latitude)) * sin(radians(d.current_latitude))
        ))::DOUBLE PRECISION as distance_km,
        d.full_name as driver_name,
        v.vehicle_type,
        v.vehicle_model,
        d.average_rating
    FROM drivers d
    LEFT JOIN vehicles v ON v.id = d.current_vehicle_id
    WHERE d.is_online = true
      AND d.is_available = true
      AND d.status = 'approved'
      AND d.is_active = true
      AND d.current_latitude IS NOT NULL
      AND d.current_longitude IS NOT NULL
      AND (
          6371 * acos(
              cos(radians(p_latitude)) * cos(radians(d.current_latitude)) *
              cos(radians(d.current_longitude) - radians(p_longitude)) +
              sin(radians(p_latitude)) * sin(radians(d.current_latitude))
          )
      ) <= p_radius_km
      AND (p_vehicle_type IS NULL OR v.vehicle_type = p_vehicle_type)
    ORDER BY distance_km ASC
    LIMIT 10;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==================== PAYMENT FUNCTIONS ====================

-- Function to process payment
CREATE OR REPLACE FUNCTION process_payment(
    p_booking_id UUID,
    p_payment_method TEXT,
    p_amount DECIMAL(10,2)
)
RETURNS UUID AS $$
DECLARE
    new_payment_id UUID;
    driver_share DECIMAL(10,2);
    platform_share DECIMAL(10,2);
    v_driver_id UUID;
BEGIN
    -- Get driver ID from booking
    SELECT driver_id INTO v_driver_id FROM bookings WHERE id = p_booking_id;
    
    -- Calculate shares (80% driver, 20% platform)
    driver_share := p_amount * 0.80;
    platform_share := p_amount * 0.20;
    
    -- Create payment record
    INSERT INTO payments (
        booking_id,
        customer_id,
        driver_id,
        amount,
        payment_method,
        payment_status,
        driver_share,
        platform_share
    ) VALUES (
        p_booking_id,
        (SELECT customer_id FROM bookings WHERE id = p_booking_id),
        v_driver_id,
        p_amount,
        p_payment_method,
        'completed',
        driver_share,
        platform_share
    ) RETURNING id INTO new_payment_id;
    
    -- Update driver wallet
    IF v_driver_id IS NOT NULL THEN
        UPDATE driver_wallet
        SET 
            balance = balance + driver_share,
            pending_balance = pending_balance + driver_share,
            lifetime_earnings = lifetime_earnings + driver_share,
            updated_at = NOW()
        WHERE driver_id = v_driver_id;
        
        -- Create earnings record
        INSERT INTO driver_earnings (
            driver_id,
            booking_id,
            gross_amount,
            platform_fee,
            net_earning,
            trip_date,
            settlement_status
        ) VALUES (
            v_driver_id,
            p_booking_id,
            p_amount,
            platform_share,
            driver_share,
            NOW(),
            'pending'
        );
    END IF;
    
    -- Update customer wallet if payment was from wallet
    IF p_payment_method = 'wallet' THEN
        UPDATE customers
        SET wallet_balance = wallet_balance - p_amount
        WHERE id = (SELECT customer_id FROM bookings WHERE id = p_booking_id);
    END IF;
    
    -- Update booking status
    UPDATE bookings
    SET status = 'payment_completed', updated_at = NOW()
    WHERE id = p_booking_id;
    
    RETURN new_payment_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==================== RATING FUNCTIONS ====================

-- Function to submit rating
CREATE OR REPLACE FUNCTION submit_rating(
    p_booking_id UUID,
    p_from_user_id UUID,
    p_from_user_type TEXT,
    p_rating INTEGER,
    p_review TEXT DEFAULT NULL,
    p_quick_tags JSONB DEFAULT '[]'::JSONB
)
RETURNS BOOLEAN AS $$
DECLARE
    to_user_uuid UUID;
    new_rating_id UUID;
BEGIN
    -- Determine target user based on booking
    IF p_from_user_type = 'customer' THEN
        SELECT driver_id INTO to_user_uuid FROM bookings WHERE id = p_booking_id;
    ELSE
        SELECT customer_id INTO to_user_uuid FROM bookings WHERE id = p_booking_id;
    END IF;
    
    -- Insert rating
    INSERT INTO ratings (
        booking_id,
        from_user_id,
        from_user_type,
        to_user_id,
        to_user_type,
        rating,
        review,
        quick_tags
    ) VALUES (
        p_booking_id,
        p_from_user_id,
        p_from_user_type,
        to_user_uuid,
        CASE p_from_user_type WHEN 'customer' THEN 'driver' ELSE 'customer' END,
        p_rating,
        p_review,
        p_quick_tags
    ) RETURNING id INTO new_rating_id;
    
    -- Update average rating for driver
    IF p_from_user_type = 'customer' AND to_user_uuid IS NOT NULL THEN
        UPDATE drivers
        SET average_rating = (
            SELECT AVG(rating)::DECIMAL(3,2)
            FROM ratings
            WHERE to_user_id = to_user_uuid AND to_user_type = 'driver'
        ),
        updated_at = NOW()
        WHERE id = to_user_uuid;
    END IF;
    
    -- Update average rating for customer
    IF p_from_user_type = 'driver' AND to_user_uuid IS NOT NULL THEN
        UPDATE customers
        SET 
            -- Could add average rating column to customers table
            updated_at = NOW()
        WHERE id = to_user_uuid;
    END IF;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==================== NOTIFICATION FUNCTIONS ====================

-- Function to create notification
CREATE OR REPLACE FUNCTION create_notification(
    p_user_id UUID,
    p_user_type TEXT,
    p_title TEXT,
    p_body TEXT,
    p_data JSONB DEFAULT '{}'::JSONB
)
RETURNS UUID AS $$
DECLARE
    new_notification_id UUID;
BEGIN
    INSERT INTO notifications (
        user_id,
        user_type,
        title,
        body,
        data
    ) VALUES (
        p_user_id,
        p_user_type,
        p_title,
        p_body,
        p_data
    ) RETURNING id INTO new_notification_id;
    
    -- TODO: Send push notification via FCM
    
    RETURN new_notification_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==================== SOS FUNCTIONS ====================

-- Function to trigger SOS
CREATE OR REPLACE FUNCTION trigger_sos(
    p_booking_id UUID,
    p_user_type TEXT,
    p_latitude DOUBLE PRECISION,
    p_longitude DOUBLE PRECISION,
    p_message TEXT DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    new_sos_id UUID;
    customer_phone TEXT;
    driver_phone TEXT;
BEGIN
    -- Get booking details
    SELECT 
        c.phone, 
        d.phone,
        c.full_name,
        d.full_name
    INTO customer_phone, driver_phone
    FROM bookings b
    LEFT JOIN customers c ON c.id = b.customer_id
    LEFT JOIN drivers d ON d.id = b.driver_id
    WHERE b.id = p_booking_id;
    
    -- Create SOS alert
    INSERT INTO sos_alerts (
        booking_id,
        customer_id,
        driver_id,
        latitude,
        longitude,
        status,
        alert_message
    ) VALUES (
        p_booking_id,
        CASE p_user_type WHEN 'customer' THEN p_user_id ELSE NULL END,
        CASE p_user_type WHEN 'driver' THEN p_user_id ELSE NULL END,
        p_latitude,
        p_longitude,
        'active',
        p_message
    ) RETURNING id INTO new_sos_id;
    
    -- Create notifications to admins
    INSERT INTO notifications (user_id, user_type, title, body, data)
    SELECT 
        a.id,
        'admin',
        '🚨 SOS Alert!',
        'Emergency alert triggered for booking ' || (SELECT booking_number FROM bookings WHERE id = p_booking_id),
        jsonb_build_object('sos_id', new_sos_id, 'booking_id', p_booking_id)
    FROM admins a WHERE a.is_active = true;
    
    RETURN new_sos_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
