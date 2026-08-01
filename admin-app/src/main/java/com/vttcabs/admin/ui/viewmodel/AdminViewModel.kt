package com.vttcabs.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vttcabs.admin.VttAdminApp
import com.vttcabs.admin.data.model.*
import com.vttcabs.admin.data.remote.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    
    private val firebaseAuth = VttAdminApp.instance.firebaseAuth
    private val firestoreService = FirestoreService()
    
    // Auth state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentAdmin = MutableStateFlow<AdminUser?>(null)
    val currentAdmin: StateFlow<AdminUser?> = _currentAdmin.asStateFlow()
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    
    // Dashboard state
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()
    
    // Drivers state
    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()
    
    private val _selectedDriver = MutableStateFlow<Driver?>(null)
    val selectedDriver: StateFlow<Driver?> = _selectedDriver.asStateFlow()
    
    private val _isLoadingDrivers = MutableStateFlow(false)
    val isLoadingDrivers: StateFlow<Boolean> = _isLoadingDrivers.asStateFlow()
    
    // Customers state
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()
    
    private val _isLoadingCustomers = MutableStateFlow(false)
    val isLoadingCustomers: StateFlow<Boolean> = _isLoadingCustomers.asStateFlow()
    
    // Bookings state
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    
    private val _isLoadingBookings = MutableStateFlow(false)
    val isLoadingBookings: StateFlow<Boolean> = _isLoadingBookings.asStateFlow()
    
    // Vehicles state
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()
    
    private val _fareRules = MutableStateFlow<List<FareRule>>(emptyList())
    val fareRules: StateFlow<List<FareRule>> = _fareRules.asStateFlow()
    
    private val _isLoadingVehicles = MutableStateFlow(false)
    val isLoadingVehicles: StateFlow<Boolean> = _isLoadingVehicles.asStateFlow()
    
    // Toast messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    // Navigation
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _isLoggedIn.value = true
            _currentAdmin.value = AdminUser(
                id = currentUser.uid,
                email = currentUser.email ?: ""
            )
            _currentScreen.value = "dashboard"
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                if (result.user != null) {
                    // Verify admin role from Firestore
                    val adminDoc = VttAdminApp.instance.firestore
                        .collection("admins")
                        .document(result.user!!.uid)
                        .get()
                        .await()
                    
                    if (adminDoc.exists()) {
                        _isLoggedIn.value = true
                        _currentAdmin.value = AdminUser(
                            id = result.user!!.uid,
                            email = result.user!!.email ?: "",
                            name = adminDoc.getString("name") ?: "",
                            phone = adminDoc.getString("phone") ?: ""
                        )
                        _currentScreen.value = "dashboard"
                        loadDashboardStats()
                    } else {
                        firebaseAuth.signOut()
                        _authError.value = "Access denied. Admin privileges required."
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Login failed: ${e.message}"
            }
        }
    }
    
    fun loginWithHardcodedCredentials(phone: String, password: String) {
        // Hardcoded admin credentials for backup access
        if (phone == "9999999999" && password == "Admin@123") {
            viewModelScope.launch {
                try {
                    val result = firebaseAuth.signInAnonymously().await()
                    if (result.user != null) {
                        _isLoggedIn.value = true
                        _currentAdmin.value = AdminUser(
                            id = "hardcoded_admin",
                            email = "admin@vtt.com",
                            name = "VTT Admin",
                            phone = phone
                        )
                        _currentScreen.value = "dashboard"
                        loadDashboardStats()
                    }
                } catch (e: Exception) {
                    _authError.value = "Login failed: ${e.message}"
                }
            }
        } else {
            _authError.value = "Invalid admin credentials."
        }
    }
    
    fun logout() {
        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _currentAdmin.value = null
        _currentScreen.value = "login"
    }
    
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        when (screen) {
            "dashboard" -> loadDashboardStats()
            "drivers" -> loadDrivers()
            "customers" -> loadCustomers()
            "bookings" -> loadBookings()
            "vehicles" -> loadVehicles()
        }
    }
    
    fun showToast(message: String) {
        _toastMessage.value = message
    }
    
    fun clearToast() {
        _toastMessage.value = null
    }
    
    // ========== DASHBOARD ==========
    
    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                val stats = firestoreService.getDashboardStats()
                _dashboardStats.value = stats
            } catch (e: Exception) {
                showToast("Error loading dashboard: ${e.message}")
            }
        }
    }
    
    // ========== DRIVERS ==========
    
    fun loadDrivers() {
        viewModelScope.launch {
            _isLoadingDrivers.value = true
            try {
                firestoreService.getAllDrivers().collect { driverList ->
                    _drivers.value = driverList
                    _isLoadingDrivers.value = false
                }
            } catch (e: Exception) {
                _isLoadingDrivers.value = false
                showToast("Error loading drivers: ${e.message}")
            }
        }
    }
    
    fun selectDriver(driver: Driver) {
        _selectedDriver.value = driver
    }
    
    fun clearSelectedDriver() {
        _selectedDriver.value = null
    }
    
    fun approveDriver(driverId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateDriverStatus(driverId, "APPROVED", true)
                showToast("Driver approved successfully")
                loadDrivers()
            } catch (e: Exception) {
                showToast("Error approving driver: ${e.message}")
            }
        }
    }
    
    fun rejectDriver(driverId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateDriverStatus(driverId, "REJECTED", false)
                showToast("Driver rejected")
                loadDrivers()
            } catch (e: Exception) {
                showToast("Error rejecting driver: ${e.message}")
            }
        }
    }
    
    fun suspendDriver(driverId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateDriverStatus(driverId, "SUSPENDED", false)
                firestoreService.updateDriverOnlineStatus(driverId, false)
                showToast("Driver suspended")
                loadDrivers()
            } catch (e: Exception) {
                showToast("Error suspending driver: ${e.message}")
            }
        }
    }
    
    fun activateDriver(driverId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateDriverStatus(driverId, "APPROVED", true)
                showToast("Driver activated")
                loadDrivers()
            } catch (e: Exception) {
                showToast("Error activating driver: ${e.message}")
            }
        }
    }
    
    // ========== CUSTOMERS ==========
    
    fun loadCustomers() {
        viewModelScope.launch {
            _isLoadingCustomers.value = true
            try {
                firestoreService.getAllCustomers().collect { customerList ->
                    _customers.value = customerList
                    _isLoadingCustomers.value = false
                }
            } catch (e: Exception) {
                _isLoadingCustomers.value = false
                showToast("Error loading customers: ${e.message}")
            }
        }
    }
    
    fun blockCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateCustomerBlockedStatus(customerId, true)
                showToast("Customer blocked")
                loadCustomers()
            } catch (e: Exception) {
                showToast("Error blocking customer: ${e.message}")
            }
        }
    }
    
    fun unblockCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateCustomerBlockedStatus(customerId, false)
                showToast("Customer unblocked")
                loadCustomers()
            } catch (e: Exception) {
                showToast("Error unblocking customer: ${e.message}")
            }
        }
    }
    
    // ========== BOOKINGS ==========
    
    fun loadBookings() {
        viewModelScope.launch {
            _isLoadingBookings.value = true
            try {
                firestoreService.getAllBookings().collect { bookingList ->
                    _bookings.value = bookingList
                    _isLoadingBookings.value = false
                }
            } catch (e: Exception) {
                _isLoadingBookings.value = false
                showToast("Error loading bookings: ${e.message}")
            }
        }
    }
    
    fun assignDriverToBooking(bookingId: String, driver: Driver) {
        viewModelScope.launch {
            try {
                firestoreService.assignDriverToBooking(
                    bookingId = bookingId,
                    driverId = driver.id,
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    vehicleNumber = driver.vehicleNumber,
                    vehicleModel = driver.vehicleModel
                )
                showToast("Driver assigned successfully")
                loadBookings()
            } catch (e: Exception) {
                showToast("Error assigning driver: ${e.message}")
            }
        }
    }
    
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateBookingStatus(bookingId, "CANCELLED")
                showToast("Booking cancelled")
                loadBookings()
            } catch (e: Exception) {
                showToast("Error cancelling booking: ${e.message}")
            }
        }
    }
    
    fun completeBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateBookingStatus(bookingId, "COMPLETED")
                showToast("Booking marked as completed")
                loadBookings()
            } catch (e: Exception) {
                showToast("Error completing booking: ${e.message}")
            }
        }
    }
    
    // ========== VEHICLES ==========
    
    fun loadVehicles() {
        viewModelScope.launch {
            _isLoadingVehicles.value = true
            try {
                firestoreService.getAllVehicles().collect { vehicleList ->
                    _vehicles.value = vehicleList
                    _isLoadingVehicles.value = false
                }
                firestoreService.getAllFareRules().collect { ruleList ->
                    _fareRules.value = ruleList
                }
            } catch (e: Exception) {
                _isLoadingVehicles.value = false
                showToast("Error loading vehicles: ${e.message}")
            }
        }
    }
    
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                firestoreService.addVehicle(vehicle)
                showToast("Vehicle added successfully")
                loadVehicles()
            } catch (e: Exception) {
                showToast("Error adding vehicle: ${e.message}")
            }
        }
    }
    
    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                firestoreService.updateVehicle(vehicle)
                showToast("Vehicle updated successfully")
                loadVehicles()
            } catch (e: Exception) {
                showToast("Error updating vehicle: ${e.message}")
            }
        }
    }
    
    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                firestoreService.deleteVehicle(vehicleId)
                showToast("Vehicle deleted")
                loadVehicles()
            } catch (e: Exception) {
                showToast("Error deleting vehicle: ${e.message}")
            }
        }
    }
    
    fun updateFareRule(rule: FareRule) {
        viewModelScope.launch {
            try {
                firestoreService.updateFareRule(rule)
                showToast("Fare rule updated")
                loadVehicles()
            } catch (e: Exception) {
                showToast("Error updating fare rule: ${e.message}")
            }
        }
    }
    
    // ========== NOTIFICATIONS ==========
    
    fun sendNotificationToUser(userId: String, title: String, message: String) {
        viewModelScope.launch {
            try {
                firestoreService.sendNotification(userId, title, message)
                showToast("Notification sent")
            } catch (e: Exception) {
                showToast("Error sending notification: ${e.message}")
            }
        }
    }
    
    fun sendBroadcastNotification(title: String, message: String) {
        viewModelScope.launch {
            try {
                firestoreService.sendBroadcastNotification(title, message)
                showToast("Broadcast sent to all users")
            } catch (e: Exception) {
                showToast("Error sending broadcast: ${e.message}")
            }
        }
    }
}
