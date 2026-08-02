package com.vttcabs.customer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vttcabs.common.FareCalculator
import com.vttcabs.common.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CustomerUiState(
    val isLoggedIn: Boolean = false,
    val currentUser: UserEntity? = null,
    val bookings: List<BookingEntity> = emptyList(),
    val currentBooking: BookingEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val pickupLocation: PopularPlace? = null,
    val dropLocation: PopularPlace? = null,
    val selectedVehicleCategory: VehicleCategory = VehicleCategory.SEDAN,
    val selectedBookingType: BookingType = BookingType.ONE_WAY,
    val fareBreakdown: FareBreakdown? = null,
    val estimatedFare: Double = 0.0,
    val distanceKm: Double = 0.0,
    val durationMins: Int = 0
)

class CustomerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()
    
    private val repository = VttRepository(application.applicationContext)
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val prefs = AuthPreferences(getApplication())
            if (prefs.isLoggedIn() && prefs.getUserRole() == UserRole.CUSTOMER) {
                val userId = prefs.getUserId() ?: return@launch
                val user = repository.getUserById(userId)
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    currentUser = user
                )
                loadBookings()
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = repository.loginCustomer(email, password)
                if (user != null) {
                    val prefs = AuthPreferences(getApplication())
                    prefs.saveSession(user.id, user.role, user.email)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentUser = user,
                        isLoading = false
                    )
                    loadBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Invalid email or password"
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
    
    fun signup(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = repository.registerCustomer(name, email, phone, password)
                if (user != null) {
                    val prefs = AuthPreferences(getApplication())
                    prefs.saveSession(user.id, user.role, user.email)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentUser = user,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Registration failed. Email may already exist."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Registration failed"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            val prefs = AuthPreferences(getApplication())
            prefs.clearSession()
            _uiState.value = CustomerUiState()
        }
    }
    
    private fun loadBookings() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: return@launch
            val bookings = repository.getBookingsByCustomer(userId)
            _uiState.value = _uiState.value.copy(bookings = bookings)
        }
    }
    
    fun setPickupLocation(place: PopularPlace) {
        _uiState.value = _uiState.value.copy(pickupLocation = place)
        calculateFare()
    }
    
    fun setDropLocation(place: PopularPlace) {
        _uiState.value = _uiState.value.copy(dropLocation = place)
        calculateFare()
    }
    
    fun setVehicleCategory(category: VehicleCategory) {
        _uiState.value = _uiState.value.copy(selectedVehicleCategory = category)
        calculateFare()
    }
    
    fun setBookingType(type: BookingType) {
        _uiState.value = _uiState.value.copy(selectedBookingType = type)
        calculateFare()
    }
    
    private fun calculateFare() {
        val pickup = _uiState.value.pickupLocation
        val drop = _uiState.value.dropLocation
        
        if (pickup != null && drop != null) {
            val distance = LocationUtils.calculateDistance(
                pickup.lat, pickup.lng, drop.lat, drop.lng
            )
            val duration = (distance / 30 * 60).toInt()
            
            val breakdown = FareCalculator.calculateFare(
                vehicleCategory = _uiState.value.selectedVehicleCategory,
                bookingType = _uiState.value.selectedBookingType,
                distanceKm = distance,
                durationMins = duration
            )
            
            _uiState.value = _uiState.value.copy(
                distanceKm = distance,
                durationMins = duration,
                estimatedFare = breakdown.grandTotal,
                fareBreakdown = breakdown
            )
        }
    }
    
    fun createBooking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = _uiState.value
                val pickup = state.pickupLocation ?: return@launch
                val drop = state.dropLocation ?: return@launch
                val user = state.currentUser ?: return@launch
                
                val booking = BookingEntity(
                    id = UUID.randomUUID().toString(),
                    customerId = user.id,
                    customerName = user.name,
                    customerPhone = user.phone,
                    vehicleCategory = state.selectedVehicleCategory,
                    bookingType = state.selectedBookingType,
                    pickupAddress = pickup.address,
                    dropAddress = drop.address,
                    pickupLat = pickup.lat,
                    pickupLng = pickup.lng,
                    dropLat = drop.lat,
                    dropLng = drop.lng,
                    distanceKm = state.distanceKm,
                    durationMins = state.durationMins,
                    totalFare = state.estimatedFare,
                    baseFare = state.fareBreakdown?.baseFare ?: 100.0,
                    distanceFare = state.fareBreakdown?.distanceFare ?: 0.0,
                    bookingStatus = BookingStatus.PENDING,
                    createdAt = System.currentTimeMillis()
                )
                
                repository.saveBooking(booking)
                _uiState.value = _uiState.value.copy(
                    currentBooking = booking,
                    isLoading = false
                )
                loadBookings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
