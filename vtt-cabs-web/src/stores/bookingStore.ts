import { create } from 'zustand';
import type { Booking, Location, VehicleType, FareCalculation } from '@/types';

interface BookingState {
  // Pickup & Drop
  pickupLocation: Location | null;
  dropLocation: Location | null;
  pickupAddress: string;
  dropAddress: string;
  
  // Vehicle Selection
  selectedVehicleType: VehicleType | null;
  
  // Fare
  fareCalculation: FareCalculation | null;
  estimatedFare: number;
  
  // Current Booking
  currentBooking: Booking | null;
  
  // Driver Location
  driverLocation: Location | null;
  
  // Actions
  setPickupLocation: (location: Location, address: string) => void;
  setDropLocation: (location: Location, address: string) => void;
  setVehicleType: (type: VehicleType) => void;
  setFareCalculation: (fare: FareCalculation) => void;
  setCurrentBooking: (booking: Booking | null) => void;
  setDriverLocation: (location: Location | null) => void;
  resetBooking: () => void;
}

const initialState = {
  pickupLocation: null,
  dropLocation: null,
  pickupAddress: '',
  dropAddress: '',
  selectedVehicleType: null,
  fareCalculation: null,
  estimatedFare: 0,
  currentBooking: null,
  driverLocation: null,
};

export const useBookingStore = create<BookingState>((set) => ({
  ...initialState,
  
  setPickupLocation: (location, address) =>
    set({ pickupLocation: location, pickupAddress: address }),
  
  setDropLocation: (location, address) =>
    set({ dropLocation: location, dropAddress: address }),
  
  setVehicleType: (type) => set({ selectedVehicleType: type }),
  
  setFareCalculation: (fare) =>
    set({ fareCalculation: fare, estimatedFare: fare.total }),
  
  setCurrentBooking: (booking) => set({ currentBooking: booking }),
  
  setDriverLocation: (location) => set({ driverLocation: location }),
  
  resetBooking: () => set(initialState),
}));
