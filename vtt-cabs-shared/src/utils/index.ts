// ============================================================================
// VTT CABS - Shared Utilities
// ============================================================================

import type { Location, FareCalculation, VehicleType, BookingStatus } from '../types';
import { PRICING, COMMISSION } from '../constants';

/**
 * Calculate distance between two coordinates using Haversine formula
 */
export function calculateDistance(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  const R = 6371; // Earth's radius in km
  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Calculate estimated fare for a trip
 */
export function calculateFare(
  distanceKm: number,
  durationMinutes: number,
  vehicleType: VehicleType,
  surgeMultiplier: number = 1,
  discount: number = 0
): FareCalculation {
  const pricing = PRICING[vehicleType];
  
  const baseFare = pricing.baseFare;
  const distanceFare = distanceKm * pricing.perKm;
  const timeFare = durationMinutes * pricing.perMinute;
  
  const subtotal = (baseFare + distanceFare + timeFare) * surgeMultiplier;
  const afterDiscount = Math.max(0, subtotal - discount);
  const gst = afterDiscount * (COMMISSION.GST_PERCENT / 100);
  const total = afterDiscount + gst;
  
  return {
    baseFare,
    distanceFare,
    timeFare,
    surgeMultiplier,
    discount,
    subtotal,
    gst,
    total: Math.max(total, pricing.minimumFare),
  };
}

/**
 * Format currency
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
}

/**
 * Format distance
 */
export function formatDistance(km: number): string {
  if (km < 1) {
    return `${Math.round(km * 1000)} m`;
  }
  return `${km.toFixed(1)} km`;
}

/**
 * Format duration
 */
export function formatDuration(minutes: number): string {
  if (minutes < 1) {
    return '< 1 min';
  }
  if (minutes < 60) {
    return `${Math.round(minutes)} min`;
  }
  const hours = Math.floor(minutes / 60);
  const mins = Math.round(minutes % 60);
  return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
}

/**
 * Format date
 */
export function formatDate(date: string | Date, format: string = 'DD MMM YYYY'): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const day = String(d.getDate()).padStart(2, '0');
  const month = d.toLocaleString('en-US', { month: 'short' });
  const year = d.getFullYear();
  const hours = String(d.getHours()).padStart(2, '0');
  const mins = String(d.getMinutes()).padStart(2, '0');
  
  return format
    .replace('DD', day)
    .replace('MMM', month)
    .replace('YYYY', String(year))
    .replace('HH', hours)
    .replace('mm', mins);
}

/**
 * Format phone number
 */
export function formatPhone(phone: string): string {
  if (phone.length === 10) {
    return `+91 ${phone.slice(0, 5)} ${phone.slice(5)}`;
  }
  return phone;
}

/**
 * Generate OTP
 */
export function generateOTP(length: number = 6): string {
  return Math.random()
    .toString()
    .slice(2, 2 + length)
    .padStart(length, '0');
}

/**
 * Validate email
 */
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate phone (Indian)
 */
export function isValidPhone(phone: string): boolean {
  const phoneRegex = /^[6-9]\d{9}$/;
  return phoneRegex.test(phone);
}

/**
 * Validate vehicle number
 */
export function isValidVehicleNumber(number: string): boolean {
  const vehicleRegex = /^[A-Z]{2}[0-9]{1,2}[A-Z]{1,2}[0-9]{4}$/;
  return vehicleRegex.test(number.toUpperCase());
}

/**
 * Get booking status display info
 */
export function getBookingStatusInfo(status: BookingStatus): { label: string; color: string; icon: string } {
  const statusMap: Record<BookingStatus, { label: string; color: string; icon: string }> = {
    SEARCHING_DRIVER: { label: 'Searching for Driver', color: 'yellow', icon: '🔍' },
    DRIVER_ASSIGNED: { label: 'Driver Assigned', color: 'blue', icon: '✓' },
    DRIVER_EN_ROUTE: { label: 'Driver En Route', color: 'blue', icon: '🚗' },
    DRIVER_ARRIVED: { label: 'Driver Arrived', color: 'green', icon: '📍' },
    TRIP_STARTED: { label: 'Trip In Progress', color: 'green', icon: '🚕' },
    TRIP_COMPLETED: { label: 'Trip Completed', color: 'gray', icon: '✅' },
    CANCELLED: { label: 'Cancelled', color: 'red', icon: '❌' },
  };
  return statusMap[status];
}

/**
 * Get vehicle type display info
 */
export function getVehicleTypeInfo(type: VehicleType): { label: string; icon: string; seats: number } {
  const typeMap: Record<VehicleType, { label: string; icon: string; seats: number }> = {
    HATCHBACK: { label: 'Hatchback', icon: '🚗', seats: 4 },
    SEDAN: { label: 'Sedan', icon: '🚙', seats: 4 },
    SUV: { label: 'SUV', icon: '🚘', seats: 6 },
    AUTO: { label: 'Auto', icon: '🛺', seats: 3 },
  };
  return typeMap[type];
}

/**
 * Debounce function
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null;
  return (...args: Parameters<T>) => {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}

/**
 * Throttle function
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle = false;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
}

/**
 * Generate unique ID
 */
export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * Clamp number between min and max
 */
export function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

/**
 * Sleep/delay
 */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Get initials from name
 */
export function getInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

/**
 * Truncate text
 */
export function truncate(text: string, length: number): string {
  if (text.length <= length) return text;
  return text.slice(0, length) + '...';
}

/**
 * Parse address components
 */
export function parseAddress(address: string): {
  street: string;
  city: string;
  state: string;
  country: string;
  pincode: string;
} {
  const parts = address.split(',').map((p) => p.trim());
  return {
    street: parts[0] || '',
    city: parts[1] || '',
    state: parts[2] || '',
    country: parts[3] || 'India',
    pincode: parts[4] || '',
  };
}

/**
 * Calculate bearing between two points
 */
export function calculateBearing(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  const dLon = toRadians(lon2 - lon1);
  const y = Math.sin(dLon) * Math.cos(toRadians(lat2));
  const x =
    Math.cos(toRadians(lat1)) * Math.sin(toRadians(lat2)) -
    Math.sin(toRadians(lat1)) * Math.cos(toRadians(lat2)) * Math.cos(dLon);
  const bearing = toRadians(Math.atan2(y, x));
  return ((bearing * 180) / Math.PI + 360) % 360;
}
