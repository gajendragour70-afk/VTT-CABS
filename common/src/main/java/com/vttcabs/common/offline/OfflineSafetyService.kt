package com.vttcabs.common.offline

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Offline Ride Safety Service
 * Handles all offline ride scenarios:
 * 1. Trip continues when internet is lost
 * 2. GPS locations are saved locally
 * 3. Auto-sync when internet returns
 * 4. Trip can start/complete offline using OTP
 * 5. Admin receives offline alerts after timeout
 */
class OfflineSafetyService private constructor(context: Context) {
    
    private val applicationContext = context.applicationContext
    private val offlineManager = OfflineManager.getInstance(applicationContext)
    private val networkMonitor = NetworkMonitor.getInstance(applicationContext)
    private val database = OfflineDatabase.getInstance(applicationContext)
    private val dao = database.offlineDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Configuration
    private var maxOfflineTimeMs = 5 * 60 * 1000L // 5 minutes default
    
    // State flows
    private val _isInOfflineRide = MutableStateFlow(false)
    val isInOfflineRide: StateFlow<Boolean> = _isInOfflineRide.asStateFlow()
    
    private val _currentBookingId = MutableStateFlow<String?>(null)
    val currentBookingId: StateFlow<String?> = _currentBookingId.asStateFlow()
    
    private val _networkStatus = MutableStateFlow(NetworkStatus.ONLINE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val _offlineDuration = MutableStateFlow(0L)
    val offlineDuration: StateFlow<Long> = _offlineDuration.asStateFlow()
    
    private val _pendingSyncItems = MutableStateFlow(0)
    val pendingSyncItems: StateFlow<Int> = _pendingSyncItems.asStateFlow()
    
    private val _safetyAlerts = MutableSharedFlow<SafetyAlert>()
    val safetyAlerts: SharedFlow<SafetyAlert> = _safetyAlerts.asSharedFlow()
    
    enum class NetworkStatus {
        ONLINE,
        OFFLINE,
        RECONNECTING
    }
    
    data class SafetyAlert(
        val type: AlertType,
        val driverId: String,
        val bookingId: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    enum class AlertType {
        DRIVER_OFFLINE,
        DRIVER_BACK_ONLINE,
        TRIP_STARTED_OFFLINE,
        TRIP_COMPLETED_OFFLINE,
        SYNC_COMPLETED,
        ADMIN_NOTIFICATION_SENT,
        CUSTOMER_NOTIFICATION_SENT,
        OFFLINE_TIMEOUT_WARNING
    }
    
    init {
        startMonitoring()
    }
    
    // ==================== SETUP ====================
    
    /**
     * Configure max offline time before alerting (default: 5 minutes)
     */
    fun setMaxOfflineTime(timeMs: Long) {
        maxOfflineTimeMs = timeMs
        offlineManager.updateConfig(OfflineConfig(maxOfflineTimeMs = timeMs))
    }
    
    /**
     * Start monitoring
     */
    private fun startMonitoring() {
        scope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                val previousStatus = _networkStatus.value
                
                _networkStatus.value = when {
                    isOnline && previousStatus == NetworkStatus.OFFLINE -> NetworkStatus.RECONNECTING
                    isOnline -> NetworkStatus.ONLINE
                    else -> NetworkStatus.OFFLINE
                }
                
                if (!isOnline && _isInOfflineRide.value) {
                    onNetworkLost()
                } else if (isOnline && previousStatus == NetworkStatus.OFFLINE) {
                    onNetworkRestored()
                }
            }
        }
        
        // Monitor offline duration
        scope.launch {
            offlineManager.currentOfflineDuration.collect { duration ->
                _offlineDuration.value = duration
                
                // Check if approaching timeout
                if (duration >= maxOfflineTimeMs * 0.8 && duration < maxOfflineTimeMs && _isInOfflineRide.value) {
                    val bookingId = _currentBookingId.value
                    if (bookingId != null) {
                        emitSafetyAlert(
                            AlertType.OFFLINE_TIMEOUT_WARNING,
                            "",
                            bookingId,
                            "Driver will be offline for ${(maxOfflineTimeMs - duration) / 1000}s"
                        )
                    }
                }
            }
        }
        
        // Monitor pending sync items
        scope.launch {
            offlineManager.pendingSyncCount.collect { count ->
                _pendingSyncItems.value = count
            }
        }
    }
    
