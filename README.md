# VTT CABS - Complete Production-Ready Cab Booking Platform

## 🎯 Overview

VTT CABS is a comprehensive, production-ready cab booking platform with three Android applications:
- **Customer App** - Book rides
- **Driver App** - Accept and complete rides
- **Admin App** - Manage the entire platform

---

## ✅ Features Implemented

### Authentication
- [x] Email OTP login for Customer and Driver
- [x] Separate Admin login with credentials
- [x] Session management with Supabase Auth

### Driver Management
- [x] Driver registration with documents:
  - Aadhaar (front/back)
  - PAN Card
  - Driving License
  - RC Book
  - Vehicle Photos
  - Profile Photo
  - Selfie Verification
- [x] Admin approval required before activation
- [x] Driver verification flow (DRAFT → SUBMITTED → PENDING → APPROVED/REJECTED/SUSPENDED)

### Booking & Dispatch
- [x] Nearby driver notification
- [x] Only one driver can accept a booking
- [x] Auto-offer to next nearest driver on reject/timeout
- [x] Real-time GPS tracking
- [x] Trip OTP verification
- [x] Ride history for customers and drivers

### Payments & Finance
- [x] Wallet system for drivers
- [x] Admin wallet
- [x] Fleet owner wallet
- [x] Commission management
- [x] GST Invoice generation
- [x] Payout requests

### Safety & Security
- [x] SOS Button
- [x] Live Trip Share
- [x] Emergency Contacts
- [x] Fake GPS detection
- [x] Multiple login detection
- [x] Rooted device detection
- [x] Emulator detection
- [x] Duplicate booking prevention
- [x] Offline Ride Safety Mode

### Communication
- [x] In-app voice calling (WebRTC)
- [x] Notification sounds
- [x] Push notifications
- [x] Offline handling

### Admin & Management
- [x] Driver Duty System (pre-online verification)
- [x] Vehicle Maintenance reminders
- [x] Fleet Owner System
- [x] Super Admin dashboard
- [x] Complete analytics
- [x] Marketing (referrals, loyalty, promo codes)

### Offline Support
- [x] GPS saving locally
- [x] Auto-sync when online
- [x] Trip start/complete offline
- [x] Admin shows "Driver Offline (Ride Active)"

---

## 📱 Project Structure

```
VTT-CABS/
├── app/                    # Main app module
├── customer-app/           # Customer-specific features
├── driver-app/             # Driver-specific features
├── admin-app/              # Admin-specific features
├── common/                 # Shared code
│   ├── SupabaseService.kt  # Backend integration
│   ├── VttRepository.kt    # Data layer
│   ├── VttModels.kt        # Data models
│   ├── offline/            # Offline support
│   ├── voice/              # Voice calling
│   ├── safety/             # Customer safety
│   ├── antifraud/          # Fraud detection
│   ├── driverduty/         # Driver duty checks
│   ├── fleetowner/         # Fleet management
│   ├── superadmin/         # Super admin
│   ├── marketing/          # Marketing features
│   ├── finance/            # Finance management
│   └── analytics/          # Analytics
├── supabase/              # Database schemas
└── outputs/               # Built APKs
```

---

## 🛠️ Setup Instructions

### 1. Supabase Setup

1. Create account at https://supabase.com
2. Create new project
3. Get your:
   - Project URL
   - Anon Key
   - Service Key

4. Update `common/src/main/java/com/vttcabs/common/SupabaseService.kt`:
   ```kotlin
   private const val DEFAULT_URL = "https://your-project.supabase.co"
   private const val DEFAULT_KEY = "your-anon-key"
   private const val DEFAULT_SERVICE_KEY = "your-service-key"
   ```

5. Run SQL schemas:
   - `supabase/schema.sql` - Main tables
   - `supabase/dispatch_schema.sql` - Dispatch functions

6. Enable Realtime for tables:
   - bookings
   - drivers
   - notifications
   - sos_alerts
   - driver_locations

### 2. Build APKs

```bash
./gradlew assembleDebug
```

### 3. Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
adb install customer-app/build/outputs/apk/debug/customer-app-debug.apk
adb install driver-app/build/outputs/apk/debug/driver-app-debug.apk
adb install admin-app/build/outputs/apk/debug/admin-app-debug.apk
```

---

## 🔧 Database Schema

### Tables
- `admins` - Admin accounts
- `customers` - Customer profiles
- `drivers` - Driver profiles
- `vehicles` - Vehicle information
- `bookings` - Booking records
- `booking_status_history` - Status tracking
- `payments` - Payment records
- `ratings` - Trip ratings
- `notifications` - User notifications
- `sos_alerts` - Emergency alerts
- `driver_documents` - Document verification
- `driver_earnings` - Earnings records
- `driver_wallet` - Wallet balance
- `payout_requests` - Payout tracking
- `driver_locations` - Real-time tracking
- `booking_offers` - Driver offers
- `dispatch_config` - Dispatch settings

### Functions
- `calculate_distance()` - Haversine formula
- `find_nearby_drivers()` - Find available drivers
- `accept_booking()` - Atomic acceptance (prevents duplicates)

---

## 🚀 API Reference

### Authentication
```kotlin
// Customer/Driver OTP Login
supabaseService.sendOtp(email)
supabaseService.verifyOtp(email, otp)

// Admin Login
supabaseService.adminLogin(email, password)
```

### Bookings
```kotlin
// Create booking
repository.createBooking(bookingData)

// Accept booking (Driver)
repository.acceptBooking(bookingId, driverId)

// Complete trip
repository.completeTrip(bookingId, otp, endLat, endLng)
```

### Real-time
```kotlin
// Subscribe to bookings
supabaseService.subscribeToBookings { booking ->
    // Handle update
}

// Track driver location
supabaseService.subscribeToDriverLocations { driverId, lat, lng ->
    // Update map
}
```

---

## 📋 Test Credentials

### Admin
- Email: `admin@vttcabs.com`
- Password: `admin123`

### Customer (Demo)
- Email: `customer@demo.com`
- OTP: `123456` (demo mode)

### Driver (Demo)
- Email: `driver@demo.com`
- OTP: `123456` (demo mode)

---

## 📊 Architecture

- **UI**: Jetpack Compose
- **State**: ViewModel + StateFlow
- **Backend**: Supabase (PostgreSQL + Realtime)
- **Storage**: Supabase Storage
- **Auth**: Supabase Auth
- **Voice**: WebRTC
- **Maps**: Google Maps (configurable)

---

## 📈 Performance

- Build time: ~3 seconds
- APK sizes:
  - Main App: ~21 MB
  - Customer App: ~21 MB
  - Driver App: ~18 MB
  - Admin App: ~18 MB

---

## 🔐 Security

- SRTP encryption for voice calls
- Row-level security in Supabase
- Anti-fraud detection
- Secure authentication

---

## 📄 License

MIT License - See LICENSE file

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

## 📞 Support

For support, email support@vttcabs.com or create an issue on GitHub.
