-- VTT CABS - Database Triggers
-- Run this after 002_functions.sql

-- ==================== TRIGGER FUNCTIONS ====================

-- Function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to create notification on booking status change
CREATE OR REPLACE FUNCTION notify_booking_status_change()
RETURNS TRIGGER AS $$
DECLARE
    customer_uuid UUID;
    driver_uuid UUID;
    notification_title TEXT;
    notification_body TEXT;
BEGIN
    -- Get relevant user IDs
    SELECT customer_id, driver_id INTO customer_uuid, driver_uuid
    FROM bookings WHERE id = NEW.id;
    
    -- Determine notification based on status
    CASE NEW.status
        WHEN 'confirmed' THEN
            notification_title := 'Booking Confirmed!';
            notification_body := 'Your ride has been confirmed.';
        WHEN 'driver_assigned' THEN
            notification_title := 'Driver Assigned';
            notification_body := 'A driver has been assigned to your ride.';
            -- Also notify driver
            IF driver_uuid IS NOT NULL THEN
                PERFORM create_notification(
                    driver_uuid,
                    'driver',
                    '🚗 New Booking!',
                    'You have been assigned a new ride. Check your app for details.',
                    jsonb_build_object('booking_id', NEW.id)
                );
            END IF;
        WHEN 'otp_verified' THEN
            notification_title := 'OTP Verified';
            notification_body := 'Trip has started. Have a safe journey!';
        WHEN 'trip_started' THEN
            notification_title := 'Trip Started';
            notification_body := 'Your trip has begun.';
        WHEN 'trip_completed' THEN
            notification_title := 'Trip Completed';
            notification_body := 'Your trip has been completed. Please rate your experience.';
        WHEN 'cancelled' THEN
            notification_title := 'Booking Cancelled';
            notification_body := 'Your booking has been cancelled.';
        ELSE
            notification_title := 'Booking Update';
            notification_body := 'Your booking status has been updated.';
    END CASE;
    
    -- Notify customer
    IF customer_uuid IS NOT NULL AND NEW.status NOT IN ('driver_assigned') THEN
        PERFORM create_notification(
            customer_uuid,
            'customer',
            notification_title,
            notification_body,
            jsonb_build_object('booking_id', NEW.id)
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to update customer ride count on booking completion
CREATE OR REPLACE FUNCTION update_customer_ride_count()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'trip_completed' AND NEW.customer_id IS NOT NULL THEN
        UPDATE customers
        SET total_rides = total_rides + 1
        WHERE id = NEW.customer_id;
    END IF;
    
    IF NEW.status = 'trip_completed' AND NEW.driver_id IS NOT NULL THEN
        UPDATE drivers
        SET total_trips = total_trips + 1
        WHERE id = NEW.driver_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to handle driver document verification
CREATE OR REPLACE FUNCTION check_driver_documents_complete()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if all required documents are verified
    IF (
        SELECT COUNT(*) = 0
        FROM driver_documents
        WHERE driver_id = NEW.driver_id
          AND document_type IN ('aadhaar_front', 'aadhaar_back', 'pan_card', 'license_front', 'license_back')
          AND verification_status != 'verified'
    ) THEN
        -- Update driver status to pending verification
        UPDATE drivers
        SET status = 'pending_verification'
        WHERE id = NEW.driver_id AND status = 'draft';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to handle driver verification approval
CREATE OR REPLACE FUNCTION handle_driver_approval()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'approved' AND OLD.status != 'approved' THEN
        -- Create driver wallet if not exists
        INSERT INTO driver_wallet (driver_id, balance, pending_balance)
        VALUES (NEW.id, 0, 0)
        ON CONFLICT (driver_id) DO NOTHING;
        
        -- Notify driver
        PERFORM create_notification(
            NEW.id,
            'driver',
            '🎉 Congratulations!',
            'Your account has been verified. You can now start accepting rides!',
            '{}'::JSONB
        );
    END IF;
    
    IF NEW.status = 'rejected' AND OLD.status != 'rejected' THEN
        -- Notify driver
        PERFORM create_notification(
            NEW.id,
            'driver',
            'Account Update',
            'Your account verification was unsuccessful. Please check your documents.',
            '{}'::JSONB
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==================== TRIGGER CREATIONS ====================

-- Customers updated_at trigger
DROP TRIGGER IF EXISTS update_customers_updated_at ON customers;
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Drivers updated_at trigger
DROP TRIGGER IF EXISTS update_drivers_updated_at ON drivers;
CREATE TRIGGER update_drivers_updated_at
    BEFORE UPDATE ON drivers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Vehicles updated_at trigger
DROP TRIGGER IF EXISTS update_vehicles_updated_at ON vehicles;
CREATE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON vehicles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Bookings updated_at trigger
DROP TRIGGER IF EXISTS update_bookings_updated_at ON bookings;
CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Booking status change notification trigger
DROP TRIGGER IF EXISTS notify_booking_status_change ON bookings;
CREATE TRIGGER notify_booking_status_change
    AFTER UPDATE ON bookings
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION notify_booking_status_change();

-- Update customer/driver ride count on completion
DROP TRIGGER IF EXISTS update_ride_count_on_completion ON bookings;
CREATE TRIGGER update_ride_count_on_completion
    AFTER UPDATE ON bookings
    FOR EACH ROW
    WHEN (NEW.status = 'trip_completed' AND OLD.status != 'trip_completed')
    EXECUTE FUNCTION update_customer_ride_count();

-- Driver document verification check
DROP TRIGGER IF EXISTS check_driver_documents ON driver_documents;
CREATE TRIGGER check_driver_documents
    AFTER UPDATE ON driver_documents
    FOR EACH ROW
    WHEN (NEW.verification_status = 'verified')
    EXECUTE FUNCTION check_driver_documents_complete();

-- Driver approval notification
DROP TRIGGER IF EXISTS notify_driver_approval ON drivers;
CREATE TRIGGER notify_driver_approval
    AFTER UPDATE ON drivers
    FOR EACH ROW
    WHEN (NEW.status IN ('approved', 'rejected') AND OLD.status NOT IN ('approved', 'rejected'))
    EXECUTE FUNCTION handle_driver_approval();

-- Payments updated_at trigger
DROP TRIGGER IF EXISTS update_payments_updated_at ON payments;
CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Ratings created_at trigger (no update needed)
-- Notifications created_at trigger (no update needed)
-- SOS alerts handling (if needed)

-- Driver wallet updated_at trigger
DROP TRIGGER IF EXISTS update_driver_wallet_updated_at ON driver_wallet;
CREATE TRIGGER update_driver_wallet_updated_at
    BEFORE UPDATE ON driver_wallet
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