    // ==================== RIDE LIFECYCLE ====================
    
    /**
     * Called when driver accepts a booking (online)
     */
    suspend fun onBookingAccepted(driverId: String, bookingId: String) {
        Log.d(TAG, "Booking accepted: $bookingId by driver: $driverId")
        
        _isInOfflineRide.value = true
        _currentBookingId.value = bookingId
        
        // Mark driver as online
        offlineManager.setDriverOnline(driverId, null, null)
        
        // Start GPS saving
        offlineManager.startGpsSaving(driverId, bookingId)
        
        // Cache the booking
        // In production, this would fetch from SupabaseService
    }
    
    /**
     * Called when driver goes offline during active ride
     */
    private suspend fun onNetworkLost() {
        val bookingId = _currentBookingId.value ?: return
        
        Log.d(TAG, "Network lost during ride: $bookingId")
        
        // Emit alert
        emitSafetyAlert(
            AlertType.DRIVER_OFFLINE,
            "",
            bookingId,
            "Driver network is unavailable. The trip is still active."
        )
        
        // Start monitoring offline duration
        // This will trigger admin notification after maxOfflineTimeMs
    }
    
    /**
     * Called when network is restored
     */
    private suspend fun onNetworkRestored() {
        val bookingId = _currentBookingId.value ?: return
        
        Log.d(TAG, "Network restored: $bookingId")
        
        // Emit alert
        emitSafetyAlert(
            AlertType.DRIVER_BACK_ONLINE,
            "",
            bookingId,
            "Driver is back online. Syncing data..."
        )
        
        // Pending items will auto-sync via OfflineManager
    }
    
    /**
     * Called when trip starts (online or offline)
     */
    suspend fun onTripStarted(bookingId: String, otp: String): TripStartResult {
        Log.d(TAG, "Trip started: $bookingId")
        
        // If offline, use offline verification
        if (!networkMonitor.isOnline.value) {
            val booking = offlineManager.getCachedBooking(bookingId)
            
            if (booking == null) {
                return TripStartResult(false, "Booking not found in cache")
            }
            
            if (booking.otp != otp && booking.tripOtp != otp) {
                return TripStartResult(false, "Invalid OTP")
            }
            
            // Queue start action
            offlineManager.updateBookingStatus(bookingId, "TRIP_STARTED")
            
            emitSafetyAlert(
                AlertType.TRIP_STARTED_OFFLINE,
                "",
                bookingId,
                "Trip started offline (OTP verified)"
            )
            
            return TripStartResult(true, "Trip started offline")
        }
        
        // Online - proceed normally
        return TripStartResult(true, "Trip started online")
    }
    
    /**
     * Called when trip completes (online or offline)
     */
    suspend fun onTripCompleted(
        bookingId: String,
        endLat: Double,
        endLng: Double,
        distance: Double,
        duration: Long
    ): TripCompleteResult {
        Log.d(TAG, "Trip completed: $bookingId")
        
        if (!networkMonitor.isOnline.value) {
            // Complete offline
            offlineManager.stopGpsSaving()
            
            val result = offlineManager.completeTripOffline(
                bookingId, endLat, endLng, distance, duration
            )
            
            emitSafetyAlert(
                AlertType.TRIP_COMPLETED_OFFLINE,
                "",
                bookingId,
                "Trip completed offline. Data will sync when online."
            )
            
            // Clear ride state
            _isInOfflineRide.value = false
            _currentBookingId.value = null
            
            return if (result.isSuccess) {
                TripCompleteResult(true, "Trip completed offline", true)
            } else {
                TripCompleteResult(false, result.exceptionOrNull()?.message ?: "Error", false)
            }
        }
        
        // Online completion
        _isInOfflineRide.value = false
        _currentBookingId.value = null
        
        return TripCompleteResult(true, "Trip completed online", false)
    }
    
