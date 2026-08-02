# Smart Ride Dispatch System

This document describes the Uber/Bolt-style smart ride dispatch system implemented in VTT-CABS.

## Overview

The dispatch system automatically matches customer bookings with nearby available drivers, handling:
- Real-time driver location tracking
- Intelligent radius expansion
- Duplicate acceptance prevention
- Admin override capabilities
- Instant status updates

## Booking Status Flow

```
NEW
  ↓
SEARCHING_DRIVER (automatically starts dispatch)
  ↓
┌─────────────────────────────────────────────┐
│ DRIVER_ASSIGNED ← Admin can manually assign │
│        ↓                                    │
│ DRIVER_ACCEPTED (first driver to accept)    │
│        ↓                                    │
│ DRIVER_ARRIVED                              │
│        ↓                                    │
│ OTP_VERIFIED                                 │
│        ↓                                    │
│ TRIP_STARTED                                 │
│        ↓                                    │
│ TRIP_COMPLETED                               │
│        ↓                                    │
│ PAYMENT_PENDING → PAYMENT_COMPLETED          │
└─────────────────────────────────────────────┘

Alternative flows:
- DRIVER_REJECTED → SEARCH_NEXT_DRIVER (loop)
- CANCELLED (by customer, driver, or admin)
```

## Driver Matching Criteria

A driver receives booking offers only if ALL conditions are met:
1. ✅ Driver is **Online** (`is_online = true`)
2. ✅ Driver is **Available** (`is_available = true`)
3. ✅ Driver is **Approved** by admin (`status = 'active'`)
4. ✅ Driver has **GPS enabled** (has valid lat/lng)
5. ✅ Driver is **Not on another trip** (`current_booking_id IS NULL`)
6. ✅ Driver is **Not suspended** (`is_active = true`)
7. ✅ Driver is within **search radius**

## Dispatch Algorithm

### 1. Initial Search
- Customer creates booking
- System searches for drivers within **5km** (configurable)
- Offers sent to up to **5 nearest drivers**
- Each offer valid for **60 seconds** (configurable)

### 2. Driver Response
- **Accept**: Booking locked to that driver, other offers cancelled
- **Reject**: System tries next driver
- **Timeout**: System tries next driver

### 3. Radius Expansion
If no driver accepts within timeout:
- Radius expands by **5km** (configurable)
- New drivers searched (excluding already-offered)
- Process repeats until:
  - Driver accepts → proceed
  - Max radius reached (**20km**, configurable)
  - Max rejections reached (**3**, configurable)

### 4. Fallback
After max attempts, booking requires **manual admin assignment**.

## Configuration

Edit `dispatch_config` table:

| Key | Default | Description |
|-----|---------|-------------|
| `initial_search_radius_km` | 5 | Starting search radius |
| `max_search_radius_km` | 20 | Maximum search radius |
| `search_timeout_seconds` | 30 | Seconds before expanding |
| `max_rejection_count` | 3 | Max rejections before manual |
| `booking_offer_validity_seconds` | 60 | Offer expiration time |

## Database Schema

### New Tables

**`booking_offers`**
```sql
- id (UUID)
- booking_id (FK)
- driver_id (FK)
- status (pending/accepted/rejected/expired/cancelled)
- offered_at
- expires_at
- distance_km
- eta_minutes
```

**`dispatch_config`**
```sql
- id (UUID)
- config_key (TEXT, UNIQUE)
- config_value (TEXT)
- description
```

### Updated Tables

**`bookings`**
- Added: `vehicle_category`, `current_search_radius_km`, `search_attempts`, `rejection_count`, `last_driver_notified_at`, `timeout_at`
- Updated: `status` enum with new states

**`drivers`**
- Added: `current_booking_id` (prevents double-booking)

## API Reference

### DispatchService (Android)

```kotlin
// Start automatic dispatch
DispatchService.getInstance().startDispatch(booking)

// Cancel dispatch
DispatchService.getInstance().cancelDispatch(bookingId)

// Driver accepts booking
DispatchService.getInstance().acceptBooking(bookingId, driverId)

// Driver rejects booking
DispatchService.getInstance().rejectBooking(bookingId, driverId, reason)

// Admin manual assignment
DispatchService.getInstance().adminAssignDriver(bookingId, driverId, adminId)

// Admin cancel booking
DispatchService.getInstance().adminCancelBooking(bookingId, reason, adminId)

// Listen to status updates
DispatchService.getInstance().bookingStatusUpdates.collect { update ->
    // Handle booking status change
}
```

### Database Functions (PostgreSQL)

