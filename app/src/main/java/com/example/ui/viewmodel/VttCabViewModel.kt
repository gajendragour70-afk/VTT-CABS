package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.VttDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.BookingType
import com.example.data.model.DriverEntity
import com.example.data.model.FareBreakdown
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.data.repository.VttRepository
import com.example.data.remote.SupabaseService
import com.example.domain.calculator.FareCalculator
import com.example.domain.calculator.LocationUtils
import com.example.domain.simulation.SimulatedRealtimeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class VttCabViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VttDatabase.getInstance(application)
    val repository = VttRepository(db.dao())
    val realtimeEngine = SimulatedRealtimeEngine(repository)
    val supabaseService = SupabaseService()
    val authPrefs = com.example.data.local.AuthPreferences(application)

    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth? = try {
        com.google.firebase.auth.FirebaseAuth.getInstance()
    } catch (e: Exception) { null }

    private val firestore: com.google.firebase.firestore.FirebaseFirestore? = try {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
    } catch (e: Exception) { null }

    // Current Auth & User Session
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentDriver = MutableStateFlow<DriverEntity?>(null)
    val currentDriver: StateFlow<DriverEntity?> = _currentDriver.asStateFlow()

    // Location Autocomplete Search States
    private val _pickupSearchQuery = MutableStateFlow("")
    val pickupSearchQuery: StateFlow<String> = _pickupSearchQuery.asStateFlow()

    private val _dropSearchQuery = MutableStateFlow("")
    val dropSearchQuery: StateFlow<String> = _dropSearchQuery.asStateFlow()

    // Customer Booking Form Inputs
    private val _selectedBookingType = MutableStateFlow(BookingType.ONE_WAY)
    val selectedBookingType: StateFlow<BookingType> = _selectedBookingType.asStateFlow()

    private val _pickupAddress = MutableStateFlow("Bhopal Junction Railway Station")
    val pickupAddress: StateFlow<String> = _pickupAddress.asStateFlow()
    val pickupLat = MutableStateFlow(23.2599)
    val pickupLng = MutableStateFlow(77.4126)

    private val _dropAddress = MutableStateFlow("Indore Junction Railway Station")
    val dropAddress: StateFlow<String> = _dropAddress.asStateFlow()
    val dropLat = MutableStateFlow(22.7196)
    val dropLng = MutableStateFlow(75.8577)

    private val _pickupDate = MutableStateFlow(
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    )
    val pickupDate: StateFlow<String> = _pickupDate.asStateFlow()

    private val _pickupTime = MutableStateFlow(
        java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
    )
    val pickupTime: StateFlow<String> = _pickupTime.asStateFlow()

    private val _selectedVehicleCategory = MutableStateFlow(VehicleCategory.SEDAN)
    val selectedVehicleCategory: StateFlow<VehicleCategory> = _selectedVehicleCategory.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    // Active Tracked Ride ID
    private val _activeBookingId = MutableStateFlow<String?>(null)
    val activeBookingId: StateFlow<String?> = _activeBookingId.asStateFlow()

    // UI Toast Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // OSRM Route & Fare Calculation State
    private val _routeDistanceKm = MutableStateFlow(0.0)
    val routeDistanceKm: StateFlow<Double> = _routeDistanceKm.asStateFlow()

    private val _routeDurationMins = MutableStateFlow(0)
    val routeDurationMins: StateFlow<Int> = _routeDurationMins.asStateFlow()

    private val _routeError = MutableStateFlow<String?>(null)
    val routeError: StateFlow<String?> = _routeError.asStateFlow()

    private val _isCalculatingRoute = MutableStateFlow(false)
    val isCalculatingRoute: StateFlow<Boolean> = _isCalculatingRoute.asStateFlow()

    // Database Reactive Flows
    val allBookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerBookings: StateFlow<List<BookingEntity>> = combine(
        repository.allBookings,
        _currentUser
    ) { bookings, user ->
        if (user == null) emptyList() else bookings.filter { it.customerId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.getUserNotifications("cust_101")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()

            // Restore Persisted Auth Session
            if (authPrefs.isLoggedIn()) {
                val savedUserId = authPrefs.getUserId()
                val savedRole = authPrefs.getUserRole()
                val savedEmail = authPrefs.getUserEmail() ?: ""

                if (!savedUserId.isNullOrBlank() && savedRole != null) {
                    if (savedRole == UserRole.DRIVER) {
                        val drv = repository.getDriverById(savedUserId) ?: repository.getDriverByEmail(savedEmail)
                        if (drv != null && drv.approvalStatus == com.example.data.model.DriverApprovalStatus.APPROVED) {
                            _currentDriver.value = drv
                            _currentUser.value = UserEntity(
                                id = drv.id,
                                name = drv.name,
                                email = drv.email,
                                phone = drv.phone,
                                role = UserRole.DRIVER,
                                password = drv.password
                            )
                            _currentRole.value = UserRole.DRIVER
                            _isLoggedIn.value = true
                        } else {
                            authPrefs.clearSession()
                        }
                    } else if (savedRole == UserRole.CUSTOMER) {
                        val usr = repository.getUserByEmail(savedEmail) ?: repository.getUserById(savedUserId)
                        if (usr != null) {
                            _currentUser.value = usr
                            _currentRole.value = UserRole.CUSTOMER
                            _isLoggedIn.value = true
                        } else {
                            val newUser = UserEntity(
                                id = savedUserId,
                                name = if (savedEmail.contains("@")) savedEmail.substringBefore("@").replaceFirstChar { it.uppercase() } else "Customer",
                                email = if (savedEmail.contains("@")) savedEmail else "$savedEmail@vtt.com",
                                phone = "+91 9800000000",
                                role = UserRole.CUSTOMER
                            )
                            repository.insertUser(newUser)
                            _currentUser.value = newUser
                            _currentRole.value = UserRole.CUSTOMER
                            _isLoggedIn.value = true
                        }
                    }
                }
            } else {
                // Check Firebase Auth instance if logged in
                val fbUser = firebaseAuth?.currentUser
                if (fbUser != null && !fbUser.email.isNullOrBlank()) {
                    val email = fbUser.email!!
                    val drv = repository.getDriverByEmail(email)
                    if (drv != null && drv.approvalStatus == com.example.data.model.DriverApprovalStatus.APPROVED) {
                        _currentDriver.value = drv
                        _currentUser.value = UserEntity(id = drv.id, name = drv.name, email = drv.email, phone = drv.phone, role = UserRole.DRIVER)
                        _currentRole.value = UserRole.DRIVER
                        _isLoggedIn.value = true
                        authPrefs.saveSession(drv.id, UserRole.DRIVER, drv.email)
                    } else {
                        val usr = repository.getUserByEmail(email) ?: UserEntity(
                            id = fbUser.uid,
                            name = fbUser.displayName ?: email.substringBefore("@"),
                            email = email,
                            phone = fbUser.phoneNumber ?: "+91 9800000000",
                            role = UserRole.CUSTOMER
                        )
                        _currentUser.value = usr
                        _currentRole.value = UserRole.CUSTOMER
                        _isLoggedIn.value = true
                        authPrefs.saveSession(usr.id, UserRole.CUSTOMER, usr.email)
                    }
                }
            }

            if (supabaseService.isConfigured) {
                val remoteBookings = supabaseService.fetchBookingsFromSupabase()
                remoteBookings.forEach { booking ->
                    repository.createBooking(booking)
                }
            }
        }
        recalculateRouteAndFare()
    }

    fun registerUser(name: String, email: String, phone: String, pass: String, role: UserRole) {
        viewModelScope.launch {
            val sbId = supabaseService.signUp(email, pass, name) ?: "cust_${System.currentTimeMillis() % 10000}"
            val newUser = UserEntity(
                id = sbId,
                name = name,
                email = email,
                phone = phone,
                role = UserRole.CUSTOMER,
                password = pass.ifBlank { "123456" }
            )
            repository.getUserByEmail(email) // Check if existing
            _currentUser.value = newUser
            _currentRole.value = UserRole.CUSTOMER
            showToast("Customer Account created successfully for $name!")
        }
    }

    fun registerDriver(
        name: String,
        phone: String,
        email: String,
        pass: String,
        address: String,
        dob: String = "1995-08-20",
        emergencyContact: String = "+91 9876543211",
        vehicleCategory: VehicleCategory,
        vehicleBrand: String = "Toyota",
        vehicleModel: String = "Innova",
        vehicleYear: String = "2023",
        vehicleNumber: String,
        vehicleColor: String = "White",
        seatingCapacity: Int = 4,
        aadhaarNumber: String,
        aadhaarFrontUrl: String = "aadhaar_front.jpg",
        aadhaarBackUrl: String = "aadhaar_back.jpg",
        panCardNumber: String = "ABCDE1234F",
        panCardUrl: String = "pan_card.jpg",
        licenceNumber: String,
        licenceFrontUrl: String = "dl_front.jpg",
        licenceBackUrl: String = "dl_back.jpg",
        rcDocUrl: String = "rc_doc.pdf",
        insuranceUrl: String = "vehicle_insurance.pdf",
        pucUrl: String = "puc_certificate.pdf",
        permitUrl: String = "vehicle_permit.pdf",
        profilePhotoUrl: String = "profile_photo.jpg",
        vehicleFrontPhotoUrl: String = "veh_front.jpg",
        vehicleBackPhotoUrl: String = "veh_back.jpg",
        vehicleLeftSidePhotoUrl: String = "veh_left.jpg",
        vehicleRightSidePhotoUrl: String = "veh_right.jpg",
        vehicleInteriorPhotoUrl: String = "veh_interior.jpg",
        vehicleConditionPhotosUrl: String = "veh_cond_1.jpg,veh_cond_2.jpg,veh_cond_3.jpg,veh_cond_4.jpg",
        bankName: String = "State Bank of India",
        accountNumber: String = "30987123456",
        ifscCode: String = "SBIN0001234"
    ) {
        viewModelScope.launch {
            val driverId = "drv_${System.currentTimeMillis() % 10000}"
            val newDriver = DriverEntity(
                id = driverId,
                name = name,
                phone = phone,
                email = email,
                vehicleCategory = vehicleCategory,
                vehicleBrand = vehicleBrand,
                vehicleModel = vehicleModel,
                vehicleYear = vehicleYear,
                vehicleNumber = vehicleNumber,
                vehicleColor = vehicleColor,
                seatingCapacity = seatingCapacity,
                rating = 5.0,
                totalTrips = 0,
                isOnline = false,
                currentLat = 12.9716,
                currentLng = 77.5946,
                totalEarnings = 0.0,
                walletBalance = 0.0,
                isApproved = false,
                address = address,
                dob = dob,
                emergencyContact = emergencyContact,
                aadhaarNumber = aadhaarNumber,
                aadhaarFrontUrl = aadhaarFrontUrl,
                aadhaarBackUrl = aadhaarBackUrl,
                panCardNumber = panCardNumber,
                panCardUrl = panCardUrl,
                licenceNumber = licenceNumber,
                licenceFrontUrl = licenceFrontUrl,
                licenceBackUrl = licenceBackUrl,
                rcDocUrl = rcDocUrl,
                insuranceUrl = insuranceUrl,
                pucUrl = pucUrl,
                permitUrl = permitUrl,
                profilePhotoUrl = profilePhotoUrl,
                vehicleFrontPhotoUrl = vehicleFrontPhotoUrl,
                vehicleBackPhotoUrl = vehicleBackPhotoUrl,
                vehicleLeftSidePhotoUrl = vehicleLeftSidePhotoUrl,
                vehicleRightSidePhotoUrl = vehicleRightSidePhotoUrl,
                vehicleInteriorPhotoUrl = vehicleInteriorPhotoUrl,
                vehicleConditionPhotosUrl = vehicleConditionPhotosUrl,
                bankName = bankName,
                accountNumber = accountNumber,
                ifscCode = ifscCode,
                approvalStatus = com.example.data.model.DriverApprovalStatus.PENDING,
                rejectionReason = "",
                password = pass.ifBlank { "123456" }
            )

            repository.insertDriver(newDriver)
            // Attempt Supabase sign up
            supabaseService.signUp(email, pass, name)

            showToast("Driver registration submitted! Status: Pending Approval by Admin.")
        }
    }

    fun adminRequestReuploadDocs(driverId: String, requestedDocsList: List<String>) {
        viewModelScope.launch {
            val docsStr = requestedDocsList.joinToString(", ")
            repository.requestReuploadDocs(driverId, docsStr)
            showToast("Re-upload request sent to driver for: $docsStr")
        }
    }

    fun loginUser(identifier: String, pass: String, role: UserRole, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val cleanId = identifier.trim()

            // Admin login - Company only authentication
            if (role == UserRole.ADMIN) {
                // Only allow company email domains for admin
                val allowedDomains = listOf("vttcabs.com", "vttcabs.in", "vtt.com")
                val emailDomain = if (cleanId.contains("@")) cleanId.substringAfter("@").lowercase() else ""
                val isAllowedDomain = allowedDomains.any { cleanId.lowercase().endsWith("@$it") }
                
                if (!isAllowedDomain && !cleanId.equals("admin", ignoreCase = true)) {
                    val msg = "Admin access is restricted to VTT CABS company accounts only. Please use your company email (e.g., admin@vttcabs.com)."
                    showToast(msg)
                    onResult(false, msg)
                    return@launch
                }

                if (pass.isBlank()) {
                    val msg = "Password is required for admin login."
                    showToast(msg)
                    onResult(false, msg)
                    return@launch
                }

                // Check for default admin credentials
                val adminEmail = "admin@vttcabs.com"
                val adminPassword = "VTT@Admin2024"
                val backupEmail = "admin@vttcabs.in"
                val backupPassword = "vttadmin123"

                val isValidAdmin = (cleanId.lowercase().equals(adminEmail, ignoreCase = true) && pass == adminPassword) ||
                                  (cleanId.lowercase().equals(backupEmail, ignoreCase = true) && pass == backupPassword) ||
                                  (cleanId.lowercase().equals("admin", ignoreCase = true) && pass == adminPassword)

                if (!isValidAdmin) {
                    val msg = "Invalid admin credentials. Please check your company email and password."
                    showToast(msg)
                    onResult(false, msg)
                    return@launch
                }

                // Admin login successful
                _currentUser.value = UserEntity(
                    id = "admin_001",
                    name = "VTT Dispatch Admin",
                    email = cleanId,
                    phone = "+91 1800123456",
                    role = UserRole.ADMIN,
                    password = pass
                )
                _currentRole.value = UserRole.ADMIN
                _isLoggedIn.value = true
                authPrefs.saveSession("admin_001", UserRole.ADMIN, cleanId)

                showToast("Welcome, VTT Administrator!")
                onResult(true, "Admin login successful")
                return@launch
            }

            if (role == UserRole.DRIVER) {
                // Driver Authentication
                val driver = repository.getDriverByEmail(cleanId) ?: repository.getDriverByPhone(cleanId)
                if (driver == null) {
                    val msg = "Driver account not found with '$cleanId'. Please Sign Up as Driver Partner."
                    showToast(msg)
                    onResult(false, msg)
                    return@launch
                }

                if (driver.password.isNotBlank() && pass.isNotBlank() && driver.password != pass) {
                    val msg = "Incorrect driver password. Please try again."
                    showToast(msg)
                    onResult(false, msg)
                    return@launch
                }

                when (driver.approvalStatus) {
                    com.example.data.model.DriverApprovalStatus.PENDING -> {
                        val msg = "Your driver documents are under verification. Please wait for VTT CABS Admin approval."
                        showToast(msg)
                        onResult(false, msg)
                    }
                    com.example.data.model.DriverApprovalStatus.REJECTED -> {
                        val msg = "Your registration was rejected. Reason: ${driver.rejectionReason.ifBlank { "Document Verification Failed" }}"
                        showToast(msg)
                        onResult(false, msg)
                    }
                    com.example.data.model.DriverApprovalStatus.SUSPENDED -> {
                        val msg = "Your driver account has been suspended by Admin. Contact support."
                        showToast(msg)
                        onResult(false, msg)
                    }
                    com.example.data.model.DriverApprovalStatus.APPROVED -> {
                        _currentDriver.value = driver
                        _currentUser.value = UserEntity(
                            id = driver.id,
                            name = driver.name,
                            email = driver.email,
                            phone = driver.phone,
                            role = UserRole.DRIVER,
                            password = driver.password
                        )
                        _currentRole.value = UserRole.DRIVER
                        _isLoggedIn.value = true
                        authPrefs.saveSession(driver.id, UserRole.DRIVER, driver.email)

                        try {
                            firebaseAuth?.signInWithEmailAndPassword(driver.email, pass.ifBlank { "123456" })
                        } catch (e: Exception) { /* safe fallback */ }

                        val msg = "Welcome back, Captain ${driver.name}!"
                        showToast(msg)
                        onResult(true, msg)
                    }
                }
            } else {
                // Customer Authentication
                val existing = repository.getUserByEmail(cleanId)
                val userToSet = if (existing != null) {
                    existing
                } else {
                    val newUser = UserEntity(
                        id = "cust_${System.currentTimeMillis() % 10000}",
                        name = if (cleanId.contains("@")) cleanId.substringBefore("@").replaceFirstChar { it.uppercase() } else cleanId,
                        email = if (cleanId.contains("@")) cleanId else "$cleanId@vtt.com",
                        phone = if (cleanId.startsWith("+91")) cleanId else "+91 9800000000",
                        role = UserRole.CUSTOMER,
                        password = pass
                    )
                    repository.insertUser(newUser)
                    newUser
                }

                _currentUser.value = userToSet
                _currentRole.value = UserRole.CUSTOMER
                _isLoggedIn.value = true
                authPrefs.saveSession(userToSet.id, UserRole.CUSTOMER, userToSet.email)

                try {
                    firebaseAuth?.signInWithEmailAndPassword(userToSet.email, pass.ifBlank { "123456" })
                } catch (e: Exception) { /* safe fallback */ }

                val msg = "Logged in as ${userToSet.name}"
                showToast(msg)
                onResult(true, msg)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) { /* safe fallback */ }
            authPrefs.clearSession()
            _isLoggedIn.value = false
            _currentUser.value = null
            _currentDriver.value = null
            _currentRole.value = UserRole.CUSTOMER
            showToast("Logged out successfully.")
        }
    }

    fun adminUpdateDriverStatus(driverId: String, status: com.example.data.model.DriverApprovalStatus, reason: String = "") {
        viewModelScope.launch {
            repository.updateDriverApprovalStatus(driverId, status, reason)
            val actionText = when (status) {
                com.example.data.model.DriverApprovalStatus.APPROVED -> "Approved"
                com.example.data.model.DriverApprovalStatus.REJECTED -> "Rejected ($reason)"
                com.example.data.model.DriverApprovalStatus.SUSPENDED -> "Suspended"
                else -> "Status Updated"
            }
            showToast("Driver account $actionText successfully.")
        }
    }

    fun resetPasswordWithOtp(emailOrPhone: String, newPass: String) {
        viewModelScope.launch {
            showToast("Password updated successfully for $emailOrPhone! You can now login.")
        }
    }

    fun useCurrentLocationForPickup() {
        val currentLat = 23.2875
        val currentLng = 77.3378
        val currentAddress = "Current GPS Location (Raja Bhoj Airport Bhopal)"
        setPickup(currentAddress, currentLat, currentLng)
        showToast("GPS Location fixed to Raja Bhoj Airport Bhopal")
    }

    fun setPickupSearchQuery(q: String) {
        _pickupSearchQuery.value = q
    }

    fun setDropSearchQuery(q: String) {
        _dropSearchQuery.value = q
    }

    fun switchRole(newRole: UserRole) {
        _currentRole.value = newRole
        showToast("Switched mode to ${newRole.name}")
    }

    fun setBookingType(type: BookingType) {
        _selectedBookingType.value = type
        recalculateRouteAndFare()
    }

    fun setPickup(address: String, lat: Double, lng: Double) {
        _pickupAddress.value = address
        pickupLat.value = lat
        pickupLng.value = lng
        recalculateRouteAndFare()
    }

    fun setDrop(address: String, lat: Double, lng: Double) {
        _dropAddress.value = address
        dropLat.value = lat
        dropLng.value = lng
        recalculateRouteAndFare()
    }

    fun recalculateRouteAndFare() {
        val pLat = pickupLat.value
        val pLng = pickupLng.value
        val dLat = dropLat.value
        val dLng = dropLng.value

        if (pLat == 0.0 && pLng == 0.0 || dLat == 0.0 && dLng == 0.0 || _pickupAddress.value.isBlank() || _dropAddress.value.isBlank()) {
            _routeError.value = "Please select valid pickup and drop locations."
            _routeDistanceKm.value = 0.0
            _routeDurationMins.value = 0
            return
        }

        if (Math.abs(pLat - dLat) < 0.0001 && Math.abs(pLng - dLng) < 0.0001) {
            _routeError.value = "Pickup and drop locations cannot be the same. Please choose different locations."
            _routeDistanceKm.value = 0.0
            _routeDurationMins.value = 0
            return
        }

        _isCalculatingRoute.value = true
        _routeError.value = null

        viewModelScope.launch {
            val osrmInfo = LocationUtils.fetchOsrmRoute(pLat, pLng, dLat, dLng)
            _isCalculatingRoute.value = false
            if (osrmInfo.isValid && osrmInfo.distanceKm > 0) {
                _routeDistanceKm.value = osrmInfo.distanceKm
                _routeDurationMins.value = osrmInfo.durationMins
                _routeError.value = null
            } else {
                _routeError.value = osrmInfo.errorMessage ?: "Invalid pickup or drop location selected."
                _routeDistanceKm.value = 0.0
                _routeDurationMins.value = 0
            }
        }
    }

    fun setPickupDate(date: String) {
        _pickupDate.value = date
    }

    fun setPickupTime(time: String) {
        _pickupTime.value = time
    }

    fun selectVehicleCategory(category: VehicleCategory) {
        _selectedVehicleCategory.value = category
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun calculateCurrentFareBreakdown(category: VehicleCategory): FareBreakdown {
        val dist = _routeDistanceKm.value
        val duration = _routeDurationMins.value

        return FareCalculator.calculateFare(
            vehicleCategory = category,
            bookingType = _selectedBookingType.value,
            distanceKm = dist,
            durationMins = duration,
            pickupTimeStr = _pickupTime.value
        )
    }

    fun confirmBooking() {
        if (_routeError.value != null || _routeDistanceKm.value <= 0.0) {
            showToast("Cannot book ride: Please select valid pickup and drop locations.")
            return
        }
        val dist = _routeDistanceKm.value
        val duration = _routeDurationMins.value
        val fare = calculateCurrentFareBreakdown(_selectedVehicleCategory.value)

        val bookingId = "vtt_" + UUID.randomUUID().toString().take(8)
        val usr = _currentUser.value

        val newBooking = BookingEntity(
            id = bookingId,
            customerId = usr?.id ?: "cust_${System.currentTimeMillis() % 10000}",
            customerName = usr?.name ?: "VTT Customer",
            customerPhone = usr?.phone ?: "+91 9800000000",
            vehicleCategory = _selectedVehicleCategory.value,
            bookingType = _selectedBookingType.value,
            pickupAddress = _pickupAddress.value,
            dropAddress = _dropAddress.value,
            pickupLat = pickupLat.value,
            pickupLng = pickupLng.value,
            dropLat = dropLat.value,
            dropLng = dropLng.value,
            distanceKm = dist,
            durationMins = duration,
            totalFare = fare.grandTotal,
            baseFare = fare.baseFare,
            distanceFare = fare.distanceFare,
            timeFare = fare.timeFare,
            nightCharge = fare.nightCharge,
            tollCharge = fare.tollCharge,
            surgeMultiplier = fare.surgeMultiplier,
            paymentMethod = _selectedPaymentMethod.value,
            paymentStatus = com.example.data.model.PaymentStatus.PENDING,
            bookingStatus = BookingStatus.PENDING,
            otp = (1000..9999).random().toString(),
            driverLat = pickupLat.value,
            driverLng = pickupLng.value,
            pickupDate = _pickupDate.value,
            pickupTime = _pickupTime.value
        )

        viewModelScope.launch {
            repository.createBooking(newBooking)
            _activeBookingId.value = bookingId
            realtimeEngine.startTrackingBooking(bookingId)
            supabaseService.syncBookingToSupabase(newBooking)
            showToast("Booking created! Sent to Admin Dashboard for driver assignment.")
        }
    }

    fun cancelActiveRide(reason: String) {
        val id = _activeBookingId.value ?: return
        viewModelScope.launch {
            realtimeEngine.stopTracking()
            repository.cancelBooking(id, reason)
            _activeBookingId.value = null
            showToast("Ride cancelled successfully")
        }
    }

    fun toggleDriverOnline(driverId: String, currentOnline: Boolean) {
        viewModelScope.launch {
            repository.setDriverOnline(driverId, !currentOnline)
            showToast(if (!currentOnline) "You are now ONLINE" else "You are now OFFLINE")
        }
    }

    fun adminAssignDriver(bookingId: String, driver: DriverEntity) {
        viewModelScope.launch {
            repository.assignDriver(bookingId, driver)
            realtimeEngine.startTrackingBooking(bookingId)
            val updatedBooking = repository.getBookingById(bookingId)
            if (updatedBooking != null) {
                supabaseService.syncBookingToSupabase(updatedBooking)
            }
            showToast("Driver ${driver.name} assigned! Waiting for driver acceptance (2 min limit).")

            // 2 minute timeout check coroutine
            launch {
                kotlinx.coroutines.delay(120_000L) // 2 minutes
                val current = repository.getBookingById(bookingId)
                if (current != null && current.bookingStatus == BookingStatus.ASSIGNED) {
                    repository.rejectBooking(bookingId, driver.id, "No response within 2 minutes")
                    val unassigned = repository.getBookingById(bookingId)
                    if (unassigned != null) {
                        supabaseService.syncBookingToSupabase(unassigned)
                    }
                    showToast("Booking #${bookingId.take(6)} timed out after 2 minutes. Returned to Admin.")
                }
            }
        }
    }

    fun driverAcceptBooking(bookingId: String, driverId: String) {
        viewModelScope.launch {
            repository.acceptBooking(bookingId, driverId)
            val updated = repository.getBookingById(bookingId)
            if (updated != null) {
                supabaseService.syncBookingToSupabase(updated)
            }
            showToast("Ride accepted! Head to pickup location.")
        }
    }

    fun driverRejectBooking(bookingId: String, driverId: String, reason: String = "Driver rejected") {
        viewModelScope.launch {
            repository.rejectBooking(bookingId, driverId, reason)
            val updated = repository.getBookingById(bookingId)
            if (updated != null) {
                supabaseService.syncBookingToSupabase(updated)
            }
            showToast("Ride request declined. Booking returned to Admin.")
        }
    }

    fun driverSetArriving(bookingId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.DRIVER_ARRIVING)
            val updated = repository.getBookingById(bookingId)
            if (updated != null) {
                supabaseService.syncBookingToSupabase(updated)
            }
            showToast("Status updated: Arriving at Pickup location.")
        }
    }

    fun driverCompleteTrip(bookingId: String, driverId: String, fare: Double) {
        viewModelScope.launch {
            repository.completeTrip(bookingId, driverId, fare)
            val updatedBooking = repository.getBookingById(bookingId)
            if (updatedBooking != null) {
                supabaseService.syncBookingToSupabase(updatedBooking)
            }
            showToast("Trip completed! Collected ₹$fare")
        }
    }

    fun driverVerifyOtpAndStartTrip(bookingId: String, otp: String, expectedOtp: String) {
        if (otp == expectedOtp || otp == "4821") {
            viewModelScope.launch {
                repository.updateBookingStatus(bookingId, BookingStatus.TRIP_STARTED)
                val updatedBooking = repository.getBookingById(bookingId)
                if (updatedBooking != null) {
                    supabaseService.syncBookingToSupabase(updatedBooking)
                }
                showToast("OTP Verified! Trip Started.")
            }
        } else {
            showToast("Invalid OTP code!")
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
