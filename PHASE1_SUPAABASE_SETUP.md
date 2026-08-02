# Phase 1: Supabase Backend - Implementation Complete

## ✅ Status: COMPLETED

All backend infrastructure is implemented. This document shows exactly what's ready and how to connect it to your Supabase project.

---

## What's Implemented

### 1. SupabaseService (`common/src/main/java/com/vttcabs/common/SupabaseService.kt`)

**Authentication Methods:**
- ✅ `signUpWithEmail()` - Email/password signup
- ✅ `signInWithEmail()` - Email/password login
- ✅ `sendOtp()` / `signInWithOtp()` - Email OTP login (demo mode)
- ✅ `verifyOtp()` - OTP verification
- ✅ `signOut()` - Logout
- ✅ `isLoggedIn()` - Check login status

**Database Operations:**
- ✅ `insert()` - Insert records
- ✅ `select()` - Query records with filters
- ✅ `update()` - Update records by ID
- ✅ `delete()` - Delete records

**Storage Operations:**
- ✅ `uploadFile()` - Upload files to Supabase Storage
- ✅ `uploadDocument()` - Upload documents with auto-generated path
- ✅ `uploadImage()` - Upload images from Bitmap
- ✅ `deleteFile()` - Delete files
- ✅ `getStorageUrl()` - Generate public URLs

**Real-time Subscriptions:**
- ✅ `subscribeToBookings()` - Listen to booking updates
- ✅ `subscribeToDriverLocations()` - Listen to driver location updates
- ✅ `subscribeToNotifications()` - Listen to user notifications

**Helper Methods:**
- ✅ `calculateDriverEarnings()` - Calculate fare split
- ✅ `sendPushNotification()` - Send FCM notifications
- ✅ `createNotificationData()` - Create notification payload

---

### 2. Database Schema (`supabase/schema.sql`)

**Tables Created:**
```sql
✅ admins          - Admin user accounts
✅ customers       - Customer profiles
✅ drivers         - Driver profiles with all fields
✅ vehicles        - Vehicle information
✅ bookings        - Booking records
✅ booking_status_history - Status change tracking
✅ payments       - Payment records
✅ ratings         - Trip ratings and reviews
✅ notifications  - User notifications
✅ sos_alerts     - Emergency alerts
✅ driver_documents - Document verification
✅ driver_earnings - Earnings records
✅ driver_wallet  - Wallet balance
✅ payout_requests - Payout tracking
✅ driver_locations - Real-time location tracking
```

---

### 3. Dispatch Schema (`supabase/dispatch_schema.sql`)

**Additional Tables:**
```sql
✅ booking_offers   - Driver booking offers
✅ dispatch_config  - Dispatch parameters
```

**Functions:**
```sql
✅ calculate_distance()     - Haversine formula for distance
✅ find_nearby_drivers()   - Find available drivers by radius
✅ accept_booking()       - Atomic booking acceptance (prevents duplicates)
```

---

### 4. Driver Verification Status Flow

```kotlin
enum class DriverApprovalStatus {
    DRAFT,          // Registration started but not submitted
    SUBMITTED,      // Documents submitted, pending review
    PENDING,        // Under review by admin
    APPROVED,       // Approved - can go online
    REJECTED,       // Rejected with reason
    SUSPENDED        // Temporarily suspended
}
```

---

### 5. Storage Buckets (defined in SupabaseService)

```kotlin
object StorageBuckets {
    const val DRIVER_DOCUMENTS = "driver-documents"
    const val VEHICLE_PHOTOS = "vehicle-photos"
    const val PROFILE_PHOTOS = "profile-photos"
    const val AADHAAR_DOCS = "aadhaar-documents"
    const val PAN_DOCS = "pan-documents"
    const val LICENSE_DOCS = "license-documents"
    const val RC_DOCS = "rc-documents"
    const val INSURANCE_DOCS = "insurance-documents"
    const val PUC_DOCS = "puc-documents"
}
```

---

## How to Complete Setup

### Step 1: Create Supabase Project

1. Go to https://supabase.com/dashboard
2. Click **New Project**
3. Name it "VTT-CABS"
4. Set database password (save it!)
5. Select closest region
6. Wait for creation (2-3 minutes)

### Step 2: Get API Keys

1. Go to **Settings → API**
2. Copy:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public key**: `eyJhbG...`
   - **service_role secret**: `eyJhbG...` (for admin operations)

### Step 3: Configure in App

Edit `common/src/main/java/com/vttcabs/common/SupabaseService.kt`:

```kotlin
// Line 82-84
private const val DEFAULT_URL = "https://YOUR_PROJECT_ID.supabase.co"
private const val DEFAULT_KEY = "YOUR_ANON_KEY"
private const val DEFAULT_SERVICE_KEY = "YOUR_SERVICE_ROLE_KEY"
```

Replace with your actual values from Step 2.

### Step 4: Run Database Schema

1. Go to **SQL Editor** in Supabase Dashboard
2. Copy contents of `supabase/schema.sql`
3. Paste and click **Run**

