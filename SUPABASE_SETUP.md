# VTT-CABS Supabase Setup Guide

This guide will help you set up Supabase for the VTT-CABS cab booking application.

## Prerequisites

- A Supabase account (sign up at https://supabase.com)
- Android Studio (for running the app)
- Git (for version control)

---

## Step 1: Create a Supabase Project

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Click "New Project"
3. Enter a project name (e.g., "VTT-CABS")
4. Set a strong database password
5. Select the closest region
6. Click "Create new project"
7. Wait for the project to be created (2-3 minutes)

---

## Step 2: Get Your API Keys

1. Go to **Settings** → **API**
2. Copy the following values:
   - **Project URL**: `https://xxxxxxxxxxxx.supabase.co`
   - **anon public key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - **service_role secret key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (for admin operations)

---

## Step 3: Run the Database Schema

1. Go to **SQL Editor** in your Supabase dashboard
2. Copy the contents of `supabase/schema.sql`
3. Paste it into the SQL Editor
4. Click **Run** to execute the schema

This will create all the necessary tables:
- `admins` - Admin users
- `customers` - Customer profiles
- `drivers` - Driver profiles
- `vehicles` - Vehicle information
- `bookings` - Booking records
- `booking_status_history` - Status change history
- `payments` - Payment records
- `ratings` - Trip ratings
- `notifications` - User notifications
- `sos_alerts` - Emergency alerts
- `driver_documents` - Document verification
- `driver_earnings` - Earnings records
- `driver_wallet` - Wallet balance
- `payout_requests` - Payout requests
- `driver_locations` - Real-time location tracking

---

## Step 4: Configure Storage Buckets

1. Go to **Storage** in your Supabase dashboard
2. Create the following buckets with **Public** access:

### Bucket Names:
| Bucket Name | Public | Description |
|-------------|--------|-------------|
| `driver-documents` | Yes | Driver ID proofs, license, etc. |
| `vehicle-photos` | Yes | Vehicle images |
| `profile-photos` | Yes | User profile pictures |
| `aadhaar-documents` | Yes | Aadhaar card copies |
| `pan-documents` | Yes | PAN card copies |
| `license-documents` | Yes | Driving license copies |
| `rc-documents` | Yes | RC book copies |
| `insurance-documents` | Yes | Insurance documents |
| `puc-documents` | Yes | Pollution certificate copies |

3. Set up storage policies for each bucket to allow authenticated users to upload their own files.

---

## Step 5: Configure Authentication

### Enable Email OTP
1. Go to **Authentication** → **Providers**
2. Make sure **Email** is enabled
3. Configure email settings:
   - Enable "Confirm email"
   - Disable "Enable Sign Up" (for production - admins create accounts)
   - Set up SMTP for production (optional for development)

### Create Admin User
Run this SQL to create your first admin:
```sql
INSERT INTO admins (email, password_hash, full_name, phone, role)
VALUES ('admin@yourcompany.com', '$2a$10$YourHashedPassword', 'Admin Name', '+919999999999', 'admin');
```

To hash a password, use this SQL:
```sql
SELECT crypt('YourPassword123', gen_salt('bf'));
```

---

## Step 6: Update the App Configuration

Edit `SupabaseService.kt` in the common module:

```kotlin
// Replace these with your actual values
private const val DEFAULT_URL = "https://your-project-id.supabase.co"
private const val DEFAULT_KEY = "your-anon-key"
```

Or better, use environment variables / local.properties:

```kotlin
private const val DEFAULT_URL = BuildConfig.SUPABASE_URL
private const val DEFAULT_KEY = BuildConfig.SUPABASE_ANON_KEY
```

---

## Step 7: Enable Realtime

1. Go to **Database** → **Replication**
2. Enable replication for these tables:
   - `bookings`
   - `drivers`
   - `notifications`
   - `sos_alerts`
   - `driver_locations`

---

## Step 8: Set Up Edge Functions (Optional)

For email OTP verification, you can create a Supabase Edge Function:

1. Install Supabase CLI
2. Create an edge function for sending OTP emails
3. Deploy to your project

---

## Testing the Integration

### 1. Start the Android App
```bash
cd VTT-CABS
./gradlew assembleDebug
```

### 2. Test Customer Flow
1. Open the Customer app
2. Enter email and request OTP
3. Enter demo OTP: `123456`
4. Complete registration
5. Book a ride

### 3. Test Driver Flow
1. Open the Driver app
2. Complete registration with documents
3. Submit for verification
4. Admin approves in Admin app
5. Driver can go online

### 4. Test Admin Flow
1. Open Admin app
2. Login with admin credentials
3. Review driver applications
4. Approve/Reject drivers
5. Monitor bookings

---

## Troubleshooting

### "No backend is configured" error
- Make sure you've updated `DEFAULT_URL` and `DEFAULT_KEY` in `SupabaseService.kt`
- Verify your Supabase project is active

### Authentication errors
- Check that Email provider is enabled
- Verify RLS policies allow the operation
- Check browser console for CORS errors

### Storage upload fails
- Ensure buckets are set to Public
- Verify storage policies allow uploads
- Check file size limits (default 50MB)

### Realtime not working
- Enable replication for the table in Database → Replication
- Check network connection
- Verify SSL is enabled

---

## Environment Variables (Recommended)

Create a `local.properties` file:
```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_KEY=your-service-role-key
```

Then access in code:
```kotlin
val supabaseUrl = project.rootProject.file("local.properties")
    .inputStream().use { props -> Properties().apply { load(props) } }
    .getProperty("SUPABASE_URL")
```

---

## Security Checklist

- [ ] Change default admin credentials
- [ ] Enable email confirmation
- [ ] Set up RLS policies for all tables
- [ ] Configure storage bucket policies
- [ ] Enable SSL
- [ ] Set up rate limiting
- [ ] Configure backup strategy
- [ ] Set up monitoring alerts

---

## Support

For issues:
1. Check Supabase Status: https://status.supabase.com
2. Documentation: https://supabase.com/docs
3. Discord: https://discord.gg/supabase

---

## Next Steps

After setup:
1. Build the APKs
2. Test all user flows
3. Deploy to Google Play Store (optional)
4. Set up production SMTP for emails
5. Configure push notifications (FCM)