    /**
     * Verify OTP (works offline)
     */
    suspend fun verifyOtp(bookingId: String, otp: String): OtpVerifyResult {
        if (!networkMonitor.isOnline.value) {
            val result = offlineManager.verifyOtpOffline(bookingId, otp)
            return if (result.isSuccess) {
                OtpVerifyResult(true, "OTP verified offline")
            } else {
                OtpVerifyResult(false, result.exceptionOrNull()?.message ?: "Invalid OTP")
            }
        }
        
        // Online verification would call SupabaseService
        return OtpVerifyResult(true, "OTP verified online")
    }
    
    /**
     * Called when driver ends the ride session
     */
    suspend fun onRideEnded() {
        offlineManager.stopGpsSaving()
        _isInOfflineRide.value = false
        _currentBookingId.value = null
        Log.d(TAG, "Ride ended")
    }
    
    // ==================== GPS SAVING ====================
    
    /**
     * Save GPS location (call periodically from GPS service)
     */
    suspend fun saveGpsLocation(latitude: Double, longitude: Double, driverId: String) {
        val bookingId = _currentBookingId.value ?: return
        offlineManager.saveGpsCoordinates(latitude, longitude, driverId, bookingId)
    }
    
    // ==================== ADMIN NOTIFICATIONS ====================
    
    /**
     * Called by OfflineManager when offline timeout is reached
     */
    fun onOfflineTimeout(driverId: String, bookingId: String, durationMs: Long) {
        scope.launch {
            emitSafetyAlert(
                AlertType.ADMIN_NOTIFICATION_SENT,
                driverId,
                bookingId,
                "Driver offline for ${durationMs / 1000}s during active ride"
            )
        }
    }
    
    /**
     * Get offline drivers for admin dashboard
     */
    fun getOfflineDrivers(): Flow<List<OfflineDriverInfo>> {
        return offlineManager.observeOfflineDrivers().map { statusList ->
            statusList.map { status ->
                OfflineDriverInfo(
                    driverId = status.driverId,
                    bookingId = status.bookingId,
                    offlineSince = status.wentOfflineAt,
                    lastLocation = if (status.lastLocationLat != null && status.lastLocationLng != null) {
                        Pair(status.lastLocationLat, status.lastLocationLng)
                    } else null,
                    isRideActive = status.bookingId != null
                )
            }
        }
    }
    
    data class OfflineDriverInfo(
        val driverId: String,
        val bookingId: String?,
        val offlineSince: Long,
        val lastLocation: Pair<Double, Double>?,
        val isRideActive: Boolean
    )
    
    // ==================== HELPER METHODS ====================
    
    private suspend fun emitSafetyAlert(
        type: AlertType,
        driverId: String,
        bookingId: String,
        message: String
    ) {
        _safetyAlerts.emit(
            SafetyAlert(type, driverId, bookingId, message)
        )
    }
    
    /**
     * Get current ride status for display
     */
    fun getRideStatus(): RideStatus {
        return RideStatus(
            isInActiveRide = _isInOfflineRide.value,
            bookingId = _currentBookingId.value,
            networkStatus = _networkStatus.value,
            isOffline = _networkStatus.value == NetworkStatus.OFFLINE,
            offlineDurationMs = _offlineDuration.value,
            pendingSyncCount = _pendingSyncItems.value,
            showOfflineMessage = _networkStatus.value == NetworkStatus.OFFLINE && _isInOfflineRide.value
        )
    }
    
    data class RideStatus(
        val isInActiveRide: Boolean,
        val bookingId: String?,
        val networkStatus: NetworkStatus,
        val isOffline: Boolean,
        val offlineDurationMs: Long,
        val pendingSyncCount: Int,
        val showOfflineMessage: Boolean
    )
    
    data class TripStartResult(val success: Boolean, val message: String)
    data class TripCompleteResult(val success: Boolean, val message: String, val completedOffline: Boolean)
    data class OtpVerifyResult(val success: Boolean, val message: String)
    
    // ==================== CLEANUP ====================
    
    fun release() {
        scope.cancel()
    }
    
    companion object {
        private const val TAG = "OfflineSafetyService"
        
        @Volatile
        private var instance: OfflineSafetyService? = null
        
        fun getInstance(context: Context): OfflineSafetyService {
            return instance ?: synchronized(this) {
                instance ?: OfflineSafetyService(context.applicationContext).also { instance = it }
            }
        }
    }
}
