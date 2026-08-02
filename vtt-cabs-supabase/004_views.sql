-- VTT CABS - Database Views
-- Run this after 003_triggers.sql

-- ==================== USEFUL VIEWS ====================

-- View: Active bookings with full details
CREATE OR REPLACE VIEW v_active_bookings AS
SELECT 
    b.id,
    b.booking_number,
    b.status,
    b.pickup_address,
    b.drop_address,
    b.total_fare,
    b.trip_date,
    c.full_name as customer_name,
    c.phone as customer_phone,
    d.full_name as driver_name,
    d.phone as driver_phone,
    v.vehicle_number,
    v.vehicle_model,
    v.vehicle_type
FROM bookings b
LEFT JOIN customers c ON c.id = b.customer_id
LEFT JOIN drivers d ON d.id = b.driver_id
LEFT JOIN vehicles v ON v.id = b.vehicle_id
WHERE b.status NOT IN ('cancelled', 'payment_completed');

-- View: Driver performance summary
CREATE OR REPLACE VIEW v_driver_performance AS
SELECT 
    d.id,
    d.full_name,
    d.email,
    d.phone,
    d.status,
    d.total_trips,
    d.average_rating,
    d.is_online,
    d.is_available,
    COALESCE(dw.balance, 0) as wallet_balance,
    COALESCE(dw.lifetime_earnings, 0) as lifetime_earnings,
    COUNT(DISTINCT v.id) FILTER (WHERE v.is_verified = true) as verified_vehicles,
    COUNT(DISTINCT b.id) FILTER (
        WHERE b.status = 'trip_completed' 
        AND b.created_at >= CURRENT_DATE - INTERVAL '30 days'
    ) as trips_this_month,
    COALESCE(SUM(b.total_fare) FILTER (
        WHERE b.status = 'trip_completed'
        AND b.created_at >= CURRENT_DATE - INTERVAL '30 days'
    ), 0) as revenue_this_month
FROM drivers d
LEFT JOIN driver_wallet dw ON dw.driver_id = d.id
LEFT JOIN vehicles v ON v.driver_id = d.id
LEFT JOIN bookings b ON b.driver_id = d.id
GROUP BY d.id, d.full_name, d.email, d.phone, d.status, 
         d.total_trips, d.average_rating, d.is_online, d.is_available,
         dw.balance, dw.lifetime_earnings;

-- View: Customer summary
CREATE OR REPLACE VIEW v_customer_summary AS
SELECT 
    c.id,
    c.full_name,
    c.email,
    c.phone,
    c.wallet_balance,
    c.total_rides,
    c.created_at as member_since,
    COUNT(DISTINCT b.id) FILTER (
        WHERE b.status = 'trip_completed'
        AND b.created_at >= CURRENT_DATE - INTERVAL '30 days'
    ) as trips_this_month,
    COALESCE(SUM(b.total_fare) FILTER (
        WHERE b.status IN ('payment_completed', 'trip_completed')
        AND b.created_at >= CURRENT_DATE - INTERVAL '30 days'
    ), 0) as spent_this_month,
    COALESCE(AVG(r.rating) FILTER (WHERE r.from_user_type = 'customer'), 0) as avg_rating_given
FROM customers c
LEFT JOIN bookings b ON b.customer_id = c.id
LEFT JOIN ratings r ON r.to_user_id = c.id AND r.to_user_type = 'customer'
GROUP BY c.id, c.full_name, c.email, c.phone, c.wallet_balance, c.total_rides, c.created_at;

-- View: Daily revenue summary
CREATE OR REPLACE VIEW v_daily_revenue AS
SELECT 
    DATE(b.created_at) as date,
    COUNT(*) as total_bookings,
    COUNT(*) FILTER (WHERE b.status = 'trip_completed') as completed_trips,
    COUNT(*) FILTER (WHERE b.status = 'cancelled') as cancelled_trips,
    COALESCE(SUM(b.total_fare) FILTER (WHERE b.status IN ('trip_completed', 'payment_completed')), 0) as gross_revenue,
    COALESCE(SUM(b.platform_fee) FILTER (WHERE b.status IN ('trip_completed', 'payment_completed')), 0) as platform_revenue,
    COALESCE(SUM(b.driver_earnings) FILTER (WHERE b.status IN ('trip_completed', 'payment_completed')), 0) as driver_payouts,
    COALESCE(AVG(b.total_fare) FILTER (WHERE b.status = 'trip_completed'), 0) as avg_fare
FROM bookings b
WHERE b.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(b.created_at)
ORDER BY date DESC;

