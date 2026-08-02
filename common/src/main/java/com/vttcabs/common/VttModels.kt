package com.vttcabs.common

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER, DRIVER, ADMIN
}

enum class BookingType {
    ONE_WAY, ROUND_TRIP, LOCAL, AIRPORT_TRANSFER, OUTSTATION, CITY_TOUR, AIRPORT_PICKUP, AIRPORT_DROP
}

enum class VehicleCategory(
    val displayName: String,
    val capacity: Int,
    val defaultBaseFare: Double,
    val defaultPerKm: Double,
    val defaultPerMin: Double,
    val description: String
) {
    HATCHBACK("Hatchback", 4, 100.0, 10.0, 0.0, "Compact & economical 4-seater"),
    SEDAN("Sedan", 4, 100.0, 12.0, 0.0, "Comfortable sedan with AC & extra luggage"),
    ERTIGA("Ertiga", 6, 100.0, 15.0, 0.0, "Spacious 6-seater MUV for families"),
    SUV("SUV", 6, 100.0, 18.0, 0.0, "Premium 6-seater SUV with top comfort"),
    INNOVA_CRYSTA("Innova Crysta", 7, 100.0, 22.0, 0.0, "Luxury 7-seater for outstation & executive trips"),
    TEMPO_TRAVELLER("Tempo Traveller", 12, 100.0, 25.0, 0.0, "Large group 12-seater with recliner seats");

    fun getPerKmRate(bookingType: BookingType): Double {
        val isOneWay = (bookingType == BookingType.ONE_WAY)
        return when (this) {
            SEDAN -> if (isOneWay) 14.0 else 12.0
            ERTIGA -> if (isOneWay) 17.0 else 15.0
            SUV -> if (isOneWay) 20.0 else 18.0
            TEMPO_TRAVELLER -> if (isOneWay) 28.0 else 25.0
            HATCHBACK -> if (isOneWay) 12.0 else 10.0
            INNOVA_CRYSTA -> if (isOneWay) 24.0 else 22.0
        }
    }
}

enum class BookingStatus {
    PENDING,
    ASSIGNED,
    ACCEPTED,
    DRIVER_ARRIVING,
    TRIP_STARTED,
    TRIP_COMPLETED,
    CANCELLED,
    SEARCHING,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED;

    val label: String
        get() = when (this) {
            PENDING, SEARCHING -> "Pending Admin Approval"
            ASSIGNED -> "Assigned to Driver"
            ACCEPTED -> "Driver Accepted"
            DRIVER_ARRIVING, ARRIVED -> "Driver Arriving"
            TRIP_STARTED, IN_PROGRESS -> "Trip Started"
            TRIP_COMPLETED, COMPLETED -> "Trip Completed"
            CANCELLED -> "Cancelled"
        }
}

enum class PaymentMethod {
    CASH, UPI, CARD, WALLET
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

enum class DriverApprovalStatus {
    DRAFT,          // Registration started but not submitted
    SUBMITTED,      // Documents submitted, pending review
    PENDING,        // Under review by admin
    APPROVED,       // Approved and can go online
    REJECTED,       // Rejected with reason
    SUSPENDED;      // Temporarily suspended
    
    val canLogin: Boolean
        get() = this == APPROVED
        
    val canGoOnline: Boolean
        get() = this == APPROVED
        