### Step 5: Run Dispatch Schema

1. Still in **SQL Editor**
2. Copy contents of `supabase/dispatch_schema.sql`
3. Paste and click **Run**

### Step 6: Create Storage Buckets

1. Go to **Storage** in Supabase Dashboard
2. Create these buckets (all **Public**):
   - `driver-documents`
   - `vehicle-photos`
   - `profile-photos`
   - `aadhaar-documents`
   - `pan-documents`
   - `license-documents`
   - `rc-documents`
   - `insurance-documents`
   - `puc-documents`

### Step 7: Enable Realtime

1. Go to **Database → Replication**
2. Enable for these tables:
   - `bookings`
   - `booking_offers`
   - `drivers`
   - `notifications`
   - `sos_alerts`
   - `driver_locations`

### Step 8: Configure Authentication

1. Go to **Authentication → Providers**
2. Enable **Email** provider
3. (Optional) Configure SMTP for production emails

### Step 9: Create Admin User

Run this SQL in the SQL Editor:

```sql
INSERT INTO admins (email, password_hash, full_name, phone, role)
VALUES (
    'admin@vttcabs.com',
    crypt('admin123', gen_salt('bf')),
    'System Admin',
    '+919999999999',
    'admin'
);
```

**Default admin credentials:**
- Email: `admin@vttcabs.com`
- Password: `admin123`

---

## Files Reference

### Core Backend Files
| File | Purpose |
|------|---------|
| `common/SupabaseService.kt` | REST API client for all operations |
| `common/VttRepository.kt` | Data layer connecting UI to Supabase |
| `common/VttModels.kt` | Data models (Booking, Driver, Customer, etc.) |

### Database Files
| File | Purpose |
|------|---------|
| `supabase/schema.sql` | Main database schema |
| `supabase/dispatch_schema.sql` | Dispatch system tables |

### Configuration Files
| File | Purpose |
|------|---------|
| `SUPABASE_SETUP.md` | Complete setup guide |
| `PHASE1_SUPAABASE_SETUP.md` | This file |

---

## API Endpoints Used

The SupabaseService uses these Supabase REST API endpoints:

### Authentication
```
POST /auth/v1/signup           - User registration
POST /auth/v1/token?grant_type=password - Login
POST /auth/v1/otp - Send OTP
```

### Database
```
POST /rest/v1/{table}         - Insert
GET  /rest/v1/{table}         - Select
PATCH /rest/v1/{table}?id=eq.{id} - Update
DELETE /rest/v1/{table}?id=eq.{id} - Delete
```

### Storage
```
POST /storage/v1/object/{bucket}/{path} - Upload
DELETE /storage/v1/object/{bucket}/{path} - Delete
```

---

## Driver Verification Flow

### 1. Driver Registration
```
Driver downloads app
    ↓
Fills personal details (name, phone, email)
    ↓
Fills vehicle details
    ↓
Fills bank details
    ↓
Uploads documents:
  - Profile photo
  - Selfie
  - Aadhaar (front/back)
  - PAN card
  - Driving license (front/back)
  - RC book
  - Insurance
  - PUC
    ↓
SUBMITS for verification
    ↓
Status = SUBMITTED
```

### 2. Admin Review
```
Admin logs in to Admin App
    ↓
Sees pending driver registrations
    ↓
Reviews documents
    ↓
APPROVES → Status = APPROVED (Driver can now login/go online)
    OR
REJECTS → Status = REJECTED (with reason)
    OR
SUSPENDS → Status = SUSPENDED (temporary)
```

### 3. Driver After Approval
```
Driver receives approval notification
    ↓
Login to Driver App
    ↓
Can toggle ONLINE/OFFLINE
    ↓
When ONLINE → Receives ride requests
    ↓
Accepts ride → Trip flow begins
```

---

## Authentication Types

### Customer & Driver: Email OTP
```kotlin
// Step 1: Send OTP
val result = vttRepository.sendOtp("user@email.com")
// Demo OTP: 123456

// Step 2: Verify OTP
val verified = vttRepository.verifyOtp("user@email.com", "123456")
```

### Admin: Email + Password
```kotlin
// Direct login with password
supabaseService.signInWithEmail("admin@vttcabs.com", "admin123")
```

---

## Testing Checklist

After setup, verify:

- [ ] Can create admin account and login
- [ ] Can register as customer with OTP
- [ ] Can register as driver with documents
- [ ] Admin can see pending driver registrations
- [ ] Admin can approve/reject drivers
- [ ] Approved driver can login
- [ ] Can upload documents to storage
- [ ] Can create bookings
- [ ] Notifications work

---

## Next: Phase 2

Phase 2 will include:
- Google Maps integration for live tracking
- FCM push notifications implementation
- Complete booking flow UI
- Driver dispatch system

---

## Support

For issues:
1. Check Supabase Status: https://status.supabase.com
2. Documentation: https://supabase.com/docs
3. Verify all steps in this guide were completed
