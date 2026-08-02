package com.vttcabs.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class VttRepository(private val app: Context) {

    private val users = MutableStateFlow<List<UserEntity>>(getDefaultUsers())
    private val driverRegistrations = MutableStateFlow<List<DriverRegistrationEntity>>(emptyList())
    private val drivers = MutableStateFlow<List<DriverEntity>>(getDefaultDrivers())
    private val bookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val bookingsList: List<BookingEntity> get() = bookings.value
    private val notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    private val ratings = MutableStateFlow<List<RatingEntity>>(emptyList())
    private val sosAlerts = MutableStateFlow<List<SosAlertEntity>>(emptyList())
    val sosAlertsList: List<SosAlertEntity> get() = sosAlerts.value
    private val driverEarnings = MutableStateFlow<List<DriverEarningEntity>>(emptyList())

    val supabaseService = SupabaseService.getInstance()

    val allBookings: Flow<List<BookingEntity>> = bookings
    val allDrivers: Flow<List<DriverEntity>> = drivers
    val driversList: List<DriverEntity> get() = drivers.value
    val allDriverRegistrations: Flow<List<DriverRegistrationEntity>> = driverRegistrations
    val pendingDriverRegistrations: Flow<List<DriverRegistrationEntity>> = 
        driverRegistrations.map { list -> list.filter { 
            it.approvalStatus == DriverApprovalStatus.SUBMITTED || 
            it.approvalStatus == DriverApprovalStatus.PENDING 
        }}
    val onlineDrivers: Flow<List<DriverEntity>> = drivers.map { list -> list.filter { it.isOnline && it.approvalStatus == DriverApprovalStatus.APPROVED } }

    // ==================== OTP AUTHENTICATION ====================

    suspend fun sendOtp(email: String): Result<String> {
        return supabaseService.sendOtp(email)
    }

    suspend fun verifyOtp(email: String, otp: String): Result<Boolean> {
        return supabaseService.verifyOtp(email, otp)
    }

    // ==================== CUSTOMER OPERATIONS ====================

    fun getDriverBookings(driverId: String): Flow<List<BookingEntity>> =
        bookings.map { list -> list.filter { it.driverId == driverId } }

    fun observeBookingById(id: String): Flow<BookingEntity?> =
        bookings.map { list -> list.find { it.id == id } }

    fun getUserNotifications(userId: String): Flow<List<NotificationEntity>> =
        notifications.map { list -> list.filter { it.userId == userId } }

    suspend fun getBookingById(id: String): BookingEntity? =
        bookings.value.find { it.id == id }

    suspend fun getUserByEmail(email: String): UserEntity? =
        users.value.find { it.email.equals(email, ignoreCase = true) }

    suspend fun getUserById(id: String): UserEntity? =
        users.value.find { it.id == id }

    suspend fun saveUser(user: UserEntity) {
        users.value = users.value + user
    }

    suspend fun getBookingsByCustomer(userId: String): List<BookingEntity> =
        bookings.value.filter { it.customerId == userId }.sortedByDescending { it.createdAt }

    suspend fun saveBooking(booking: BookingEntity) {
        bookings.value = bookings.value + booking
        // Notify driver if assigned
        booking.driverId?.let { driverId ->
            addNotification(driverId, UserRole.DRIVER, "New Booking", "You have a new ride request!", "BOOKING", booking.id)
        }
    }

    suspend fun updateBooking(booking: BookingEntity) {
        bookings.value = bookings.value.map { if (it.id == booking.id) booking else it }
        // Emit real-time update
        // Real-time update emitted (demo mode)
    }

    suspend fun loginCustomer(email: String, password: String): UserEntity? {
        return users.value.find { 
            it.email.equals(email, ignoreCase = true) && it.password == password && it.role == UserRole.CUSTOMER 
        }
    }

    suspend fun loginCustomerWithOtp(email: String): UserEntity? {
        return users.value.find { 
            it.email.equals(email, ignoreCase = true) && it.role == UserRole.CUSTOMER 
        }
    }

    suspend fun registerCustomer(name: String, email: String, phone: String, password: String): UserEntity {
        val user = UserEntity(
            id = UUID.randomUUID().toString(), name = name, email = email,
            phone = phone, role = UserRole.CUSTOMER, password = password
        )
        saveUser(user)
        return user
    }

    // ==================== DRIVER REGISTRATION WORKFLOW ====================

    suspend fun createDriverRegistration(email: String, password: String, phone: String, fullName: String): DriverRegistrationEntity {
        val registration = DriverRegistrationEntity(
            id = UUID.randomUUID().toString(),
            email = email,
            password = password,
            fullName = fullName,
            phone = phone,
            approvalStatus = DriverApprovalStatus.DRAFT
        )
        driverRegistrations.value = driverRegistrations.value + registration
        return registration
    }

    suspend fun getDriverRegistrationById(id: String): DriverRegistrationEntity? =
        driverRegistrations.value.find { it.id == id }

    suspend fun getDriverRegistrationByEmail(email: String): DriverRegistrationEntity? =
        driverRegistrations.value.find { it.email.equals(email, ignoreCase = true) }

    suspend fun updateDriverRegistration(registration: DriverRegistrationEntity) {
        driverRegistrations.value = driverRegistrations.value.map { 
            if (it.id == registration.id) registration else it 
        }
    }

    suspend fun updateDriverPersonalInfo(
        registrationId: String,
        fullName: String,
        phone: String,
        alternatePhone: String,
        dateOfBirth: String,
        gender: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
        emergencyContactRelation: String
    ) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        val updated = registration.copy(
            fullName = fullName,
            phone = phone,
            alternatePhone = alternatePhone,
            dateOfBirth = dateOfBirth,
            gender = gender,
            address = address,
            city = city,
            state = state,
            pincode = pincode,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            emergencyContactRelation = emergencyContactRelation,
            personalInfoComplete = true,
            updatedAt = System.currentTimeMillis()
        )
        updateDriverRegistration(updated)
    }

    suspend fun updateDriverDocuments(
        registrationId: String,
        aadhaarNumber: String,
        panCardNumber: String,
        drivingLicenseNumber: String,
        licenseExpiryDate: String,
        aadhaarFrontUrl: String,
        aadhaarBackUrl: String,
        panCardUrl: String,
        licenseFrontUrl: String,
        licenseBackUrl: String,
        profilePhotoUrl: String,
        selfieUrl: String
    ) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        val updated = registration.copy(
            aadhaarNumber = aadhaarNumber,
            panCardNumber = panCardNumber,
            drivingLicenseNumber = drivingLicenseNumber,
            licenseExpiryDate = licenseExpiryDate,
            aadhaarFrontUrl = aadhaarFrontUrl,
            aadhaarBackUrl = aadhaarBackUrl,
            panCardUrl = panCardUrl,
            licenseFrontUrl = licenseFrontUrl,
            licenseBackUrl = licenseBackUrl,
            profilePhotoUrl = profilePhotoUrl,
            selfieUrl = selfieUrl,
            documentsComplete = true,
            updatedAt = System.currentTimeMillis()
        )
        updateDriverRegistration(updated)
    }

    suspend fun updateDriverVehicleInfo(
        registrationId: String,
        vehicleCategory: String,
        vehicleBrand: String,
        vehicleModel: String,
        vehicleYear: String,
        vehicleNumber: String,
        vehicleColor: String,
        seatingCapacity: Int,
        vehicleFrontPhotoUrl: String,
        vehicleBackPhotoUrl: String,
        vehicleLeftPhotoUrl: String,
        vehicleRightPhotoUrl: String
    ) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        val updated = registration.copy(
            vehicleCategory = vehicleCategory,
            vehicleBrand = vehicleBrand,
            vehicleModel = vehicleModel,
            vehicleYear = vehicleYear,
            vehicleNumber = vehicleNumber,
            vehicleColor = vehicleColor,
            seatingCapacity = seatingCapacity,
            vehicleFrontPhotoUrl = vehicleFrontPhotoUrl,
            vehicleBackPhotoUrl = vehicleBackPhotoUrl,
            vehicleLeftPhotoUrl = vehicleLeftPhotoUrl,
            vehicleRightPhotoUrl = vehicleRightPhotoUrl,
            vehicleInfoComplete = true,
            updatedAt = System.currentTimeMillis()
        )
        updateDriverRegistration(updated)
    }

    suspend fun updateDriverBankDetails(
        registrationId: String,
        bankName: String,
        accountHolderName: String,
        accountNumber: String,
        ifscCode: String,
        upiId: String
    ) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        val updated = registration.copy(
            bankName = bankName,
            accountHolderName = accountHolderName,
            accountNumber = accountNumber,
            ifscCode = ifscCode,
            upiId = upiId,
            bankDetailsComplete = true,
            updatedAt = System.currentTimeMillis()
        )
        updateDriverRegistration(updated)
    }

    suspend fun uploadDocument(file: File, bucket: String, folder: String): Result<String> {
        return supabaseService.uploadDocument(app, file, bucket, folder)
    }

    suspend fun uploadImage(bitmap: Bitmap, bucket: String, folder: String, fileName: String): Result<String> {
        return supabaseService.uploadImage(app, bitmap, bucket, folder, fileName)
    }

    suspend fun submitDriverRegistration(registrationId: String): Boolean {
        val registration = getDriverRegistrationById(registrationId) ?: return false
        
        // Validate all sections are complete
        if (!registration.personalInfoComplete || !registration.documentsComplete || 
            !registration.vehicleInfoComplete || !registration.bankDetailsComplete) {
            return false
        }

        val updated = registration.copy(
            approvalStatus = DriverApprovalStatus.SUBMITTED,
            submittedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        updateDriverRegistration(updated)

        // Notify admin
        addNotification("admin", UserRole.ADMIN, "New Driver Application", 
            "${registration.fullName} has submitted their application for review", "DRIVER", registration.id)
        
        return true
    }

    suspend fun convertRegistrationToDriver(registrationId: String): DriverEntity? {
        val registration = getDriverRegistrationById(registrationId) ?: return null
        
        val driver = DriverEntity(
            id = registration.id,
            name = registration.fullName,
            phone = registration.phone,
            email = registration.email,
            vehicleCategory = try { 
                VehicleCategory.valueOf(registration.vehicleCategory) 
            } catch (e: Exception) { VehicleCategory.SEDAN },
            vehicleBrand = registration.vehicleBrand,
            vehicleModel = registration.vehicleModel,
            vehicleYear = registration.vehicleYear,
            vehicleNumber = registration.vehicleNumber,
            vehicleColor = registration.vehicleColor,
            seatingCapacity = registration.seatingCapacity,
            rating = 5.0,
            totalTrips = 0,
            isOnline = false,
            totalEarnings = 0.0,
            walletBalance = 0.0,
            isApproved = true,
            address = registration.address,
            emergencyContact = registration.emergencyContactPhone,
            aadhaarNumber = registration.aadhaarNumber,
            aadhaarFrontUrl = registration.aadhaarFrontUrl,
            aadhaarBackUrl = registration.aadhaarBackUrl,
            panCardNumber = registration.panCardNumber,
            panCardUrl = registration.panCardUrl,
            licenceNumber = registration.drivingLicenseNumber,
            licenceFrontUrl = registration.licenseFrontUrl,
            licenceBackUrl = registration.licenseBackUrl,
            profilePhotoUrl = registration.profilePhotoUrl,
            selfieUrl = registration.selfieUrl,
            vehicleFrontPhotoUrl = registration.vehicleFrontPhotoUrl,
            vehicleBackPhotoUrl = registration.vehicleBackPhotoUrl,
            vehicleLeftSidePhotoUrl = registration.vehicleLeftPhotoUrl,
            vehicleRightSidePhotoUrl = registration.vehicleRightPhotoUrl,
            bankName = registration.bankName,
            accountNumber = registration.accountNumber,
            ifscCode = registration.ifscCode,
            upiId = registration.upiId,
            approvalStatus = DriverApprovalStatus.APPROVED,
            password = registration.password
        )
        
        saveDriver(driver)
        
        // Update registration status
        updateDriverRegistration(registration.copy(
            approvalStatus = DriverApprovalStatus.APPROVED,
            isVerified = true,
            reviewedAt = System.currentTimeMillis()
        ))
        
        return driver
    }

    // ==================== DRIVER OPERATIONS ====================

    suspend fun getDriverById(id: String): DriverEntity? =
        drivers.value.find { it.id == id }

    suspend fun getDriverByEmail(email: String): DriverEntity? =
        drivers.value.find { it.email.equals(email, ignoreCase = true) }

    suspend fun getDriverByPhone(phone: String): DriverEntity? =
        drivers.value.find { it.phone == phone }

    suspend fun saveDriver(driver: DriverEntity) {
        drivers.value = drivers.value + driver
        // Also create user account for driver
        if (users.value.none { it.id == driver.id }) {
            saveUser(UserEntity(
                id = driver.id, name = driver.name, email = driver.email,
                phone = driver.phone, role = UserRole.DRIVER, password = driver.password
            ))
        }
    }

    suspend fun loginDriver(email: String, password: String): DriverEntity? {
        return drivers.value.find {
            (it.email.equals(email, ignoreCase = true) || it.phone == email) &&
            it.password == password && it.approvalStatus == DriverApprovalStatus.APPROVED
        }
    }

    suspend fun loginDriverWithOtp(email: String): DriverEntity? {
        return drivers.value.find {
            it.email.equals(email, ignoreCase = true) && it.approvalStatus == DriverApprovalStatus.APPROVED
        }
    }

    suspend fun assignDriverToBooking(bookingId: String, driverId: String) {
        val driver = getDriverById(driverId) ?: return
        val booking = getBookingById(bookingId) ?: return
        val tripOtp = String.format("%04d", (1000..9999).random())
        
        updateBooking(booking.copy(
            driverId = driverId, driverName = driver.name, driverPhone = driver.phone,
            vehicleNumber = driver.vehicleNumber, vehicleModel = driver.vehicleModel,
            bookingStatus = BookingStatus.ASSIGNED, assignedAt = System.currentTimeMillis(),
            tripOtp = tripOtp
        ))
        
        // Notify driver
        addNotification(driverId, UserRole.DRIVER, "New Booking Assigned", 
            "Booking from ${booking.customerName}. OTP: $tripOtp", "BOOKING", bookingId)
        
        // Notify customer
        addNotification(booking.customerId, UserRole.CUSTOMER, "Driver Assigned", 
            "${driver.name} will pick you up. Vehicle: ${driver.vehicleNumber}", "BOOKING", bookingId)
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus, driverId: String? = null) {
        val booking = getBookingById(bookingId) ?: return
        val now = System.currentTimeMillis()
        
        val updatedBooking = when (status) {
            BookingStatus.ACCEPTED -> booking.copy(bookingStatus = status, acceptedAt = now)
            BookingStatus.TRIP_STARTED -> {
                // Calculate driver earnings
                val (netEarning, platformFee) = supabaseService.calculateDriverEarnings(booking.totalFare)
                recordDriverEarning(driverId ?: "", bookingId, booking.totalFare, netEarning, platformFee)
                booking.copy(bookingStatus = status, startedAt = now, driverEarning = netEarning, companyShare = platformFee)
            }
            BookingStatus.COMPLETED -> booking.copy(bookingStatus = status, completedAt = now, paymentStatus = PaymentStatus.COMPLETED)
            else -> booking.copy(bookingStatus = status)
        }
        
        updateBooking(updatedBooking)
        
        // Send notifications based on status
        when (status) {
            BookingStatus.ACCEPTED -> {
                addNotification(booking.customerId, UserRole.CUSTOMER, "Ride Accepted", 
                    "${booking.driverName} has accepted your ride", "BOOKING", bookingId)
            }
            BookingStatus.DRIVER_ARRIVING -> {
                addNotification(booking.customerId, UserRole.CUSTOMER, "Driver Arriving", 
                    "${booking.driverName} is on the way to pick you up", "BOOKING", bookingId)
            }
            BookingStatus.ARRIVED -> {
                addNotification(booking.customerId, UserRole.CUSTOMER, "Driver Arrived", 
                    "${booking.driverName} has arrived at pickup location. Share OTP: ${booking.tripOtp}", "BOOKING", bookingId)
            }
            BookingStatus.TRIP_STARTED -> {
                addNotification(booking.customerId, UserRole.CUSTOMER, "Trip Started", 
                    "Your trip to ${booking.dropAddress} has begun. Have a safe journey!", "BOOKING", bookingId)
            }
            BookingStatus.COMPLETED -> {
                addNotification(booking.customerId, UserRole.CUSTOMER, "Trip Completed", 
                    "Trip completed. Fare: ₹${booking.totalFare}. Please rate your driver.", "PAYMENT", bookingId)
            }
            BookingStatus.CANCELLED -> {
                driverId?.let { 
                    addNotification(it, UserRole.DRIVER, "Booking Cancelled", 
                        "The booking was cancelled by the customer", "BOOKING", bookingId)
                }
            }
            else -> {}
        }
    }

    suspend fun setDriverOnline(driverId: String, isOnline: Boolean) {
        val driver = getDriverById(driverId) ?: return
        
        if (driver.approvalStatus != DriverApprovalStatus.APPROVED) {
            Log.w("VttRepository", "Driver not approved, cannot go online")
            return
        }
        
        drivers.value = drivers.value.map { 
            if (it.id == driverId) it.copy(isOnline = isOnline) else it 
        }
        
        addNotification(driverId, UserRole.DRIVER, 
            if (isOnline) "You are now Online" else "You are now Offline",
            if (isOnline) "You will receive ride requests" else "You won't receive ride requests",
            "SYSTEM", "")
    }

    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        drivers.value = drivers.value.map { 
            if (it.id == driverId) it.copy(currentLat = lat, currentLng = lng) else it 
        }
        // Emit real-time update
        // Location update emitted (demo mode)
    }

    // ==================== ADMIN OPERATIONS ====================

    suspend fun loginAdmin(email: String, password: String): UserEntity? {
        return users.value.find {
            (it.email.equals(email, ignoreCase = true) || it.phone == email) &&
            it.password == password && it.role == UserRole.ADMIN
        }
    }

    suspend fun approveDriver(registrationId: String, adminId: String) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        
        updateDriverRegistration(registration.copy(
            approvalStatus = DriverApprovalStatus.APPROVED,
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = adminId,
            isVerified = true
        ))
        
        // Create actual driver account
        convertRegistrationToDriver(registrationId)
        
        // Notify driver
        addNotification(registrationId, UserRole.DRIVER, "Application Approved!", 
            "Congratulations! Your driver account has been approved. You can now login and start driving.", 
            "DRIVER", registrationId)
    }

    suspend fun rejectDriver(registrationId: String, adminId: String, reason: String) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        
        updateDriverRegistration(registration.copy(
            approvalStatus = DriverApprovalStatus.REJECTED,
            rejectionReason = reason,
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = adminId
        ))
        
        // Notify driver
        addNotification(registrationId, UserRole.DRIVER, "Application Rejected", 
            "Your application was rejected. Reason: $reason. Please submit corrected documents.", 
            "DRIVER", registrationId)
    }

    suspend fun requestDriverReupload(registrationId: String, adminId: String, documents: List<String>) {
        val registration = getDriverRegistrationById(registrationId) ?: return
        
        updateDriverRegistration(registration.copy(
            approvalStatus = DriverApprovalStatus.SUBMITTED,
            reuploadRequestedDocs = documents.joinToString(","),
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = adminId
        ))
        
        // Notify driver
        addNotification(registrationId, UserRole.DRIVER, "Document Re-upload Required", 
            "Please re-upload the following documents: ${documents.joinToString(", ")}", 
            "DRIVER", registrationId)
    }

    suspend fun suspendDriver(driverId: String, adminId: String, reason: String) {
        drivers.value = drivers.value.map { 
            if (it.id == driverId) it.copy(
                approvalStatus = DriverApprovalStatus.SUSPENDED,
                isOnline = false,
                rejectionReason = reason
            ) else it 
        }
        
        addNotification(driverId, UserRole.DRIVER, "Account Suspended", 
            "Your account has been suspended. Reason: $reason. Contact support for details.", 
            "DRIVER", driverId)
    }

    suspend fun activateDriver(driverId: String) {
        drivers.value = drivers.value.map { 
            if (it.id == driverId) it.copy(
                approvalStatus = DriverApprovalStatus.APPROVED,
                rejectionReason = ""
            ) else it 
        }
        
        addNotification(driverId, UserRole.DRIVER, "Account Reactivated", 
            "Your account has been reactivated. You can now go online and receive rides.", 
            "DRIVER", driverId)
    }

    suspend fun getAllDriverRegistrations(): List<DriverRegistrationEntity> =
        driverRegistrations.value.sortedByDescending { it.submittedAt }

    suspend fun getPendingDriverRegistrations(): List<DriverRegistrationEntity> =
        driverRegistrations.value.filter { 
            it.approvalStatus == DriverApprovalStatus.SUBMITTED || 
            it.approvalStatus == DriverApprovalStatus.PENDING 
        }.sortedByDescending { it.submittedAt }

    fun getAdminDashboardStats(): AdminDashboardStats {
        val allDrivers = drivers.value
        val allBookings = bookings.value
        val pendingRegistrations = driverRegistrations.value.filter { 
            it.approvalStatus == DriverApprovalStatus.SUBMITTED || 
            it.approvalStatus == DriverApprovalStatus.PENDING 
        }
        
        val completedTrips = allBookings.count { it.bookingStatus == BookingStatus.COMPLETED }
        val cancelledTrips = allBookings.count { it.bookingStatus == BookingStatus.CANCELLED }
        val totalRevenue = allBookings.filter { it.bookingStatus == BookingStatus.COMPLETED }.sumOf { it.totalFare }
        
        val now = System.currentTimeMillis()
        val todayStart = now - (now % 86400000)
        val weekStart = todayStart - (7 * 86400000)
        val monthStart = todayStart - (30 * 86400000)
        
        return AdminDashboardStats(
            totalDrivers = allDrivers.size,
            activeDrivers = allDrivers.count { it.isOnline && it.approvalStatus == DriverApprovalStatus.APPROVED },
            pendingDriverApplications = pendingRegistrations.size,
            rejectedDrivers = allDrivers.count { it.approvalStatus == DriverApprovalStatus.REJECTED } + 
                             driverRegistrations.value.count { it.approvalStatus == DriverApprovalStatus.REJECTED },
            suspendedDrivers = allDrivers.count { it.approvalStatus == DriverApprovalStatus.SUSPENDED },
            totalCustomers = users.value.count { it.role == UserRole.CUSTOMER },
            totalBookings = allBookings.size,
            completedTrips = completedTrips,
            cancelledTrips = cancelledTrips,
            totalRevenue = totalRevenue,
            todayRevenue = allBookings.filter { it.completedAt >= todayStart }.sumOf { it.totalFare },
            weekRevenue = allBookings.filter { it.completedAt >= weekStart }.sumOf { it.totalFare },
            monthRevenue = allBookings.filter { it.completedAt >= monthStart }.sumOf { it.totalFare },
            averageRating = if (allDrivers.isNotEmpty()) allDrivers.map { it.rating }.average() else 0.0,
            sosAlerts = sosAlerts.value.count { it.status == "ACTIVE" }
        )
    }

    // ==================== BOOKING & PAYMENT ====================

    suspend fun completeTrip(bookingId: String, driverId: String, fare: Double) {
        val booking = getBookingById(bookingId) ?: return
        updateBooking(booking.copy(
            bookingStatus = BookingStatus.COMPLETED, 
            paymentStatus = PaymentStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        ))
        addNotification(booking.customerId, UserRole.CUSTOMER, "Ride Completed",
            "Receipt of ₹$fare paid via ${booking.paymentMethod.name}.",
            "PAYMENT", bookingId)
    }

    suspend fun cancelBooking(bookingId: String, reason: String, cancelledBy: String) {
        val booking = getBookingById(bookingId) ?: return
        updateBooking(booking.copy(
            bookingStatus = BookingStatus.CANCELLED, 
            cancelledReason = reason,
            cancelledBy = cancelledBy
        ))
        
        // Notify the other party
        when (cancelledBy) {
            booking.customerId -> {
                booking.driverId?.let {
                    addNotification(it, UserRole.DRIVER, "Booking Cancelled", 
                        "Customer cancelled the booking. Reason: $reason", "BOOKING", bookingId)
                }
            }
            booking.driverId -> {
                addNotification(booking.customerId, UserRole.CUSTOMER, "Booking Cancelled", 
                    "Driver cancelled the booking. Reason: $reason. We're finding you a new driver.", "BOOKING", bookingId)
            }
        }
    }

    // ==================== RATINGS & REVIEWS ====================

    suspend fun submitRating(
        bookingId: String,
        fromUserId: String,
        toUserId: String,
        fromUserType: UserRole,
        toUserType: UserRole,
        rating: Int,
        review: String
    ) {
        val ratingEntity = RatingEntity(
            id = UUID.randomUUID().toString(),
            bookingId = bookingId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            fromUserType = fromUserType,
            toUserType = toUserType,
            rating = rating,
            review = review
        )
        ratings.value = ratings.value + ratingEntity
        
        // Update user's/driver's average rating
        when (toUserType) {
            UserRole.DRIVER -> {
                val driverRatings = ratings.value.filter { it.toUserId == toUserId && it.toUserType == UserRole.DRIVER }
                val avgRating = driverRatings.map { it.rating }.average()
                drivers.value = drivers.value.map { 
                    if (it.id == toUserId) it.copy(rating = avgRating) else it 
                }
            }
            UserRole.CUSTOMER -> {
                val customerRatings = ratings.value.filter { it.toUserId == toUserId && it.toUserType == UserRole.CUSTOMER }
                val avgRating = customerRatings.map { it.rating }.average()
                users.value = users.value.map { 
                    if (it.id == toUserId) it.copy(rating = avgRating) else it 
                }
            }
            else -> {}
        }
        
        // Thank notification
        addNotification(fromUserId, fromUserType, "Thank you for your feedback!", 
            "Your rating has been submitted successfully.", "GENERAL", bookingId)
    }

    suspend fun getDriverRatings(driverId: String): List<RatingEntity> =
        ratings.value.filter { it.toUserId == driverId && it.toUserType == UserRole.DRIVER }
            .sortedByDescending { it.createdAt }

    suspend fun getBookingRatings(bookingId: String): List<RatingEntity> =
        ratings.value.filter { it.bookingId == bookingId }

    // ==================== SOS ====================

    suspend fun triggerSos(bookingId: String, userId: String, userType: UserRole, lat: Double, lng: Double) {
        val sosAlert = SosAlertEntity(
            id = UUID.randomUUID().toString(),
            bookingId = bookingId,
            userId = userId,
            userType = userType,
            latitude = lat,
            longitude = lng,
            triggeredAt = System.currentTimeMillis(),
            status = "ACTIVE"
        )
        sosAlerts.value = sosAlerts.value + sosAlert
        
        // Emit SOS alert
        // SOS alert emitted (demo mode)
        
        // Notify admin immediately
        addNotification("admin", UserRole.ADMIN, "🚨 SOS ALERT!", 
            "Emergency alert triggered by ${if (userType == UserRole.CUSTOMER) "customer" else "driver"} in booking $bookingId. Location: $lat, $lng", 
            "SOS", bookingId)
        
        // Notify the other party in the booking
        val booking = getBookingById(bookingId)
        when (userType) {
            UserRole.CUSTOMER -> {
                booking?.driverId?.let {
                    addNotification(it, UserRole.DRIVER, "🚨 Emergency Alert!", 
                        "SOS triggered by customer in your booking!", "SOS", bookingId)
                }
            }
            UserRole.DRIVER -> {
                booking?.customerId?.let {
                    addNotification(it, UserRole.CUSTOMER, "🚨 Emergency Alert!", 
                        "SOS triggered by driver in your booking!", "SOS", bookingId)
                }
            }
            else -> {}
        }
    }

    suspend fun acknowledgeSos(alertId: String, adminId: String) {
        sosAlerts.value = sosAlerts.value.map { alert ->
            if (alert.id == alertId) alert.copy(
                status = "ACKNOWLEDGED",
                acknowledgedBy = adminId
            ) else alert
        }
    }

    suspend fun resolveSos(alertId: String, adminId: String, notes: String) {
        sosAlerts.value = sosAlerts.value.map { alert ->
            if (alert.id == alertId) alert.copy(
                status = "RESOLVED",
                acknowledgedBy = adminId,
                resolvedAt = System.currentTimeMillis(),
                notes = notes
            ) else alert
        }
    }

    suspend fun getActiveSosAlerts(): List<SosAlertEntity> =
        sosAlerts.value.filter { it.status == "ACTIVE" }

    // ==================== NOTIFICATIONS ====================

    suspend fun addNotification(
        userId: String,
        userType: UserRole,
        title: String,
        message: String,
        type: String = "GENERAL",
        referenceId: String = ""
    ) {
        val notification = NotificationEntity(
            userId = userId,
            userType = userType,
            title = title,
            message = message,
            type = type,
            referenceId = referenceId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notifications.value = notifications.value + notification
        
        // Emit real-time notification
        // Notification emitted (demo mode)
        
        // Send push notification
        supabaseService.sendPushNotification(userId, title, message)
    }

    suspend fun markNotificationRead(notificationId: Long) {
        notifications.value = notifications.value.map { 
            if (it.id == notificationId) it.copy(isRead = true) else it 
        }
    }

    suspend fun getUnreadNotifications(userId: String): List<NotificationEntity> =
        notifications.value.filter { it.userId == userId && !it.isRead }

    // ==================== DRIVER EARNINGS ====================

    private suspend fun recordDriverEarning(driverId: String, bookingId: String, grossAmount: Double, netEarning: Double, platformFee: Double) {
        val earning = DriverEarningEntity(
            id = UUID.randomUUID().toString(),
            driverId = driverId,
            bookingId = bookingId,
            grossAmount = grossAmount,
            platformFee = platformFee,
            gstAmount = platformFee * 0.05,
            netEarning = netEarning,
            tripDate = System.currentTimeMillis(),
            paymentStatus = PaymentStatus.PENDING
        )
        driverEarnings.value = driverEarnings.value + earning
        
        // Update driver's total earnings
        drivers.value = drivers.value.map { driver ->
            if (driver.id == driverId) driver.copy(
                totalEarnings = driver.totalEarnings + netEarning,
                walletBalance = driver.walletBalance + netEarning,
                totalTrips = driver.totalTrips + 1
            ) else driver
        }
    }

    suspend fun getDriverEarnings(driverId: String): List<DriverEarningEntity> =
        driverEarnings.value.filter { it.driverId == driverId }.sortedByDescending { it.tripDate }

    suspend fun getDriverEarningSummary(driverId: String): DriverEarningSummary? {
        val driver = getDriverById(driverId) ?: return null
        val earnings = getDriverEarnings(driverId)
        
        val now = System.currentTimeMillis()
        val weekStart = now - (7 * 86400000)
        val monthStart = now - (30 * 86400000)
        
        val grossEarnings = earnings.sumOf { it.grossAmount }
        val platformFees = earnings.sumOf { it.platformFee }
        val pendingPayout = earnings.filter { it.paymentStatus == PaymentStatus.PENDING }.sumOf { it.netEarning }
        
        return DriverEarningSummary(
            driverId = driverId,
            driverName = driver.name,
            totalTrips = earnings.size,
            grossEarnings = grossEarnings,
            platformFees = platformFees,
            netEarnings = grossEarnings - platformFees,
            pendingPayout = pendingPayout,
            thisWeekEarnings = earnings.filter { it.tripDate >= weekStart }.sumOf { it.netEarning },
            thisMonthEarnings = earnings.filter { it.tripDate >= monthStart }.sumOf { it.netEarning }
        )
    }

    suspend fun requestPayout(driverId: String, amount: Double): Result<Boolean> {
        val driver = getDriverById(driverId) ?: return Result.failure(Exception("Driver not found"))
        
        if (driver.walletBalance < amount) {
            return Result.failure(Exception("Insufficient balance"))
        }
        
        // In production, integrate with payment gateway
        drivers.value = drivers.value.map { d ->
            if (d.id == driverId) d.copy(walletBalance = d.walletBalance - amount) else d
        }
        // Mark earnings as paid (simplified for demo)
        
        addNotification(driverId, UserRole.DRIVER, "Payout Initiated", 
            "Your payout of ₹$amount has been initiated. It will be credited within 2-3 business days.", 
            "PAYMENT", "")
        
        return Result.success(true)
    }

    // ==================== DEFAULT DATA ====================

    private fun getDefaultUsers(): List<UserEntity> = listOf(
        UserEntity("user1", "Test Customer", "test@vtt.com", "9876543210", UserRole.CUSTOMER, password = "test123"),
        UserEntity("admin1", "Admin User", "admin@vttcabs.com", "9999999999", UserRole.ADMIN, password = "VTT@Admin2024")
    )

    private fun getDefaultDrivers(): List<DriverEntity> = listOf(
        DriverEntity("driver1", "John Driver", "9876543211", "driver1@vtt.com", VehicleCategory.SEDAN, vehicleModel = "Honda City", vehicleNumber = "KA-01-AB-1234", password = "driver123", approvalStatus = DriverApprovalStatus.APPROVED),
        DriverEntity("driver2", "Mike Driver", "9876543212", "driver2@vtt.com", VehicleCategory.ERTIGA, vehicleModel = "Maruti Ertiga", vehicleNumber = "KA-01-CD-5678", password = "driver123", approvalStatus = DriverApprovalStatus.APPROVED)
    )
}
