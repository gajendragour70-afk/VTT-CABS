-- VTT CABS - Sample Data
-- Run this last - optional for demo/testing

-- ==================== ADMIN USER ====================
INSERT INTO admins (email, password_hash, full_name, phone, role)
VALUES (
    'admin@vttcabs.com',
    '$2a$10$abcdefghijklmnopqrstuvwxyz', -- hash of 'admin123'
    'Admin User',
    '+919876543210',
    'super_admin'
) ON CONFLICT (email) DO NOTHING;

-- ==================== SAMPLE CUSTOMERS ====================
-- Note: In production, customers are created via Supabase Auth
-- These are placeholder records that would be linked to auth.users

INSERT INTO customers (email, full_name, phone, wallet_balance, total_rides)
VALUES 
    ('customer1@example.com', 'Rahul Sharma', '+919876543211', 500.00, 15),
    ('customer2@example.com', 'Priya Patel', '+919876543212', 250.00, 8),
    ('customer3@example.com', 'Amit Kumar', '+919876543213', 100.00, 3),
    ('customer4@example.com', 'Sneha Gupta', '+919876543214', 750.00, 22),
    ('customer5@example.com', 'Vikram Singh', '+919876543215', 0.00, 1)
ON CONFLICT (email) DO NOTHING;

-- ==================== SAMPLE DRIVERS ====================
INSERT INTO drivers (
    email, full_name, phone, status, is_online, is_available,
    current_latitude, current_longitude, total_trips, average_rating,
    wallet_balance, pending_payout, city, state
)
VALUES 
    (
        'driver1@vttcabs.com', 'Rajesh Kumar', '+919876543220',
        'approved', true, true,
        28.6139, 77.2090, 45, 4.75,
        2500.00, 500.00, 'Gurugram', 'Haryana'
    ),
    (
        'driver2@vttcabs.com', 'Amit Singh', '+919876543221',
        'approved', true, false,
        28.6280, 77.2195, 32, 4.60,
        1800.00, 350.00, 'Gurugram', 'Haryana'
    ),
    (
        'driver3@vttcabs.com', 'Priya Sharma', '+919876543222',
        'approved', false, false,
        28.6300, 77.2150, 28, 4.85,
        1200.00, 200.00, 'Gurugram', 'Haryana'
    ),
    (
        'driver4@vttcabs.com', 'Vikram Yadav', '+919876543223',
        'approved', true, true,
        28.6200, 77.2100, 52, 4.55,
        3500.00, 700.00, 'Gurugram', 'Haryana'
    ),
    (
        'driver5@vttcabs.com', 'Neha Gupta', '+919876543224',
        'pending_verification', false, false,
        NULL, NULL, 0, 0.00,
        0.00, 0.00, 'Gurugram', 'Haryana'
    )
ON CONFLICT (email) DO NOTHING;

-- ==================== SAMPLE VEHICLES ====================
INSERT INTO vehicles (driver_id, vehicle_number, vehicle_type, vehicle_model, vehicle_brand, vehicle_color, is_verified, verification_status)
SELECT 
    d.id, 'DL 01 CA 1234', 'sedan', 'City', 'Honda', 'Silver', true, 'verified'
FROM drivers d WHERE d.email = 'driver1@vttcabs.com'
ON CONFLICT (vehicle_number) DO NOTHING;

INSERT INTO vehicles (driver_id, vehicle_number, vehicle_type, vehicle_model, vehicle_brand, vehicle_color, is_verified, verification_status)
SELECT 
    d.id, 'DL 01 CB 5678', 'suv', 'Creta', 'Hyundai', 'White', true, 'verified'
FROM drivers d WHERE d.email = 'driver2@vttcabs.com'
ON CONFLICT (vehicle_number) DO NOTHING;

INSERT INTO vehicles (driver_id, vehicle_number, vehicle_type, vehicle_model, vehicle_brand, vehicle_color, is_verified, verification_status)
SELECT 
    d.id, 'DL 01 CC 9012', 'hatchback', 'Swift', 'Maruti', 'Red', true, 'verified'
FROM drivers d WHERE d.email = 'driver3@vttcabs.com'
ON CONFLICT (vehicle_number) DO NOTHING;

