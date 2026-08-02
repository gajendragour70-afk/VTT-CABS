// ============================================================================
// VTT CABS - Shared Types
// ============================================================================

// User Types
export interface User {
  id: string;
  email: string;
  phone?: string;
  fullName: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Customer extends User {
  type: 'customer';
  totalRides: number;
  averageRating: number;
}

export interface Driver extends User {
  type: 'driver';
  approvalStatus: DriverApprovalStatus;
  vehicleId?: string;
  currentLatitude?: number;
  currentLongitude?: number;
  isOnline: boolean;
  isOnTrip: boolean;
  totalRides: number;
  averageRating: number;
  totalEarnings: number;
  documents?: DriverDocuments;
}

export interface Admin extends User {
  type: 'admin';
  role: AdminRole;
  permissions: string[];
}

// Driver Status
export type DriverApprovalStatus = 
  | 'DRAFT' 
  | 'SUBMITTED' 
  | 'PENDING' 
  | 'APPROVED' 
  | 'REJECTED' 
  | 'SUSPENDED';

// Admin Roles
export type AdminRole = 'SUPER_ADMIN' | 'CITY_ADMIN' | 'SUPPORT_ADMIN' | 'FINANCE_ADMIN';

// Driver Documents
export interface DriverDocuments {
  aadhaarFront?: string;
  aadhaarBack?: string;
  panCard?: string;
  drivingLicenseFront?: string;
  drivingLicenseBack?: string;
  rcBook?: string;
  vehicleInsurance?: string;
  vehiclePuc?: string;
  profilePhoto?: string;
  selfiePhoto?: string;
  verificationStatus: DocumentVerificationStatus;
}

export type DocumentVerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

// Vehicle Types
export interface Vehicle {
  id: string;
  ownerId: string;
  ownerType: 'driver' | 'fleet_owner';
  vehicleNumber: string;
  vehicleType: VehicleType;
  vehicleModel: string;
  vehicleColor: string;
  rcNumber: string;
  rcExpiry: string;
  insurancePolicy: string;
  insuranceExpiry: string;
  pucNumber: string;
  pucExpiry: string;
  isActive: boolean;
  isVerified: boolean;
}

export type VehicleType = 'HATCHBACK' | 'SEDAN' | 'SUV' | 'AUTO';

// Booking Types
export interface Booking {
  id: string;
  customerId: string;
  driverId?: string;
  vehicleId?: string;
  status: BookingStatus;
  pickupAddress: string;
  pickupLatitude: number;
  pickupLongitude: number;
  dropAddress: string;
  dropLatitude: number;
  dropLongitude: number;
  vehicleType: VehicleType;
  otp: string;
  tripOtp?: string;
  estimatedFare: number;
  finalFare?: number;
  distance?: number;
  duration?: number;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  promoCode?: string;
  discount?: number;
  bookingType: BookingType;
  scheduledTime?: string;
  createdAt: string;
  updatedAt: string;
}

export type BookingStatus = 
  | 'SEARCHING_DRIVER'
  | 'DRIVER_ASSIGNED'
  | 'DRIVER_EN_ROUTE'
  | 'DRIVER_ARRIVED'
  | 'TRIP_STARTED'
  | 'TRIP_COMPLETED'
  | 'CANCELLED';

export type BookingType = 'INSTANT' | 'SCHEDULED';

export type PaymentMethod = 'CASH' | 'ONLINE' | 'WALLET';

export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

// Rating
export interface Rating {
  id: string;
  bookingId: string;
  fromUserId: string;
  toUserId: string;
  rating: number;
  feedback?: string;
  createdAt: string;
}

// SOS Alert
export interface SOSAlert {
  id: string;
  bookingId?: string;
  customerId: string;
  driverId?: string;
  latitude: number;
  longitude: number;
  status: SOSStatus;
  triggeredAt: string;
  resolvedAt?: string;
}

export type SOSStatus = 'TRIGGERED' | 'DISPATCHED' | 'RESPONDED' | 'RESOLVED' | 'CANCELLED';

// Chat Message
export interface ChatMessage {
  id: string;
  bookingId: string;
  senderId: string;
  senderType: 'customer' | 'driver';
  message: string;
  type: 'text' | 'image' | 'location';
  readAt?: string;
  createdAt: string;
}

// Wallet Types
export interface Wallet {
  id: string;
  userId: string;
  userType: 'driver' | 'customer' | 'fleet_owner';
  balance: number;
  pendingAmount: number;
  totalEarnings: number;
  totalSpent: number;
}

export interface WalletTransaction {
  id: string;
  walletId: string;
  type: TransactionType;
  amount: number;
  description: string;
  balanceAfter: number;
  createdAt: string;
}

export type TransactionType = 
  | 'TRIP_EARNING'
  | 'TRIP_PAYMENT'
  | 'COMMISSION_DEDUCTED'
  | 'BONUS'
  | 'INCENTIVE'
  | 'PENALTY'
  | 'PAYOUT'
  | 'REFUND'
  | 'CASHBACK';

// Location Types
export interface Location {
  latitude: number;
  longitude: number;
  accuracy?: number;
  speed?: number;
  bearing?: number;
  timestamp: number;
}

export interface DriverLocation {
  driverId: string;
  latitude: number;
  longitude: number;
  heading?: number;
  speed?: number;
  isOnline: boolean;
  isOnTrip: boolean;
  updatedAt: string;
}

// Fare Types
export interface FareCalculation {
  baseFare: number;
  distanceFare: number;
  timeFare: number;
  surgeMultiplier: number;
  discount: number;
  subtotal: number;
  gst: number;
  total: number;
}

export interface PricingConfig {
  cityId: string;
  vehicleType: VehicleType;
  baseFare: number;
  perKmRate: number;
  perMinuteRate: number;
  minimumFare: number;
  surgeMultiplier: number;
}

// City Types
export interface City {
  id: string;
  name: string;
  state: string;
  country: string;
  isActive: boolean;
  baseFare: number;
  perKmRate: number;
  minimumFare: number;
  driverSearchRadius: number;
}

// Notification Types
export interface Notification {
  id: string;
  userId: string;
  title: string;
  body: string;
  data?: Record<string, string>;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
}

export type NotificationType = 
  | 'BOOKING_REQUEST'
  | 'DRIVER_ASSIGNED'
  | 'DRIVER_EN_ROUTE'
  | 'DRIVER_ARRIVED'
  | 'TRIP_STARTED'
  | 'TRIP_COMPLETED'
  | 'PAYMENT_RECEIVED'
  | 'RATING_RECEIVED'
  | 'SOS_ALERT'
  | 'DRIVER_APPROVED'
  | 'DRIVER_REJECTED'
  | 'DOCUMENT_EXPIRY'
  | 'PROMO_OFFER';

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiError;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
}

