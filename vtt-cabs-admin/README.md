# VTT CABS - Admin Android App

Android application for VTT CABS administrators to manage drivers, bookings, and analytics.

## 🛡️ Features

- **Dashboard**: Overview of all key metrics
- **Driver Management**: Approve, reject, suspend drivers
- **Booking Management**: View and manage all bookings
- **Analytics**: Revenue, rides, and performance metrics
- **User Management**: View and manage customers
- **Settings**: App configuration

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Backend**: Supabase

## 📁 Project Structure

```
app/src/main/java/com/vttcabs/admin/
├── VttAdminApp.kt           # Application class
├── data/                    # Data layer
├── di/                      # Dependency injection
├── domain/                  # Domain layer
├── ui/                      # Presentation layer
│   ├── auth/                # Login screen
│   ├── dashboard/           # Main dashboard
│   ├── drivers/             # Driver management
│   ├── bookings/            # Booking management
│   ├── analytics/           # Analytics screens
│   └── settings/            # Settings
└── util/                    # Utilities
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Supabase project

### Setup

1. Clone the repository
2. Open in Android Studio
3. Add `google-services.json` to `app/`
4. Build and run

### Demo Credentials

- Email: `admin@vttcabs.com`
- Password: `admin123`

## 📄 License

MIT License
