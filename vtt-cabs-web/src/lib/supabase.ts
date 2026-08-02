import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://qqamfbxqmncgjfxqnfvj.supabase.co';
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFxYW1mYnhxbW5jZ2pmeHFuZnZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4OTc3MDAsImV4cCI6MjA3NjQ3MzcwMH0.demo_key';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

// Database Types matching the schema
export interface Admin {
  id: string;
  email: string;
  password_hash: string;
  full_name: string;
  phone?: string;
  role: string;
  is_active: boolean;
  created_at: string;
  updated_at: string;
}

export interface Customer {
  id: string;
  auth_user_id?: string;
  email: string;
  full_name: string;
  phone: string;
  profile_image_url?: string;
  wallet_balance: number;
  total_rides: number;
  is_active: boolean;
  fcm_token?: string;
  created_at: string;
  updated_at: string;
}

export interface Driver {
  id: string;
  auth_user_id?: string;
  email: string;
  full_name: string;
  phone: string;
  profile_image_url?: string;
  date_of_birth?: string;
  gender?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  bank_account_number?: string;
  bank_ifsc?: string;
  bank_name?: string;
  upi_id?: string;
  emergency_contact_name?: string;
  emergency_contact_phone?: string;
  emergency_contact_relation?: string;
  aadhaar_front_url?: string;
  aadhaar_back_url?: string;
  pan_card_url?: string;
  driving_license_front_url?: string;
  driving_license_back_url?: string;
  rc_doc_url?: string;
  insurance_url?: string;
  pollution_certificate_url?: string;
  driver_selfie_url?: string;
  status: 'draft' | 'submitted' | 'pending_verification' | 'approved' | 'rejected' | 'suspended' | 'active';
  is_online: boolean;
  is_available: boolean;
  current_latitude?: number;
  current_longitude?: number;
  last_location_update?: string;
  current_vehicle_id?: string;
  total_trips: number;
  average_rating: number;
  wallet_balance: number;
  pending_payout: number;
  is_active: boolean;
  fcm_token?: string;
  rejection_reason?: string;
  created_at: string;
  updated_at: string;
}

export interface Vehicle {
  id: string;
  driver_id?: string;
  vehicle_number: string;
  vehicle_type: 'auto' | 'hatchback' | 'sedan' | 'suv' | 'luxury';
  vehicle_model: string;
  vehicle_brand: string;
  vehicle_color?: string;
  vehicle_year?: number;
  rc_front_url?: string;
  rc_back_url?: string;
  insurance_url?: string;
  pollution_certificate_url?: string;
  vehicle_front_url?: string;
  vehicle_back_url?: string;
  vehicle_left_url?: string;
  vehicle_right_url?: string;
  is_active: boolean;
  is_verified: boolean;
  verification_status: 'pending' | 'verified' | 'rejected';
  created_at: string;
  updated_at: string;
}

export interface Booking {
  id: string;
  booking_number: string;
  customer_id?: string;
  driver_id?: string;
  vehicle_id?: string;
  booking_type: 'local' | 'rental' | 'airport' | 'outstation' | 'one_way' | 'round_trip';
  pickup_address: string;
  pickup_latitude: number;
  pickup_longitude: number;
  drop_address?: string;
  drop_latitude?: number;
  drop_longitude?: number;
  waypoints?: any[];
  trip_date: string;
  trip_time?: string;
  estimated_pickup_time?: string;
  actual_pickup_time?: string;
  actual_drop_time?: string;
  estimated_distance_km?: number;
  actual_distance_km?: number;
  estimated_duration_minutes?: number;
  actual_duration_minutes?: number;
  base_fare: number;
  distance_fare: number;
  time_fare: number;
  surge_multiplier: number;
  coupon_discount: number;
  toll_charges: number;
  total_fare: number;
  driver_earnings: number;
  platform_fee: number;
  status: 'requested' | 'confirmed' | 'driver_assigned' | 'otp_verified' | 'trip_started' | 'trip_completed' | 'cancelled' | 'payment_pending' | 'payment_completed';
  trip_otp?: string;
  driver_current_latitude?: number;
  driver_current_longitude?: number;
  cancelled_by?: string;
  cancellation_reason?: string;
  cancellation_time?: string;
  customer_notes?: string;
  driver_notes?: string;
  created_at: string;
  updated_at: string;
}

export interface Payment {
  id: string;
  booking_id?: string;
  customer_id?: string;
  driver_id?: string;
  amount: number;
  payment_method?: 'cash' | 'card' | 'upi' | 'wallet' | 'online';
  payment_status: 'pending' | 'processing' | 'completed' | 'failed' | 'refunded';
  transaction_id?: string;
  gateway_transaction_id?: string;
  gateway_response?: any;
  driver_share: number;
  platform_share: number;
  coupon_id?: string;
  coupon_discount: number;
  created_at: string;
  updated_at: string;
}

export interface Rating {
  id: string;
  booking_id?: string;
  from_user_id: string;
  from_user_type: 'customer' | 'driver';
  to_user_id: string;
  to_user_type: 'customer' | 'driver';
  rating: number;
  review?: string;
  quick_tags?: string[];
  is_visible: boolean;
  created_at: string;
}

export interface Notification {
  id: string;
  user_id: string;
  user_type: 'customer' | 'driver' | 'admin';
  title: string;
  body: string;
  data?: any;
  is_read: boolean;
  created_at: string;
}

export interface DriverLocation {
  id: string;
  driver_id: string;
  latitude: number;
  longitude: number;
  bearing?: number;
  speed?: number;
  accuracy?: number;
  is_active: boolean;
  created_at: string;
}

export interface DriverEarning {
  id: string;
  driver_id: string;
  booking_id?: string;
  gross_amount: number;
  platform_fee: number;
  gst: number;
  toll_charges: number;
  incentive: number;
  penalty: number;
  net_earning: number;
  trip_date: string;
  settlement_status: 'pending' | 'processing' | 'settled';
  settled_at?: string;
  created_at: string;
}

export interface DriverWallet {
  id: string;
  driver_id: string;
  balance: number;
  pending_balance: number;
  lifetime_earnings: number;
  lifetime_payouts: number;
  created_at: string;
  updated_at: string;
}

// Helper function to check if user is authenticated
export const isAuthenticated = async (): Promise<boolean> => {
  const { data: { session } } = await supabase.auth.getSession();
  return !!session;
};

// Helper function to get current user
export const getCurrentUser = async () => {
  const { data: { user } } = await supabase.auth.getUser();
  return user;
};

// Helper function to get customer profile
export const getCustomerProfile = async (authUserId: string) => {
  const { data, error } = await supabase
    .from('customers')
    .select('*')
    .eq('auth_user_id', authUserId)
    .single();
  if (error) throw error;
  return data as Customer;
};

// Helper function to get driver profile
export const getDriverProfile = async (authUserId: string) => {
  const { data, error } = await supabase
    .from('drivers')
    .select('*')
    .eq('auth_user_id', authUserId)
    .single();
  if (error) throw error;
  return data as Driver;
};
