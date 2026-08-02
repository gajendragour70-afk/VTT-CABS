package com.vttcabs.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vttcabs.common.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoggedIn: Boolean = false,
    val currentAdmin: UserEntity? = null,
    val bookings: List<BookingEntity> = emptyList(),
    val drivers: List<DriverEntity> = emptyList(),
    val customers: List<UserEntity> = emptyList(),
    val pendingBookings: List<BookingEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalRevenue: Double = 0.0,
    val todayTrips: Int = 0,
    val onlineDrivers: Int = 0
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    
    private val repository = VttRepository(application.applicationContext)
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val prefs = AuthPreferences(getApplication())
            if (prefs.isLoggedIn() && prefs.getUserRole() == UserRole.ADMIN) {
                val adminId = prefs.getUserId()
                val admin = if (adminId != null) repository.getUserById(adminId) else null
                _uiState.value = _uiState.value.copy(isLoggedIn = true, currentAdmin = admin)
                if (admin != null) loadDashboard()
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val admin = repository.loginAdmin(email, password)
                if (admin != null) {
                    val prefs = AuthPreferences(getApplication())
                    prefs.saveSession(admin.id, UserRole.ADMIN, admin.email)
                    _uiState.value = _uiState.value.copy(isLoggedIn = true, currentAdmin = admin, isLoading = false)
                    loadDashboard()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Invalid admin credentials")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            val prefs = AuthPreferences(getApplication())
            prefs.clearSession()
            _uiState.value = AdminUiState()
        }
    }
    
    private fun loadDashboard() {
        viewModelScope.launch {
            repository.allBookings.collect { bookings ->
                val pending = bookings.filter { it.bookingStatus == BookingStatus.PENDING }
                val completed = bookings.filter { it.bookingStatus == BookingStatus.COMPLETED }
                val today = completed.count { it.createdAt > System.currentTimeMillis() - 86400000 }
                
                _uiState.value = _uiState.value.copy(
                    bookings = bookings.sortedByDescending { it.createdAt },
                    pendingBookings = pending,
                    totalRevenue = completed.sumOf { it.totalFare },
                    todayTrips = today
                )
            }
        }
        
        viewModelScope.launch {
            repository.allDrivers.collect { drivers ->
                _uiState.value = _uiState.value.copy(
                    drivers = drivers,
                    onlineDrivers = drivers.filter { it.isOnline }.size
                )
            }
        }
    }
    
    fun approveBooking(bookingId: String, driverId: String) {
        viewModelScope.launch {
            repository.assignDriverToBooking(bookingId, driverId)
            loadDashboard()
        }
    }
    
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId, "Cancelled by admin", "admin")
            loadDashboard()
        }
    }
}