```sql
-- Find nearby drivers
SELECT * FROM find_nearby_drivers(
    28.6139,  -- pickup latitude
    77.2090,  -- pickup longitude
    5.0,      -- radius in km
    'sedan',   -- vehicle category (optional)
    NULL       -- exclude driver ID (optional)
);

-- Atomically accept booking (prevents duplicates)
SELECT accept_booking(
    'booking-uuid',
    'driver-uuid',
    'offer-uuid'
);
```

## Real-time Updates

### Supabase Realtime Tables
- `bookings` - Status changes
- `booking_offers` - Offer status
- `drivers` - Location & availability
- `notifications` - Push notifications
- `driver_locations` - Live tracking

### Client Subscriptions
```kotlin
// Customer subscribes to booking updates
supabase.from("bookings")
    .subscribeToChanges()
    .onUpdate { /* update UI */ }

// Driver subscribes to new offers
supabase.from("booking_offers")
    .subscribeToChanges()
    .filter("driver_id", Filter.equals(myId))
    .filter("status", Filter.equals("pending"))
    .onInsert { /* show offer notification */ }

// Admin subscribes to all bookings
supabase.from("bookings")
    .subscribeToChanges()
    .onInsert { /* new booking alert */ }
```

## Duplicate Prevention

The `accept_booking()` function uses **row-level locking**:

```sql
SELECT status, driver_id FROM bookings 
WHERE id = p_booking_id 
FOR UPDATE;  -- Locks the row

-- Check if already accepted
IF v_current_driver_id IS NOT NULL THEN
    RETURN FALSE;
END IF;

-- Proceed with assignment
UPDATE bookings SET driver_id = p_driver_id ...
```

This ensures two drivers cannot accept the same booking simultaneously.

## Admin Controls

### Dashboard Actions
1. **View All Bookings** - Real-time list with status filters
2. **Manual Assignment** - Assign any available driver
3. **Reassign Driver** - Change assigned driver
4. **Cancel Booking** - With reason
5. **Force Complete** - Complete trip manually
6. **View Driver Locations** - Live map

### Override Flow
```
Customer cancels → Admin can reassign to new driver
Driver doesn't move → Admin can contact or reassign
No drivers available → Admin manual assignment
```

## Setup Instructions

### 1. Run Database Schema
```sql
-- First run main schema
\supabase\schema.sql

-- Then run dispatch additions
\supabase\dispatch_schema.sql
```

### 2. Enable Realtime
In Supabase Dashboard:
1. Go to Database → Replication
2. Add tables: `bookings`, `booking_offers`, `drivers`, `notifications`, `driver_locations`

### 3. Configure Storage Policies
```sql
-- Allow drivers to view nearby bookings (their offers)
CREATE POLICY "Drivers can view own offers" ON booking_offers
FOR SELECT USING (driver_id IN (
    SELECT id FROM drivers 
    WHERE auth_user_id = auth.uid()
));
```

### 4. Update Mobile App
```kotlin
// Initialize dispatch service
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseService.initialize(this)
    }
}

// Start dispatch when booking created
viewModelScope.launch {
    val booking = createBooking(...)
    DispatchService.getInstance().startDispatch(booking)
}

// Listen to updates
lifecycleScope.launch {
    DispatchService.getInstance().bookingStatusUpdates.collect { update ->
        when (update.status) {
            "driver_accepted" -> showDriverAccepted(update.driverId)
            "cancelled" -> showCancellation(update.reason)
        }
    }
}
```

## Testing

### Manual Test Flow
1. Create customer account
2. Create driver account (approved)
3. Driver goes online
4. Customer books ride
5. Driver receives offer notification
6. Driver accepts/rejects
7. Verify status updates in all apps

### Load Test
```bash
# Simulate multiple drivers
for i in {1..10}; do
    curl -X POST https://your-api/drivers \
        -d "{\"driver_id\": \"$i\", \"lat\": 28.61, \"lng\": 77.20}"
done

# Simulate booking
curl -X POST https://your-api/bookings \
    -d "{\"customer_id\": \"c1\", \"lat\": 28.61, \"lng\": 77.20}"
```

## Troubleshooting

### No drivers found
- Check `drivers` table has `is_online=true` and `status='active'`
- Verify GPS coordinates are valid
- Check `current_booking_id` is NULL

### Offers not appearing
- Check RLS policies allow driver to see offers
- Verify FCM tokens are set for push notifications
- Check `booking_offers` table for new records

### Duplicate acceptance
- Verify `accept_booking()` function is being called
- Check database user has permission to use `FOR UPDATE`

### Radius not expanding
- Check `search_timeout_seconds` is not too long
- Verify `max_search_radius_km` is set correctly
- Check dispatches aren't being cancelled prematurely

## Performance Considerations

- Driver location updates: max 1 per 5 seconds
- Database indexes on frequently queried columns
- Connection pooling for dispatch queries
- Realtime subscription limits per client