// Pagination
export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  hasMore: boolean;
}

// OTP Types
export interface OTPRequest {
  email: string;
  type: 'login' | 'signup' | 'password_reset';
}

export interface OTPVerify {
  email: string;
  otp: string;
}

// Auth Types
export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  session: Session | null;
  isLoading: boolean;
}

export interface Session {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

// Fleet Owner Types
export interface FleetOwner extends User {
  type: 'fleet_owner';
  companyName?: string;
  gstin?: string;
  address?: string;
  totalDrivers: number;
  totalVehicles: number;
  totalEarnings: number;
}

// Emergency Contact
export interface EmergencyContact {
  id: string;
  customerId: string;
  name: string;
  phone: string;
  email?: string;
  relationship: string;
  isPrimary: boolean;
}

// Referral
export interface Referral {
  id: string;
  referrerId: string;
  referredUserId: string;
  referralCode: string;
  status: 'pending' | 'active' | 'rewarded';
  rewardAmount: number;
  createdAt: string;
}

// Coupon/Promo
export interface Coupon {
  id: string;
  code: string;
  description: string;
  discountType: 'PERCENTAGE' | 'FIXED' | 'FREE_RIDE';
  discountValue: number;
  maxDiscount?: number;
  minOrderValue?: number;
  usageLimit: number;
  usedCount: number;
  validFrom: string;
  validTo: string;
  isActive: boolean;
}