INSERT INTO vehicles (driver_id, vehicle_number, vehicle_type, vehicle_model, vehicle_brand, vehicle_color, is_verified, verification_status)
SELECT 
    d.id, 'DL 01 CD 3456', 'luxury', 'E-Class', 'Mercedes', 'Black', true, 'verified'
FROM drivers d WHERE d.email = 'driver4@vttcabs.com'
ON CONFLICT (vehicle_number) DO NOTHING;

INSERT INTO vehicles (driver_id, vehicle_number, vehicle_type, vehicle_model, vehicle_brand, vehicle_color, is_verified, verification_status)
SELECT 
    d.id, 'DL 01 CE 7890', 'auto', 'Activa', 'Honda', 'Black', false, 'pending'
FROM drivers d WHERE d.email = 'driver5@vttcabs.com'
ON CONFLICT (vehicle_number) DO NOTHING;

-- Update driver current vehicle
UPDATE drivers SET current_vehicle_id = (
    SELECT v.id FROM vehicles v WHERE v.vehicle_number = 'DL 01 CA 1234'
) WHERE email = 'driver1@vttcabs.com';

UPDATE drivers SET current_vehicle_id = (
    SELECT v.id FROM vehicles v WHERE v.vehicle_number = 'DL 01 CB 5678'
) WHERE email = 'driver2@vttcabs.com';

UPDATE drivers SET current_vehicle_id = (
    SELECT v.id FROM vehicles v WHERE v.vehicle_number = 'DL 01 CC 9012'
) WHERE email = 'driver3@vttcabs.com';

UPDATE drivers SET current_vehicle_id = (
    SELECT v.id FROM vehicles v WHERE v.vehicle_number = 'DL 01 CD 3456'
) WHERE email = 'driver4@vttcabs.com';

-- Create driver wallets
INSERT INTO driver_wallet (driver_id, balance, pending_balance, lifetime_earnings)
SELECT id, 2500.00, 500.00, 15000.00 FROM drivers WHERE email = 'driver1@vttcabs.com'
ON CONFLICT (driver_id) DO NOTHING;

INSERT INTO driver_wallet (driver_id, balance, pending_balance, lifetime_earnings)
SELECT id, 1800.00, 350.00, 10000.00 FROM drivers WHERE email = 'driver2@vttcabs.com'
ON CONFLICT (driver_id) DO NOTHING;

INSERT INTO driver_wallet (driver_id, balance, pending_balance, lifetime_earnings)
SELECT id, 1200.00, 200.00, 8000.00 FROM drivers WHERE email = 'driver3@vttcabs.com'
ON CONFLICT (driver_id) DO NOTHING;

INSERT INTO driver_wallet (driver_id, balance, pending_balance, lifetime_earnings)
SELECT id, 3500.00, 700.00, 20000.00 FROM drivers WHERE email = 'driver4@vttcabs.com'
ON CONFLICT (driver_id) DO NOTHING;

-- ==================== SAMPLE BOOKINGS ====================
INSERT INTO bookings (
    booking_number, customer_id, driver_id, vehicle_id,
    pickup_address, pickup_latitude, pickup_longitude,
    drop_address, drop_latitude, drop_longitude,
    estimated_distance_km, total_fare, driver_earnings, platform_fee,
    status, trip_otp, trip_date
)
SELECT 
    'VTT-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-00001',
    c.id, d.id, v.id,
    'Sector 15, Gurugram', 28.6139, 77.2090,
    'Cyber Hub, Gurugram', 28.6280, 77.2195,
    5.2, 120.00, 96.00, 24.00,
    'trip_completed', '123456', NOW() - INTERVAL '2 hours'
FROM customers c, drivers d, vehicles v
WHERE c.email = 'customer1@example.com' AND d.email = 'driver1@vttcabs.com' AND v.vehicle_number = 'DL 01 CA 1234';

