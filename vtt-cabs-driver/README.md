# VTT CABS - Driver Android App

Android application for VTT CABS drivers to accept and manage rides.

## 🚗 Features

- **Authentication**: Email OTP login
- **Online/Offline Status**: Toggle availability
- **Booking Management**: Accept/reject ride requests
- **Trip Tracking**: Start, track, and complete trips
- **Earnings Dashboard**: View daily/weekly/monthly earnings
- **Ride History**: Complete ride history with earnings
- **Profile Management**: Edit profile, manage documents
- **Navigation**: Integrated Google Maps navigation
- **Customer Contact**: In-app calling (no phone number exposure)
- **SOS Emergency**: Emergency alert button

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Backend**: Supabase
- **Maps**: Google Maps SDK
- **Location**: Google Play Services Location
- **Background**: WorkManager + Foreground Service
- **Storage**: DataStore Preferences

## 📁 Project Structure

```
app/src/main/java/com/vttcabs/driver/
├── VttDriverApp.kt           # Application class
├── data/                      # Data layer
│   ├── api/                  # API clients
│   ├── model/                # Data models
│   ├── repository/           # Repository implementations
│   └── service/              # Background services
├── di/                        # Dependency injection
├── domain/                    # Domain layer
│   ├── model/                # Domain models
│   ├── repository/          # Repository interfaces
│   └── usecase/              # Use cases
├── ui/                        # Presentation layer
│   ├── auth/                 # Login/Register screens
│   ├── booking/              # Booking detail screens
│   ├── dashboard/            # Main dashboard
│   ├── history/              # Ride history
│   ├── navigation/           # Navigation setup
│   ├── profile/              # Profile screens
│   └── theme/                # App theme
└── util/                      # Utilities
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Google Maps API key
- Supabase project

### Setup

1. Clone the repository
2. Open in Android Studio
3. Create `local.properties` with:
   ```
   MAPS_API_KEY=your-maps-api-key
   SUPABASE_URL=your-supabase-url
   SUPABASE_ANON_KEY=your-anon-key
   ```

4. Add `google-services.json` to `app/`
5. Build and run

### Demo Credentials

- Any email
- OTP: `123456`

## 📱 Screens

- **Login**: Email OTP authentication
- **Register**: New driver registration
- **Dashboard**: Main screen with online toggle and booking requests
- **Booking Detail**: Trip details with customer info and OTP
- **History**: Past rides and earnings
- **Profile**: Driver profile and settings

## 🔧 Configuration

### Supabase Tables Required

- `drivers` - Driver profiles
- `bookings` - Ride bookings
- `vehicles` - Vehicle information
- `driver_documents` - Document verification
- `driver_locations` - Real-time location tracking
- `ratings` - Trip ratings

### Permissions Required

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `ACCESS_BACKGROUND_LOCATION`
- `FOREGROUND_SERVICE`
- `POST_NOTIFICATIONS`
- `CAMERA`
- `CALL_PHONE`

## 📄 License

MIT License