-- View: Pending driver verifications
CREATE OR REPLACE VIEW v_pending_verifications AS
SELECT 
    d.id,
    d.full_name,
    d.email,
    d.phone,
    d.created_at as applied_on,
    d.status,
    COUNT(dd.id) as documents_submitted,
    COUNT(dd.id) FILTER (WHERE dd.verification_status = 'pending') as documents_pending,
    COUNT(dd.id) FILTER (WHERE dd.verification_status = 'rejected') as documents_rejected
FROM drivers d
LEFT JOIN driver_documents dd ON dd.driver_id = d.id
WHERE d.status IN ('submitted', 'pending_verification')
GROUP BY d.id, d.full_name, d.email, d.phone, d.created_at, d.status
ORDER BY d.created_at ASC;

-- View: Active SOS alerts
CREATE OR REPLACE VIEW v_active_sos_alerts AS
SELECT 
    s.id,
    s.status,
    s.created_at as triggered_at,
    s.latitude,
    s.longitude,
    s.alert_message,
    b.booking_number,
    c.full_name as customer_name,
    c.phone as customer_phone,
    d.full_name as driver_name,
    d.phone as driver_phone
FROM sos_alerts s
LEFT JOIN bookings b ON b.id = s.booking_id
LEFT JOIN customers c ON c.id = s.customer_id
LEFT JOIN drivers d ON d.id = s.driver_id
WHERE s.status = 'active'
ORDER BY s.created_at DESC;

-- View: Driver locations with details (for map display)
CREATE OR REPLACE VIEW v_driver_locations AS
SELECT 
    d.id as driver_id,
    d.full_name,
    d.phone,
    d.is_online,
    d.is_available,
    d.current_latitude,
    d.current_longitude,
    d.last_location_update,
    v.vehicle_type,
    v.vehicle_number,
    v.vehicle_model,
    d.average_rating
FROM drivers d
LEFT JOIN vehicles v ON v.id = d.current_vehicle_id
WHERE d.current_latitude IS NOT NULL 
  AND d.current_longitude IS NOT NULL
  AND d.is_active = true;

-- View: Booking history for customers
CREATE OR REPLACE VIEW v_customer_booking_history AS
SELECT 
    b.id,
    b.booking_number,
    b.pickup_address,
    b.drop_address,
    b.total_fare,
    b.status,
    b.created_at as booked_at,
    b.trip_date,
    d.full_name as driver_name,
    d.phone as driver_phone,
    v.vehicle_number,
    v.vehicle_model,
    r.rating,
    r.review
FROM bookings b
LEFT JOIN drivers d ON d.id = b.driver_id
LEFT JOIN vehicles v ON v.id = b.vehicle_id
LEFT JOIN LATERAL (
    SELECT rating, review 
    FROM ratings 
    WHERE booking_id = b.id AND from_user_type = 'customer'
    LIMIT 1
) r ON true
ORDER BY b.created_at DESC;

-- View: Driver earnings summary
CREATE OR REPLACE VIEW v_driver_earnings_summary AS
SELECT 
    de.driver_id,
    d.full_name,
    SUM(de.gross_amount) as total_gross,
    SUM(de.platform_fee) as total_platform_fee,
    SUM(de.toll_charges) as total_toll,
    SUM(de.incentive) as total_incentives,
    SUM(de.penalty) as total_penalties,
    SUM(de.net_earning) as total_net,
    COUNT(de.id) as total_trips,
    SUM(de.net_earning) FILTER (WHERE de.trip_date >= CURRENT_DATE - INTERVAL '7 days') as earnings_this_week,
    SUM(de.net_earning) FILTER (WHERE de.trip_date >= CURRENT_DATE - INTERVAL '30 days') as earnings_this_month
FROM driver_earnings de
JOIN drivers d ON d.id = de.driver_id
GROUP BY de.driver_id, d.full_name;

-- View: Vehicle utilization
CREATE OR REPLACE VIEW v_vehicle_utilization AS
SELECT 
    v.id,
    v.vehicle_number,
    v.vehicle_type,
    v.vehicle_model,
    d.full_name as owner,
    v.is_verified,
    v.verification_status,
    COUNT(b.id) FILTER (WHERE b.status = 'trip_completed' AND b.created_at >= CURRENT_DATE - INTERVAL '30 days') as trips_last_30_days,
    COALESCE(SUM(b.actual_distance_km) FILTER (WHERE b.status = 'trip_completed' AND b.created_at >= CURRENT_DATE - INTERVAL '30 days'), 0) as distance_last_30_days
FROM vehicles v
LEFT JOIN drivers d ON d.id = v.driver_id
LEFT JOIN bookings b ON b.vehicle_id = v.id
GROUP BY v.id, v.vehicle_number, v.vehicle_type, v.vehicle_model, d.full_name, v.is_verified, v.verification_status;
