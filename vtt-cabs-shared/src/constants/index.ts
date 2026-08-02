// ============================================================================
// VTT CABS - Shared Constants
// ============================================================================

// App Info
export const APP_NAME = 'VTT CABS';
export const APP_VERSION = '1.0.0';

// API URLs (Replace with actual Supabase URL)
export const SUPABASE_URL = import.meta.env.VITE_SUPABASE_URL || 'https://your-project.supabase.co';
export const SUPABASE_ANON_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY || 'your-anon-key';

// Google Maps
export const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || 'your-google-maps-api-key';

// Pricing Constants
export const PRICING = {
  HATCHBACK: {
    baseFare: 50,
    perKm: 10,
    perMinute: 1,
    minimumFare: 80,
  },
  SEDAN: {
    baseFare: 60,
    perKm: 12,
    perMinute: 1.5,
    minimumFare: 100,
  },
  SUV: {
    baseFare: 80,
    perKm: 15,
    perMinute: 2,
    minimumFare: 150,
  },
  AUTO: {
    baseFare: 30,
    perKm: 8,
    perMinute: 0.5,
    minimumFare: 50,
  },
};

// Commission
export const COMMISSION = {
  PLATFORM_FEE_PERCENT: 15, // 15% platform fee
  GST_PERCENT: 18, // 18% GST on commission
};

// Driver Settings
export const DRIVER_SETTINGS = {
  SEARCH_RADIUS_KM: 5, // Default search radius
  MAX_SEARCH_RADIUS_KM: 20,
  BOOKING_REQUEST_TIMEOUT_SEC: 30,
  LOCATION_UPDATE_INTERVAL_MS: 5000, // 5 seconds
  OFFLINE_THRESHOLD_MS: 5 * 60 * 1000, // 5 minutes
};

// OTP Settings
export const OTP_SETTINGS = {
  LENGTH: 6,
  EXPIRY_MINUTES: 5,
  MAX_ATTEMPTS: 3,
  RESEND_COOLDOWN_SEC: 60,
};

// Booking Settings
export const BOOKING_SETTINGS = {
  CANCELLATION_WINDOW_MINUTES: 5,
  DRIVER_ARRIVAL_TIMEOUT_MINUTES: 30,
  TRIP_COMPLETION_GRACE_PERIOD_MINUTES: 5,
};

// Wallet Settings
export const WALLET_SETTINGS = {
  MIN_PAYOUT_AMOUNT: 500,
  PAYOUT_PROCESSING_DAYS: 3,
  CASHBACK_EXPIRY_DAYS: 90,
};

// Referral Settings
export const REFERRAL_SETTINGS = {
  REFEREE_BONUS: 50,
  REFERRER_BONUS: 50,
  MIN_RIDES_FOR_REWARD: 1,
};

// Storage Buckets
export const STORAGE_BUCKETS = {
  PROFILE_PHOTOS: 'profile-photos',
  DRIVER_DOCUMENTS: 'driver-documents',
  VEHICLE_PHOTOS: 'vehicle-photos',
  AADHAAR_DOCUMENTS: 'aadhaar-documents',
  PAN_DOCUMENTS: 'pan-documents',
  LICENSE_DOCUMENTS: 'license-documents',
  RC_DOCUMENTS: 'rc-documents',
  INSURANCE_DOCUMENTS: 'insurance-documents',
  PUC_DOCUMENTS: 'puc-documents',
  CHAT_ATTACHMENTS: 'chat-attachments',
};

// Notification Types
export const NOTIFICATION_CHANNELS = {
  BOOKING: 'booking_notifications',
  SOS: 'sos_alerts',
  PROMO: 'promotional',
  GENERAL: 'general',
};

// Emergency Contacts
export const EMERGENCY_CONTACTS = {
  POLICE: '100',
  AMBULANCE: '108',
  FIRE: '101',
};

// Document Expiry Warning Days
export const DOCUMENT_WARNING_DAYS = {
  RC: 30,
  INSURANCE: 30,
  PUC: 7,
  LICENSE: 30,
};

// Rating Settings
export const RATING_SETTINGS = {
  MIN_RATING: 1,
  MAX_RATING: 5,
  DEFAULT_RATING: 5,
};

// Pagination
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
};

// Date Formats
export const DATE_FORMATS = {
  DISPLAY_DATE: 'DD MMM YYYY',
  DISPLAY_TIME: 'HH:mm',
  DISPLAY_DATETIME: 'DD MMM YYYY, HH:mm',
  ISO_DATE: 'YYYY-MM-DD',
  ISO_DATETIME: 'YYYY-MM-DDTHH:mm:ss',
};

// Validation
export const VALIDATION = {
  MIN_NAME_LENGTH: 2,
  MAX_NAME_LENGTH: 50,
  MIN_PASSWORD_LENGTH: 6,
  MAX_PASSWORD_LENGTH: 100,
  PHONE_LENGTH: 10,
  VEHICLE_NUMBER_PATTERN: /^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$/,
};

// Animation Durations (ms)
export const ANIMATION = {
  FAST: 150,
  NORMAL: 300,
  SLOW: 500,
};

// Breakpoints (Tailwind)
export const BREAKPOINTS = {
  SM: 640,
  MD: 768,
  LG: 1024,
  XL: 1280,
  XXL: 1536,
};