    val canReceiveRides: Boolean
        get() = this == APPROVED && true // Also requires isOnline = true
}

enum class DocumentType {
    AADHAAR_FRONT,
    AADHAAR_BACK,
    PAN_CARD,
    LICENSE_FRONT,
    LICENSE_BACK,
    RC_DOCUMENT,
    INSURANCE,
    PUC_CERTIFICATE,
    PROFILE_PHOTO,
    VEHICLE_FRONT,
    VEHICLE_BACK,
    VEHICLE_LEFT,
    VEHICLE_RIGHT,
    VEHICLE_INTERIOR,
    SELFIE,
    OTHER
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val profilePicUrl: String = "",
    val rating: Double = 4.9,
    val totalRides: Int = 0,
    val password: String = "123456",
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val otpCode: String = "",
    val otpExpiresAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "driver_registrations")
data class DriverRegistrationEntity(
    @PrimaryKey val id: String,
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String,
    val alternatePhone: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyContactRelation: String = "",
    
    // Document Numbers
    val aadhaarNumber: String = "",
    val panCardNumber: String = "",
    val drivingLicenseNumber: String = "",
    val licenseExpiryDate: String = "",
    
    // Document URLs
    val aadhaarFrontUrl: String = "",
    val aadhaarBackUrl: String = "",
    val panCardUrl: String = "",
    val licenseFrontUrl: String = "",
    val licenseBackUrl: String = "",
    val rcDocUrl: String = "",
    val insuranceUrl: String = "",
    val pucUrl: String = "",
    val profilePhotoUrl: String = "",
    val selfieUrl: String = "",
    val vehicleFrontPhotoUrl: String = "",
    val vehicleBackPhotoUrl: String = "",
    val vehicleLeftPhotoUrl: String = "",
    val vehicleRightPhotoUrl: String = "",
    
    // Vehicle Info
    val vehicleCategory: String = "",
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val vehicleYear: String = "",
    val vehicleNumber: String = "",
    val vehicleColor: String = "",
    val seatingCapacity: Int = 4,
    
    // Bank Details
    val bankName: String = "",
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val upiId: String = "",
    
    // Status
    val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.DRAFT,
    val rejectionReason: String = "",
    val reuploadRequestedDocs: String = "",
    val submittedAt: Long = 0L,
    val reviewedAt: Long = 0L,
    val reviewedBy: String = "",
    val reviewedNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Progress tracking
    val personalInfoComplete: Boolean = false,
    val documentsComplete: Boolean = false,
    val vehicleInfoComplete: Boolean = false,
    val bankDetailsComplete: Boolean = false,
    val isVerified: Boolean = false
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val vehicleCategory: VehicleCategory,
    val vehicleBrand: String = "Honda",
    val vehicleModel: String,
    val vehicleYear: String = "2022",
    val vehicleNumber: String,
    val vehicleColor: String = "Silver",
    val seatingCapacity: Int = 4,
    val rating: Double = 4.8,
    val totalTrips: Int = 0,
    val isOnline: Boolean = true,
    val currentLat: Double = 12.9716,
    val currentLng: Double = 77.5946,
    val totalEarnings: Double = 0.0,
    val walletBalance: Double = 1250.0,
    val isApproved: Boolean = true,
    val address: String = "Bangalore Central",
    val dob: String = "1992-05-15",
    val emergencyContact: String = "+91 9876543211",
    val aadhaarNumber: String = "9876-5432-1098",
    val aadhaarFrontUrl: String = "aadhaar_front.jpg",
    val aadhaarBackUrl: String = "aadhaar_back.jpg",
    val panCardNumber: String = "ABCDE1234F",
    val panCardUrl: String = "pan_card.jpg",
    val licenceNumber: String = "DL-042023009182",
    val licenceFrontUrl: String = "dl_front.jpg",
    val licenceBackUrl: String = "dl_back.jpg",
    val rcDocUrl: String = "rc_document.pdf",
    val insuranceUrl: String = "vehicle_insurance.pdf",
    val pucUrl: String = "puc_certificate.pdf",
    val permitUrl: String = "vehicle_permit.pdf",
    val profilePhotoUrl: String = "profile_photo.jpg",
    val vehicleFrontPhotoUrl: String = "veh_front.jpg",
    val vehicleBackPhotoUrl: String = "veh_back.jpg",
    val vehicleLeftSidePhotoUrl: String = "veh_left.jpg",
    val vehicleRightSidePhotoUrl: String = "veh_right.jpg",
    val vehicleInteriorPhotoUrl: String = "veh_interior.jpg",
    val selfieUrl: String = "",
    val vehicleConditionPhotosUrl: String = "",
    val bankName: String = "State Bank of India",
    val accountNumber: String = "30987123456",
    val ifscCode: String = "SBIN0001234",
    val upiId: String = "",
    val reuploadRequestedDocs: String = "",
    val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.APPROVED,
    val rejectionReason: String = "",
    val password: String = "123456",
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val otpCode: String = "",
    val otpExpiresAt: Long = 0L
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val vehicleNumber: String? = null,
    val vehicleModel: String? = null,
    val vehicleCategory: VehicleCategory,
    val bookingType: BookingType,
    val pickupAddress: String,
    val dropAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropLat: Double,
    val dropLng: Double,
    val distanceKm: Double,
    val durationMins: Int,
    val totalFare: Double,
    val baseFare: Double,
    val distanceFare: Double,
    val driverEarning: Double = 0.0,
    val companyShare: Double = 0.0,
    val timeFare: Double = 0.0,
    val nightCharge: Double = 0.0,
    val tollCharge: Double = 0.0,
    val parkingCharge: Double = 0.0,
    val surgeMultiplier: Double = 1.0,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val bookingStatus: BookingStatus = BookingStatus.PENDING,
    val otp: String = "4821",
    val tripOtp: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val assignedAt: Long = 0L,
    val acceptedAt: Long = 0L,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val cancelledReason: String? = null,
    val cancelledBy: String? = null,
    val driverLat: Double = pickupLat,
    val driverLng: Double = pickupLng,
    val pickupDate: String = "",
    val pickupTime: String = "",
    val specialRequests: String = ""
)

@Entity(tableName = "driver_earnings")
data class DriverEarningEntity(
    @PrimaryKey val id: String,
    val driverId: String,
    val bookingId: String,
    val grossAmount: Double,
    val platformFee: Double,
    val gstAmount: Double,
    val netEarning: Double,
    val tripDate: Long = System.currentTimeMillis(),
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paidAt: Long = 0L,
    val transactionId: String = ""
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val fromUserId: String,
    val toUserId: String,
    val fromUserType: UserRole,
    val toUserType: UserRole,
    val rating: Int, // 1-5 stars
    val review: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sos_alerts")
data class SosAlertEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val userId: String,
    val userType: UserRole,
    val latitude: Double,
    val longitude: Double,
    val triggeredAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE", // ACTIVE, ACKNOWLEDGED, RESOLVED
    val acknowledgedBy: String = "",
    val resolvedAt: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userType: UserRole = UserRole.CUSTOMER,
    val title: String,
    val message: String,
    val type: String = "GENERAL", // BOOKING, PAYMENT, DRIVER, SYSTEM, SOS
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionUrl: String = ""
)

@Entity(tableName = "fare_rules")
data class FareRuleEntity(
    @PrimaryKey val vehicleCategory: VehicleCategory,
    val baseFare: Double,
    val perKmRate: Double,
    val perMinRate: Double,
    val nightSurgeMultiplier: Double = 1.25,
    val waitingRatePerMin: Double = 2.0
)

data class PopularPlace(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val iconName: String
)

data class FareBreakdown(
    val baseFare: Double,
    val distanceFare: Double,
    val perKmRate: Double = 0.0,
    val perKmRateText: String = "",
    val rateBreakdownNote: String = "",
    val distanceKm: Double = 0.0,
    val durationMins: Int = 0,
    val minimumFareApplied: Boolean = false,
    val timeFare: Double = 0.0,
    val nightCharge: Double = 0.0,
    val driverAllowance: Double = 0.0,
    val tollCharge: Double = 0.0,
    val parkingCharge: Double = 0.0,
    val surgeMultiplier: Double = 1.0,
    val subtotal: Double,
    val gstAmount: Double = 0.0,
    val grandTotal: Double,
    val disclaimer: String = "Fare excludes Toll, Parking & State Tax. These will be paid separately."
)

// Admin Report Data Classes
data class AdminDashboardStats(
    val totalDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val pendingDriverApplications: Int = 0,
    val rejectedDrivers: Int = 0,
    val suspendedDrivers: Int = 0,
    val totalCustomers: Int = 0,
    val totalBookings: Int = 0,
    val completedTrips: Int = 0,
    val cancelledTrips: Int = 0,
    val totalRevenue: Double = 0.0,
    val todayRevenue: Double = 0.0,
    val weekRevenue: Double = 0.0,
    val monthRevenue: Double = 0.0,
    val averageRating: Double = 0.0,
    val sosAlerts: Int = 0
)

data class DriverEarningSummary(
    val driverId: String,
    val driverName: String,
    val totalTrips: Int,
    val grossEarnings: Double,
    val platformFees: Double,
    val netEarnings: Double,
    val pendingPayout: Double,
    val thisWeekEarnings: Double,
    val thisMonthEarnings: Double
)
