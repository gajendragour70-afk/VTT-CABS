# VTT CABS - Supabase Setup Guide

This directory contains the Supabase database schema and migrations for the VTT CABS platform.

## 📁 Structure

```
supabase/
├── migrations/
│   ├── 001_initial_schema.sql    # Base tables
│   ├── 002_functions.sql         # Database functions
│   ├── 003_triggers.sql          # Database triggers
│   ├── 004_views.sql             # Useful views
│   └── 005_sample_data.sql        # Demo/sample data
├── functions/                     # Edge functions (future)
├── storage/                       # Storage bucket configs
└── SETUP.md                       # This file
```

## 🚀 Setup Instructions

### Option 1: Using Supabase Dashboard

1. Go to your Supabase project dashboard
2. Navigate to **SQL Editor**
3. Copy and paste the contents of each migration file in order:
   - `001_initial_schema.sql`
   - `002_functions.sql`
   - `003_triggers.sql`
   - `004_views.sql`
   - `005_sample_data.sql` (optional - for demo)

### Option 2: Using Supabase CLI

```bash
# Install Supabase CLI
npm install -g supabase

# Login to Supabase
supabase login

# Link to your project
supabase link --project-ref <your-project-ref>

# Push migrations
supabase db push
```

## 📋 Tables Overview

### Core Tables

| Table | Description |
|-------|-------------|
| `customers` | Customer profiles and wallet |
| `drivers` | Driver profiles, documents, status |
| `vehicles` | Vehicle information and verification |
| `bookings` | Ride bookings with full trip details |
| `payments` | Payment records |
| `ratings` | Trip ratings and reviews |

### Supporting Tables

| Table | Description |
|-------|-------------|
| `admins` | Admin users |
| `notifications` | Push notifications |
| `sos_alerts` | Emergency SOS alerts |
| `driver_documents` | Document verification |
| `driver_earnings` | Earnings breakdown |
| `driver_wallet` | Driver wallet balance |
| `payout_requests` | Payout requests |
| `driver_locations` | Real-time location tracking |

## 🔐 Row Level Security

All tables have RLS enabled with policies for:

- **Customers**: Can only view/update own profile
- **Drivers**: Can only view/update own profile and vehicles
- **Admins**: Full access to all data

## 🔄 Realtime Subscriptions

Enabled for:
- `bookings` - Track booking status changes
- `drivers` - Track driver online status
- `notifications` - Push notifications
- `sos_alerts` - Emergency alerts
- `driver_locations` - Real-time driver locations

## 📨 Notifications

Use Supabase Edge Functions to send push notifications:

```javascript
// Example: Send notification to driver
await supabase.functions.invoke('send-notification', {
  body: {
    userId: driverId,
    title: 'New Booking!',
    body: 'You have a new ride request'
  }
});
```

## 📱 Environment Variables

Update your `.env` files with:

```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-service-key
```

## 🧪 Testing

Run the sample data migration to create demo users:

- Customer: `customer@vttcabs.com` / `password123`
- Driver: `driver@vttcabs.com` / `password123`
- Admin: `admin@vttcabs.com` / `admin123`

## 📞 Support

For issues or questions, create an issue on GitHub.
