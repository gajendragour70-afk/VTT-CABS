import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://qqamfbxqmncgjfxqnfvj.supabase.co';
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFxYW1mYnhxbW5jZ2pmeHFuZnZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4OTc3MDAsImV4cCI6MjA3NjQ3MzcwMH0.demo_key';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

// Database types
export interface Booking {
  id: string;
  user_id: string;
  pickup_location: string;
  drop_location: string;
  pickup_date: string;
  pickup_time: string;
  vehicle_type: 'sedan' | 'ertiga' | 'suv' | 'tempo';
  status: 'pending' | 'confirmed' | 'completed' | 'cancelled';
  total_amount: number;
  created_at: string;
  updated_at: string;
}

export interface User {
  id: string;
  email: string;
  full_name: string;
  phone: string;
  created_at: string;
}
