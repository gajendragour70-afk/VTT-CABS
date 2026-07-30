package com.example.data.repository

import com.example.data.local.VttDao
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.BookingType
import com.example.data.model.DriverEntity
import com.example.data.model.FareRuleEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class VttRepository(private val dao: VttDao) {

    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookings()
    val allDrivers: Flow<List<DriverEntity>> = dao.getAllDrivers()
    val onlineDrivers: Flow<List<DriverEntity>> = dao.getOnlineDrivers()

    fun getCustomerBookings(customerId: String): Flow<List<BookingEntity>> =
        dao.getCustomerBookings(customerId)

    fun getDriverBookings(driverId: String): Flow<List<BookingEntity>> =
        dao.getDriverBookings(driverId)

    fun observeBookingById(id: String): Flow<BookingEntity?> =
        dao.observeBookingById(id)

    fun getUserNotifications(userId: String): Flow<List<NotificationEntity>> =
        dao.getUserNotifications(userId)

    suspend fun getBookingById(id: String): BookingEntity? =
        dao.getBookingById(id)

    suspend fun getUserByEmail(email: String): UserEntity? =
        dao.getUserByEmail(email)

    suspend fun getDriverByEmail(email: String): DriverEntity? =
        dao.getDriverByEmail(email)

    suspend fun getDriverByPhone(phone: String): DriverEntity? =
        dao.getDriverByPhone(phone)

    suspend fun insertDriver(driver: DriverEntity) {
        dao.insertDriver(driver)
        dao.insertUser(
            UserEntity(
                id = driver.id,
                name = driver.name,
                email = driver.email,
                phone = driver.phone,
                role = UserRole.DRIVER,
                password = driver.password
            )
        )
    }

    suspend fun updateDriverApprovalStatus(driverId: String, status: com.example.data.model.DriverApprovalStatus, reason: String = "") {
        val isApproved = (status == com.example.data.model.DriverApprovalStatus.APPROVED)
        dao.updateDriverApprovalStatus(driverId, status, isApproved, reason)

        val driver = dao.getDriverById(driverId)
        if (driver != null) {
            val title = when (status) {
                com.example.data.model.DriverApprovalStatus.APPROVED -> "Account Approved! 🎉"
                com.example.data.model.DriverApprovalStatus.REJECTED -> "Application Update"
                com.example.data.model.DriverApprovalStatus.SUSPENDED -> "Account Status Changed"
                else -> "Account Status Notice"
            }
            val msg = when (status) {
                com.example.data.model.DriverApprovalStatus.APPROVED -> "Congratulations ${driver.name}! Your driver profile has been approved by VTT Admin. You can now log in and accept rides."
                com.example.data.model.DriverApprovalStatus.REJECTED -> "Your driver application was rejected. Reason: ${reason.ifBlank { "Documents verification failed." }}"
                com.example.data.model.DriverApprovalStatus.SUSPENDED -> "Your driver account has been suspended by Admin. Contact support."
                else -> "Your application is currently pending verification."
            }
            dao.insertNotification(
                NotificationEntity(
                    userId = driver.id,
                    title = title,
                    message = msg
                )
            )
        }
    }

    suspend fun requestReuploadDocs(driverId: String, docs: String) {
        dao.updateReuploadRequestedDocs(driverId, docs)
        dao.insertNotification(
            NotificationEntity(
                userId = driverId,
                title = "Document Re-upload Required 📄",
                message = "VTT Admin requested updated documents: $docs. Please update your registration documents."
            )
        )
    }

    suspend fun createBooking(booking: BookingEntity) {
        dao.insertBooking(booking)
        // Create notification
        dao.insertNotification(
            NotificationEntity(
                userId = booking.customerId,
                title = "Ride Requested",
                message = "Searching for nearby ${booking.vehicleCategory.displayName} driver..."
            )
        )
    }

    suspend fun updateBooking(booking: BookingEntity) {
        dao.updateBooking(booking)
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        dao.updateBookingStatus(bookingId, status)
        val booking = dao.getBookingById(bookingId)
        if (booking != null) {
            val title = when (status) {
                BookingStatus.PENDING, BookingStatus.SEARCHING -> "Booking Submitted"
                BookingStatus.ASSIGNED -> "Driver Assigned"
                BookingStatus.ACCEPTED -> "Driver Accepted Ride!"
                BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED -> "Driver Arriving!"
                BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> "Ride Started"
                BookingStatus.TRIP_COMPLETED, BookingStatus.COMPLETED -> "Ride Completed"
                BookingStatus.CANCELLED -> "Ride Cancelled"
            }
            val msg = when (status) {
                BookingStatus.PENDING, BookingStatus.SEARCHING -> "Pending Admin approval and driver dispatch."
                BookingStatus.ASSIGNED -> "Driver ${booking.driverName} (${booking.vehicleModel}) assigned by Admin."
                BookingStatus.ACCEPTED -> "Driver ${booking.driverName} accepted your trip! Model: ${booking.vehicleModel}, Vehicle: ${booking.vehicleNumber}."
                BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED -> "Your driver ${booking.driverName} is arriving at pickup location."
                BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> "Enjoy your trip to ${booking.dropAddress}."
                BookingStatus.TRIP_COMPLETED, BookingStatus.COMPLETED -> "Thank you for riding with VTT CABS! Total: ₹${booking.totalFare}"
                BookingStatus.CANCELLED -> "Your ride booking #${booking.id.take(6)} was cancelled."
            }
            dao.insertNotification(
                NotificationEntity(
                    userId = booking.customerId,
                    title = title,
                    message = msg
                )
            )
        }
    }

    suspend fun assignDriver(
        bookingId: String,
        driver: DriverEntity
    ) {
        val assignedTime = System.currentTimeMillis()
        dao.assignDriverToBooking(
            bookingId = bookingId,
            driverId = driver.id,
            driverName = driver.name,
            driverPhone = driver.phone,
            vehicleNumber = driver.vehicleNumber,
            vehicleModel = driver.vehicleModel,
            assignedAt = assignedTime
        )
        dao.insertNotification(
            NotificationEntity(
                userId = driver.id,
                title = "New Booking Request!",
                message = "Customer: ${dao.getBookingById(bookingId)?.customerName ?: "Rider"} • Pickup: ${dao.getBookingById(bookingId)?.pickupAddress}"
            )
        )
    }

    suspend fun acceptBooking(bookingId: String, driverId: String) {
        updateBookingStatus(bookingId, BookingStatus.ACCEPTED)
        val booking = dao.getBookingById(bookingId)
        if (booking != null) {
            dao.insertNotification(
                NotificationEntity(
                    userId = booking.customerId,
                    title = "Driver On The Way! 🎉",
                    message = "Captain ${booking.driverName} (${booking.vehicleModel} - ${booking.vehicleNumber}) accepted your booking. Phone: ${booking.driverPhone}"
                )
            )
        }
    }

    suspend fun rejectBooking(bookingId: String, driverId: String, reason: String = "Driver busy or rejected") {
        val booking = dao.getBookingById(bookingId)
        dao.unassignDriverFromBooking(bookingId)
        if (booking != null) {
            dao.insertNotification(
                NotificationEntity(
                    userId = "admin",
                    title = "Booking #${bookingId.take(6)} Driver Rejected",
                    message = "Driver ${booking.driverName ?: driverId} rejected assignment: $reason. Booking returned to Pending."
                )
            )
        }
    }

    suspend fun setDriverOnline(driverId: String, isOnline: Boolean) {
        dao.updateDriverOnlineStatus(driverId, isOnline)
    }

    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        dao.updateDriverLocation(driverId, lat, lng)
    }

    suspend fun completeTrip(bookingId: String, driverId: String, fare: Double) {
        val booking = dao.getBookingById(bookingId) ?: return
        val updated = booking.copy(
            bookingStatus = BookingStatus.COMPLETED,
            paymentStatus = PaymentStatus.COMPLETED
        )
        dao.updateBooking(updated)
        dao.addDriverEarnings(driverId, fare)
        dao.insertNotification(
            NotificationEntity(
                userId = booking.customerId,
                title = "Ride Completed",
                message = "Receipt of ₹$fare paid via ${booking.paymentMethod.name}."
            )
        )
    }

    suspend fun cancelBooking(bookingId: String, reason: String) {
        val booking = dao.getBookingById(bookingId) ?: return
        val updated = booking.copy(
            bookingStatus = BookingStatus.CANCELLED,
            cancelledReason = reason
        )
        dao.updateBooking(updated)
        dao.insertNotification(
            NotificationEntity(
                userId = booking.customerId,
                title = "Booking Cancelled",
                message = "Reason: $reason"
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingUsers = dao.getAllUsers().firstOrNull()
        if (existingUsers.isNullOrEmpty()) {
            // Default Customer
            val custUser = UserEntity(
                id = "cust_101",
                name = "Guest",
                email = "user@vtt.com",
                phone = "+91 9876543210",
                role = UserRole.CUSTOMER
            )
            dao.insertUser(custUser)

            // Default Admin
            val adminUser = UserEntity(
                id = "admin_001",
                name = "VTT Dispatch Admin",
                email = "admin@vtt.com",
                phone = "+91 1800123456",
                role = UserRole.ADMIN
            )
            dao.insertUser(adminUser)

            // Default Drivers
            val drivers = listOf(
                DriverEntity(
                    id = "drv_01",
                    name = "Rajesh Sharma",
                    phone = "+91 9811223344",
                    email = "driver1@vtt.com",
                    vehicleCategory = VehicleCategory.SEDAN,
                    vehicleModel = "Honda City (White)",
                    vehicleNumber = "KA-01-MJ-4821",
                    rating = 4.9,
                    totalTrips = 342,
                    isOnline = true,
                    currentLat = 12.9750,
                    currentLng = 77.5850,
                    totalEarnings = 45800.0,
                    walletBalance = 3200.0
                ),
                DriverEntity(
                    id = "drv_02",
                    name = "Vikram Singh",
                    phone = "+91 9822334455",
                    email = "driver2@vtt.com",
                    vehicleCategory = VehicleCategory.INNOVA_CRYSTA,
                    vehicleModel = "Toyota Innova Crysta (Silver)",
                    vehicleNumber = "KA-05-ET-9012",
                    rating = 4.95,
                    totalTrips = 510,
                    isOnline = true,
                    currentLat = 12.9800,
                    currentLng = 77.6100,
                    totalEarnings = 89200.0,
                    walletBalance = 5400.0
                ),
                DriverEntity(
                    id = "drv_03",
                    name = "Suresh Kumar",
                    phone = "+91 9833445566",
                    email = "driver3@vtt.com",
                    vehicleCategory = VehicleCategory.HATCHBACK,
                    vehicleModel = "Maruti Swift (Blue)",
                    vehicleNumber = "KA-03-HA-1188",
                    rating = 4.7,
                    totalTrips = 189,
                    isOnline = true,
                    currentLat = 12.9650,
                    currentLng = 77.5950,
                    totalEarnings = 23400.0,
                    walletBalance = 1800.0
                ),
                DriverEntity(
                    id = "drv_04",
                    name = "Mohd. Ismail",
                    phone = "+91 9844556677",
                    email = "driver4@vtt.com",
                    vehicleCategory = VehicleCategory.SUV,
                    vehicleModel = "Mahindra XUV700 (Black)",
                    vehicleNumber = "KA-04-SV-7700",
                    rating = 4.88,
                    totalTrips = 276,
                    isOnline = true,
                    currentLat = 12.9850,
                    currentLng = 77.6400,
                    totalEarnings = 61200.0,
                    walletBalance = 4100.0
                )
            )
            for (drv in drivers) {
                dao.insertDriver(drv)
                dao.insertUser(
                    UserEntity(
                        id = drv.id,
                        name = drv.name,
                        email = drv.email,
                        phone = drv.phone,
                        role = UserRole.DRIVER
                    )
                )
            }

            // Fare rules
            for (cat in VehicleCategory.values()) {
                dao.insertFareRule(
                    FareRuleEntity(
                        vehicleCategory = cat,
                        baseFare = cat.defaultBaseFare,
                        perKmRate = cat.defaultPerKm,
                        perMinRate = cat.defaultPerMin
                    )
                )
            }

            // Seed a completed sample booking
            dao.insertBooking(
                BookingEntity(
                    id = "vtt_b001",
                    customerId = custUser.id,
                    customerName = custUser.name,
                    customerPhone = custUser.phone,
                    driverId = "drv_01",
                    driverName = "Rajesh Sharma",
                    driverPhone = "+91 9811223344",
                    vehicleNumber = "KA-01-MJ-4821",
                    vehicleModel = "Honda City (White)",
                    vehicleCategory = VehicleCategory.SEDAN,
                    bookingType = BookingType.AIRPORT_DROP,
                    pickupAddress = "MG Road Boulevard, City Center",
                    dropAddress = "International Airport (Terminal 1)",
                    pickupLat = 12.9756,
                    pickupLng = 77.6066,
                    dropLat = 13.1986,
                    dropLng = 77.7066,
                    distanceKm = 34.5,
                    durationMins = 52,
                    totalFare = 785.0,
                    baseFare = 100.0,
                    distanceFare = 517.5,
                    timeFare = 104.0,
                    tollCharge = 35.0,
                    paymentMethod = PaymentMethod.UPI,
                    paymentStatus = PaymentStatus.COMPLETED,
                    bookingStatus = BookingStatus.COMPLETED,
                    createdAt = System.currentTimeMillis() - 86400000,
                    pickupDate = "2026-07-30",
                    pickupTime = "10:30 AM"
                )
            )
        }
    }
}
