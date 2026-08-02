import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://qqamfbxqmncgjfxqnfvj.supabase.co';
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFxYW1mYnhxbW5jZ2pmeHFuZnZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4OTc3MDAsImV4cCI6MjA3NjQ3MzcwMH0.demo_key';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

// Database Types
export interface Profile {
  id: string;
  email: string;
  full_name: string;
  phone: string;
  avatar_url?: string;
  role: 'customer' | 'driver' | 'admin';
  is_active: boolean;
  created_at: string;
  updated_at: string;
}

export interface Vehicle {
  id: string;
  driver_id?: string;
  vehicle_number: string;
  vehicle_type: 'sedan' | 'ertiga' | 'suv' | 'tempo';
  vehicle_model: string;
  vehicle_color?: string;
  seats: number;
  is_available: boolean;
  is_verified: boolean;
  image_url?: string;
  created_at: string;
  updated_at: string;
}

export interface Driver {
  id: string;
  user_id: string;
  license_number: string;
  license_expiry?: string;
  is_available: boolean;
  is_verified: boolean;
  current_location_lat?: number;
  current_location_lng?: number;
  rating: number;
  total_rides: number;
  created_at: string;
  updated_at: string;
}

export interface Booking {
  id: string;
  user_id: string;
  driver_id?: string;
  vehicle_id?: string;
  pickup_location: string;
  pickup_lat?: number;
  pickup_lng?: number;
  drop_location: string;
  drop_lat?: number;
  drop_lng?: number;
  pickup_date: string;
  pickup_time: string;
  vehicle_type: 'sedan' | 'ertiga' | 'suv' | 'tempo';
  trip_type: 'oneway' | 'roundtrip';
  status: 'pending' | 'confirmed' | 'in_progress' | 'completed' | 'cancelled';
  estimated_distance?: number;
  estimated_time?: number;
  estimated_price?: number;
  final_price?: number;
  coupon_code?: string;
  discount_amount?: number;
  notes?: string;
  created_at: string;
  updated_at: string;
}

export interface Ride {
  id: string;
  booking_id: string;
  driver_id: string;
  vehicle_id: string;
  start_time?: string;
  end_time?: string;
  actual_pickup_time?: string;
  actual_drop_time?: string;
  actual_distance?: number;
  actual_time?: number;
  actual_price?: number;
  pickup_lat?: number;
  pickup_lng?: number;
  drop_lat?: number;
  drop_lng?: number;
  status: 'assigned' | 'accepted' | 'arrived' | 'started' | 'completed' | 'cancelled';
  otp?: string;
  cancellation_reason?: string;
  driver_rating?: number;
  driver_feedback?: string;
  created_at: string;
  updated_at: string;
}

export interface Payment {
  id: string;
  booking_id: string;
  ride_id?: string;
  user_id: string;
  amount: number;
  payment_method: 'cash' | 'card' | 'upi' | 'wallet';
  payment_status: 'pending' | 'completed' | 'failed' | 'refunded';
  transaction_id?: string;
  payment_gateway?: string;
  gateway_response?: any;
  created_at: string;
  updated_at: string;
}

export interface Wallet {
  id: string;
  user_id: string;
  balance: number;
  created_at: string;
  updated_at: string;
}

export interface WalletTransaction {
  id: string;
  wallet_id: string;
  amount: number;
  transaction_type: 'credit' | 'debit';
  transaction_method: 'payment' | 'refund' | 'topup' | 'bonus' | 'withdrawal';
  description?: string;
  reference_id?: string;
  created_at: string;
}
