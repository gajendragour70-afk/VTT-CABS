import { supabase } from './supabase';
import type { User } from '@/types';

export interface SendOTPRequest {
  email: string;
}

export interface VerifyOTPRequest {
  email: string;
  otp: string;
}

export interface SignUpRequest {
  email: string;
  fullName: string;
  phone: string;
}

class AuthService {
  /**
   * Send OTP to email
   */
  async sendOTP(email: string): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.signInWithOtp({
        email,
        options: {
          shouldCreateUser: true,
        },
      });
      
      if (error) throw error;
      
      return { success: true };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }
  
  /**
   * Verify OTP
   */
  async verifyOTP(email: string, otp: string): Promise<{
    success: boolean;
    user?: User;
    error?: string;
  }> {
    try {
      // For demo purposes, accept '123456' as valid OTP
      if (otp !== '123456') {
        return { success: false, error: 'Invalid OTP' };
      }
      
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password: otp, // Use OTP as password for demo
      });
      
      if (error) throw error;
      
      if (data.user) {
        const user: User = {
          id: data.user.id,
          email: data.user.email || '',
          fullName: data.user.user_metadata?.full_name || 'User',
          phone: data.user.phone || undefined,
          avatarUrl: data.user.user_metadata?.avatar_url || undefined,
          createdAt: data.user.created_at,
          updatedAt: data.user.updated_at || data.user.created_at,
        };
        
        return { success: true, user };
      }
      
      return { success: false, error: 'User not found' };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }
  
  /**
   * Sign up new user
   */
  async signUp(data: SignUpRequest): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.signUp({
        email: data.email,
        password: 'temp-password', // Will be replaced with OTP
        options: {
          data: {
            full_name: data.fullName,
            phone: data.phone,
          },
        },
      });
      
      if (error) throw error;
      
      return { success: true };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }
  
  /**
   * Sign out
   */
  async signOut(): Promise<void> {
    await supabase.auth.signOut();
  }
  
  /**
   * Get current user
   */
  async getCurrentUser(): Promise<User | null> {
    const { data } = await supabase.auth.getUser();
    
    if (data.user) {
      return {
        id: data.user.id,
        email: data.user.email || '',
        fullName: data.user.user_metadata?.full_name || 'User',
        phone: data.user.phone || undefined,
        avatarUrl: data.user.user_metadata?.avatar_url || undefined,
        createdAt: data.user.created_at,
        updatedAt: data.user.updated_at || data.user.created_at,
      };
    }
    
    return null;
  }
  
  /**
   * Listen to auth changes
   */
  onAuthStateChange(callback: (user: User | null) => void) {
    const { data } = supabase.auth.onAuthStateChange((_, session) => {
      if (session?.user) {
        const user: User = {
          id: session.user.id,
          email: session.user.email || '',
          fullName: session.user.user_metadata?.full_name || 'User',
          phone: session.user.phone || undefined,
          avatarUrl: session.user.user_metadata?.avatar_url || undefined,
          createdAt: session.user.created_at,
          updatedAt: session.user.updated_at || session.user.created_at,
        };
        callback(user);
      } else {
        callback(null);
      }
    });
    
    return () => data.subscription.unsubscribe();
  }
}

export const authService = new AuthService();
export default authService;
