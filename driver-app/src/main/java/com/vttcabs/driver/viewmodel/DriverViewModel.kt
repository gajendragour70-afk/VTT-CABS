package com.vttcabs.driver.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vttcabs.common.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverUiState(
    val isLoggedIn: Boolean = false,
    val currentDriver: DriverEntity? = null,
    val currentRegistration: DriverRegistrationEntity? = null,
    val isOnline: Boolean = false,
    val isApproved: Boolean = false,
    val isRegistrationPending: Boolean = false,
    val bookings: List<BookingEntity> = emptyList(),
    val currentBooking: BookingEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val earnings: Double = 0.0,
    val todayTrips: Int = 0
)

class DriverViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()
    
    private val repository = VttRepository(application.applicationContext)
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val prefs = AuthPreferences(getApplication())
            if (prefs.isLoggedIn() && prefs.getUserRole() == UserRole.DRIVER) {
                val driverId = prefs.getUserId()
                if (driverId != null) {
                    val driver = repository.getDriverById(driverId)
                    if (driver != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            currentDriver = driver,
                            isOnline = driver.isOnline,
                            isApproved = driver.approvalStatus == DriverApprovalStatus.APPROVED
                        )
                        loadBookings()
                    }
                }
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val driver = repository.loginDriver(email, password)
                if (driver != null) {
                    val prefs = AuthPreferences(getApplication())
                    prefs.saveSession(driver.id, UserRole.DRIVER, driver.email)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentDriver = driver,
                        isOnline = driver.isOnline,
                        isApproved = driver.approvalStatus == DriverApprovalStatus.APPROVED,
                        isLoading = false
                    )
                    loadBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Invalid credentials or account not approved"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }
    
    fun loginWithOtp(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val driver = repository.loginDriverWithOtp(email)
                if (driver != null) {
                    val prefs = AuthPreferences(getApplication())
                    prefs.saveSession(driver.id, UserRole.DRIVER, driver.email)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentDriver = driver,
                        isOnline = driver.isOnline,
                        isApproved = driver.approvalStatus == DriverApprovalStatus.APPROVED,
                        isLoading = false
                    )
                    loadBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Driver not found or not approved"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }
    
    fun toggleOnline() {
        viewModelScope.launch {
            val driver = _uiState.value.currentDriver ?: return@launch
            
            if (driver.approvalStatus != DriverApprovalStatus.APPROVED) {
                _uiState.value = _uiState.value.copy(
                    error = "Your account must be approved before going online"
                )
                return@launch
            }
            
            val newStatus = !_uiState.value.isOnline
            repository.setDriverOnline(driver.id, newStatus)
            _uiState.value = _uiState.value.copy(isOnline = newStatus)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            val prefs = AuthPreferences(getApplication())
            prefs.clearSession()
            _uiState.value = DriverUiState()
        }
    }
    
    private fun loadBookings() {
        viewModelScope.launch {
            val driverId = _uiState.value.currentDriver?.id ?: return@launch
            repository.getDriverBookings(driverId).collect { bookings ->
                _uiState.value = _uiState.value.copy(
                    bookings = bookings,
                    earnings = bookings.filter { it.bookingStatus == BookingStatus.COMPLETED }
                        .sumOf { it.totalFare },
                    todayTrips = bookings.count { 
                        it.bookingStatus == BookingStatus.COMPLETED && 
                        it.createdAt > System.currentTimeMillis() - 86400000 
                    }
                )
            }
        }
    }
    
    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            val booking = repository.getBookingById(bookingId)
            _uiState.value = _uiState.value.copy(currentBooking = booking)
        }
    }
    
    fun acceptBooking(bookingId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.ACCEPTED)
            loadBooking(bookingId)
        }
    }
    
    fun startTrip(bookingId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.TRIP_STARTED)
            loadBooking(bookingId)
        }
    }
    
    fun completeTrip(bookingId: String) {
        viewModelScope.launch {
            val driver = _uiState.value.currentDriver ?: return@launch
            val booking = repository.getBookingById(bookingId) ?: return@launch
            repository.completeTrip(bookingId, driver.id, booking.totalFare)
            _uiState.value = _uiState.value.copy(currentBooking = null)
            loadBookings()
        }
    }
    
    fun updateLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val driver = _uiState.value.currentDriver ?: return@launch
            repository.updateDriverLocation(driver.id, lat, lng)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
