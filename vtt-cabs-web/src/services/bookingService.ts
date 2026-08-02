import { supabase } from './supabase';
import type { Booking, VehicleType, Location, DriverLocation } from '@/types';
import { useBookingStore } from '@/stores/bookingStore';

export interface CreateBookingRequest {
  pickupAddress: string;
  pickupLatitude: number;
  pickupLongitude: number;
  dropAddress: string;
  dropLatitude: number;
  dropLongitude: number;
  vehicleType: VehicleType;
  estimatedFare: number;
  paymentMethod: 'CASH' | 'ONLINE' | 'WALLET';
  promoCode?: string;
}

export interface RateDriverRequest {
  bookingId: string;
  rating: number;
  feedback?: string;
}

class BookingService {
  /**
   * Create a new booking
   */
  async createBooking(data: CreateBookingRequest): Promise<{
    success: boolean;
    booking?: Booking;
    error?: string;
  }> {
    try {
      // Generate OTP
      const otp = Math.random().toString().slice(2, 6);
      
      const bookingData = {
        customer_id: (await supabase.auth.getUser()).data.user?.id,
        pickup_address: data.pickupAddress,
        pickup_latitude: data.pickupLatitude,
        pickup_longitude: data.pickupLongitude,
        drop_address: data.dropAddress,
        drop_latitude: data.dropLatitude,
        drop_longitude: data.dropLongitude,
        vehicle_type: data.vehicleType,
        estimated_fare: data.estimatedFare,
        payment_method: data.paymentMethod,
        promo_code: data.promoCode,
        otp,
        status: 'SEARCHING_DRIVER',
      };
      
      const { data: booking, error } = await supabase
        .from('bookings')
        .insert(bookingData)
        .select()
        .single();
      
      if (error) throw error;
      
      return {
        success: true,
        booking: this.mapBooking(booking),
      };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }
  
  /**
   * Get booking by ID
   */
  async getBooking(bookingId: string): Promise<Booking | null> {
    try {
      const { data, error } = await supabase
        .from('bookings')
        .select('*, driver:drivers(*), vehicle:vehicles(*)')
        .eq('id', bookingId)
        .single();
      
      if (error) throw error;
      
      return this.mapBooking(data);
    } catch (error) {
      console.error('Error fetching booking:', error);
      return null;
    }
  }
  
  /**
   * Get user's booking history
   */
  async getBookingHistory(
    page: number = 1,
    limit: number = 20
  ): Promise<Booking[]> {
    try {
      const userId = (await supabase.auth.getUser()).data.user?.id;
      
      const { data, error } = await supabase
        .from('bookings')
        .select('*, driver:drivers(*)')
        .eq('customer_id', userId)
        .order('created_at', { ascending: false })
        .range((page - 1) * limit, page * limit - 1);
      
      if (error) throw error;
      
      return data.map(this.mapBooking);
    } catch (error) {
      console.error('Error fetching history:', error);
      return [];
    }
  }
  
  /**
   * Cancel booking
   */
  async cancelBooking(bookingId: string): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase
        .from('bookings')
        .update({ status: 'CANCELLED', updated_at: new Date().toISOString() })
        .eq('id', bookingId);
      
      if (error) throw error;
      
      return { success: true };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }
  
  /**
   * Rate driver
   */
  async rateDriver(data: RateDriverRequest): Promise<{ success: boolean; error?: string }> {
    try {
      const userId = (await supabase.auth.getUser()).data.user?.id;
      
      // Get booking to find driver ID
      const booking = await this.getBooking(data.bookingId);
      if (!booking?.driverId) {
        return { success: false, error: 'Driver not found' };
      }
      
      // Insert rating
      const { error } = await supabase.from('ratings').insert({
        booking_id: data.bookingId,
        from_user_id: userId,
        to_user_id: booking.driverId,
        rating: data.rating,
        feedback: data.feedback,
      });
      
      if (error) throw error;
      
      return { success: true };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }
  
  /**
   * Subscribe to booking updates
   */
  subscribeToBookingUpdates(
    bookingId: string,
    callback: (booking: Booking) => void
  ) {
    const channel = supabase
      .channel(`booking:${bookingId}`)
      .on(
        'postgres_changes',
        {
          event: 'UPDATE',
          schema: 'public',
          table: 'bookings',
          filter: `id=eq.${bookingId}`,
        },
        (payload) => {
          callback(this.mapBooking(payload.new));
        }
      )
      .subscribe();
    
    return () => {
      supabase.removeChannel(channel);
    };
  }
  
  /**
   * Subscribe to driver location updates
   */
  subscribeToDriverLocation(
    driverId: string,
    callback: (location: Location) => void
  ) {
    const channel = supabase
      .channel(`driver-location:${driverId}`)
      .on(
        'postgres_changes',
        {
          event: 'UPDATE',
          schema: 'public',
          table: 'driver_locations',
          filter: `driver_id=eq.${driverId}`,
        },
        (payload) => {
          const loc = payload.new as DriverLocation;
          callback({
            latitude: loc.latitude,
            longitude: loc.longitude,
            timestamp: new Date(loc.updated_at).getTime(),
          });
        }
      )
      .subscribe();
    
    return () => {
      supabase.removeChannel(channel);
    };
  }
  
  /**
   * Map database booking to interface
   */
  private mapBooking(data: any): Booking {
    return {
      id: data.id,
      customerId: data.customer_id,
      driverId: data.driver_id,
      vehicleId: data.vehicle_id,
      status: data.status,
      pickupAddress: data.pickup_address,
      pickupLatitude: data.pickup_latitude,
      pickupLongitude: data.pickup_longitude,
      dropAddress: data.drop_address,
      dropLatitude: data.drop_latitude,
      dropLongitude: data.drop_longitude,
      vehicleType: data.vehicle_type,
      otp: data.otp,
      tripOtp: data.trip_otp,
      estimatedFare: data.estimated_fare,
      finalFare: data.final_fare,
      distance: data.distance,
      duration: data.duration,
      paymentMethod: data.payment_method,
      paymentStatus: data.payment_status,
      promoCode: data.promo_code,
      discount: data.discount,
      bookingType: data.booking_type,
      scheduledTime: data.scheduled_time,
      createdAt: data.created_at,
      updatedAt: data.updated_at,
    };
  }
}

export const bookingService = new BookingService();
export default bookingService;
