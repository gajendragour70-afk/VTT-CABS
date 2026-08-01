package com.vttcabs.admin.data.model

data class AdminUser(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "admin",
    val createdAt: Long = System.currentTimeMillis()
)

data class Driver(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val vehicleCategory: String = "",
    val vehicleModel: String = "",
    val vehicleNumber: String = "",
    val rating: Double = 0.0,
    val totalTrips: Int = 0,
    val isOnline: Boolean = false,
    val isActive: Boolean = true,
    val approvalStatus: String = "PENDING",
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val walletBalance: Double = 0.0,
    val documents: DriverDocuments = DriverDocuments(),
    val createdAt: Long = System.currentTimeMillis()
)

data class DriverDocuments(
    val aadhaarNumber: String = "",
    val aadhaarFrontUrl: String = "",
    val aadhaarBackUrl: String = "",
    val panCardNumber: String = "",
    val panCardUrl: String = "",
    val licenceNumber: String = "",
    val licenceFrontUrl: String = "",
    val licenceBackUrl: String = "",
    val rcUrl: String = "",
    val insuranceUrl: String = "",
    val pucUrl: String = "",
    val permitUrl: String = "",
    val photoUrl: String = ""
)

data class Customer(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isBlocked: Boolean = false,
    val totalBookings: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val vehicleNumber: String? = null,
    val vehicleModel: String? = null,
    val vehicleCategory: String = "",
    val bookingType: String = "",
    val pickupAddress: String = "",
    val dropAddress: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val dropLat: Double = 0.0,
    val dropLng: Double = 0.0,
    val distanceKm: Double = 0.0,
    val durationMins: Int = 0,
    val totalFare: Double = 0.0,
    val paymentMethod: String = "CASH",
    val paymentStatus: String = "PENDING",
    val bookingStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val pickupDate: String = "",
    val pickupTime: String = ""
)

data class Vehicle(
    val id: String = "",
    val category: String = "",
    val name: String = "",
    val description: String = "",
    val baseFare: Double = 0.0,
    val perKmRate: Double = 0.0,
    val perMinRate: Double = 0.0,
    val imageUrl: String = "",
    val isActive: Boolean = true
)

data class FareRule(
    val id: String = "",
    val vehicleCategory: String = "",
    val baseFare: Double = 0.0,
    val perKmRate: Double = 0.0,
    val perMinRate: Double = 0.0,
    val minimumFare: Double = 0.0,
    val surgeMultiplier: Double = 1.0
)

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class DashboardStats(
    val totalBookings: Int = 0,
    val activeTrips: Int = 0,
    val completedTrips: Int = 0,
    val cancelledTrips: Int = 0,
    val totalDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val totalCustomers: Int = 0,
    val totalRevenue: Double = 0.0
)

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

enum class BookingStatus {
    PENDING,
    SEARCHING,
    ASSIGNED,
    ACCEPTED,
    DRIVER_ARRIVING,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