INSERT INTO bookings (
    booking_number, customer_id, driver_id, vehicle_id,
    pickup_address, pickup_latitude, pickup_longitude,
    drop_address, drop_latitude, drop_longitude,
    estimated_distance_km, total_fare, driver_earnings, platform_fee,
    status, trip_otp, trip_date
)
SELECT 
    'VTT-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-00002',
    c.id, d.id, v.id,
    'Ambience Mall, Gurugram', 28.6250, 77.0850,
    'Rajiv Chowk, Gurugram', 28.6280, 77.0450,
    8.5, 180.00, 144.00, 36.00,
    'driver_assigned', '234567', NOW() - INTERVAL '30 minutes'
FROM customers c, drivers d, vehicles v
WHERE c.email = 'customer2@example.com' AND d.email = 'driver2@vttcabs.com' AND v.vehicle_number = 'DL 01 CB 5678';

INSERT INTO bookings (
    booking_number, customer_id, driver_id, vehicle_id,
    pickup_address, pickup_latitude, pickup_longitude,
    drop_address, drop_latitude, drop_longitude,
    estimated_distance_km, total_fare, driver_earnings, platform_fee,
    status, trip_otp, trip_date
)
SELECT 
    'VTT-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-00003',
    c.id, NULL, NULL,
    'IFFCO Chowk, Gurugram', 28.4595, 77.0276,
    'HUDA City Centre', 28.4815, 77.0677,
    6.8, 150.00, 120.00, 30.00,
    'requested', '345678', NOW()
FROM customers c
WHERE c.email = 'customer3@example.com';

-- ==================== SAMPLE RATINGS ====================
INSERT INTO ratings (booking_id, from_user_id, from_user_type, to_user_id, to_user_type, rating, review)
SELECT 
    b.id, c.id, 'customer', d.id, 'driver', 5, 'Great ride! Very professional driver.'
FROM bookings b, customers c, drivers d
WHERE b.customer_id = c.id AND b.driver_id = d.id
  AND c.email = 'customer1@example.com' AND d.email = 'driver1@vttcabs.com'
  AND b.status = 'trip_completed'
ON CONFLICT DO NOTHING;

INSERT INTO ratings (booking_id, from_user_id, from_user_type, to_user_id, to_user_type, rating, review)
SELECT 
    b.id, d.id, 'driver', c.id, 'customer', 5, 'Nice customer.'
FROM bookings b, customers c, drivers d
WHERE b.customer_id = c.id AND b.driver_id = d.id
  AND c.email = 'customer1@example.com' AND d.email = 'driver1@vttcabs.com'
  AND b.status = 'trip_completed'
ON CONFLICT DO NOTHING;

-- ==================== SAMPLE DRIVER LOCATIONS ====================
INSERT INTO driver_locations (driver_id, latitude, longitude, bearing, speed)
SELECT id, 28.6139, 77.2090, 45.0, 30.5 FROM drivers WHERE email = 'driver1@vttcabs.com';

INSERT INTO driver_locations (driver_id, latitude, longitude, bearing, speed)
SELECT id, 28.6280, 77.2195, 90.0, 25.0 FROM drivers WHERE email = 'driver2@vttcabs.com';

INSERT INTO driver_locations (driver_id, latitude, longitude, bearing, speed)
SELECT id, 28.6200, 77.2100, 180.0, 0.0 FROM drivers WHERE email = 'driver4@vttcabs.com';

-- ==================== SAMPLE NOTIFICATIONS ====================
INSERT INTO notifications (user_id, user_type, title, body)
SELECT id, 'customer', 'Welcome to VTT CABS!', 'Thank you for joining. Start booking rides now!'
FROM customers WHERE email = 'customer1@example.com';

INSERT INTO notifications (user_id, user_type, title, body)
SELECT id, 'driver', 'Welcome to VTT CABS!', 'Start earning by accepting rides in your area!'
FROM drivers WHERE email = 'driver1@vttcabs.com';

-- ==================== VERIFICATION COMPLETE ====================
DO $$
BEGIN
    RAISE NOTICE 'Sample data inserted successfully!';
    RAISE NOTICE '';
    RAISE NOTICE 'Demo Credentials:';
    RAISE NOTICE '  Admin: admin@vttcabs.com / admin123';
    RAISE NOTICE '  Customer: customer1@example.com';
    RAISE NOTICE '  Driver: driver1@vttcabs.com';
END $$;
