package com.vttcabs.admin.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vttcabs.admin.VttAdminApp
import com.vttcabs.admin.data.model.*
import com.vttcabs.admin.data.remote.FirestoreService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "AdminViewModel"
    }
    
    private val firebaseAuth = VttAdminApp.instance.firebaseAuth
    private val firestoreService = FirestoreService()
    
    // Initialization state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _initError = MutableStateFlow<String?>(null)
    val initError: StateFlow<String?> = _initError.asStateFlow()
    
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
        Log.d(TAG, "AdminViewModel initialized")
        initializeApp()
    }
    
    private fun initializeApp() {
        viewModelScope.launch {
            _isLoading.value = true
            _initError.value = null
            Log.d(TAG, "Starting initialization...")
            
            try {
                // Small delay to show loading screen
                delay(500)
                
                // Check Firebase connection
                Log.d(TAG, "Checking Firebase connection...")
                val currentUser = firebaseAuth.currentUser
                Log.d(TAG, "Firebase connected. Current user: ${currentUser?.uid}")
                
                _isLoading.value = false
                Log.d(TAG, "Initialization complete")
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed: ${e.message}", e)
                _initError.value = "Failed to connect to Firebase: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun retryInit() {
        _initError.value = null
        initializeApp()
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            _isLoading.value = true
            Log.d(TAG, "Attempting Firebase login with email: $email")
            
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                Log.d(TAG, "Firebase login result: ${result.user?.uid}")
                
                if (result.user != null) {
                    // Verify admin role from Firestore
                    Log.d(TAG, "Verifying admin role...")
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
                        Log.d(TAG, "Admin login successful")
                    } else {
                        firebaseAuth.signOut()
                        _authError.value = "Access denied. Admin privileges required."
                        Log.w(TAG, "Admin access denied - no admin document found")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login failed: ${e.message}", e)
                _authError.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loginWithHardcodedCredentials(phone: String, password: String) {
        Log.d(TAG, "Attempting hardcoded login for phone: $phone")
        
        // Hardcoded admin credentials for backup access
        if (phone == "9999999999" && password == "Admin@123") {
            viewModelScope.launch {
                _authError.value = null
                _isLoading.value = true
                
                try {
                    // Sign in anonymously to get Firebase auth
                    val result = firebaseAuth.signInAnonymously().await()
                    Log.d(TAG, "Anonymous sign-in result: ${result.user?.uid}")
                    
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
                        Log.d(TAG, "Hardcoded admin login successful")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Hardcoded login failed: ${e.message}", e)
                    _authError.value = "Login failed: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _authError.value = "Invalid admin credentials."
            Log.w(TAG, "Invalid credentials provided")
        }
    }
    
    fun logout() {
        Log.d(TAG, "Logging out...")
        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _currentAdmin.value = null
        _currentScreen.value = "login"
        _drivers.value = emptyList()
        _customers.value = emptyList()
        _bookings.value = emptyList()
        _dashboardStats.value = DashboardStats()
        Log.d(TAG, "Logout complete")
    }
    
    fun navigateTo(screen: String) {
        Log.d(TAG, "Navigating to: $screen")
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
        Log.d(TAG, "Toast: $message")
        _toastMessage.value = message
    }
    
    fun clearToast() {
        _toastMessage.value = null
    }
    
    // ========== DASHBOARD ==========
    
    fun loadDashboardStats() {
        viewModelScope.launch {
            Log.d(TAG, "Loading dashboard stats...")
            try {
                val stats = firestoreService.getDashboardStats()
                _dashboardStats.value = stats
                Log.d(TAG, "Dashboard stats loaded: $stats")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard: ${e.message}", e)
                // Set default values on error
                _dashboardStats.value = DashboardStats()
                showToast("Error loading dashboard: ${e.message}")
            }
        }
    }
    
    // ========== DRIVERS ==========
    
    fun loadDrivers() {
        viewModelScope.launch {
            _isLoadingDrivers.value = true
            Log.d(TAG, "Loading drivers...")
            try {
                firestoreService.getAllDrivers().collect { driverList ->
                    _drivers.value = driverList
                    _isLoadingDrivers.value = false
                    Log.d(TAG, "Loaded ${driverList.size} drivers")
                }
            } catch (e: Exception) {
                _isLoadingDrivers.value = false
                _drivers.value = emptyList()
                Log.e(TAG, "Error loading drivers: ${e.message}", e)
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
            Log.d(TAG, "Loading customers...")
            try {
                firestoreService.getAllCustomers().collect { customerList ->
                    _customers.value = customerList
                    _isLoadingCustomers.value = false
                    Log.d(TAG, "Loaded ${customerList.size} customers")
                }
            } catch (e: Exception) {
                _isLoadingCustomers.value = false
                _customers.value = emptyList()
                Log.e(TAG, "Error loading customers: ${e.message}", e)
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
            Log.d(TAG, "Loading bookings...")
            try {
                firestoreService.getAllBookings().collect { bookingList ->
                    _bookings.value = bookingList
                    _isLoadingBookings.value = false
                    Log.d(TAG, "Loaded ${bookingList.size} bookings")
                }
            } catch (e: Exception) {
                _isLoadingBookings.value = false
                _bookings.value = emptyList()
                Log.e(TAG, "Error loading bookings: ${e.message}", e)
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
                _vehicles.value = emptyList()
                _fareRules.value = emptyList()
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
