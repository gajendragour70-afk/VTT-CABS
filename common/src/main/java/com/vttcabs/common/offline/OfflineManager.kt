package com.vttcabs.common.offline

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Main offline management service
 * Coordinates GPS saving, sync, and offline trip operations
 */
class OfflineManager private constructor(context: Context) {
    
    private val applicationContext = context.applicationContext
    private val database = OfflineDatabase.getInstance(applicationContext)
    private val dao = database.offlineDao()
    private val networkMonitor = NetworkMonitor.getInstance(applicationContext)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val gson = Gson()
    
    // Configuration
    private var config = OfflineConfig()
    
    // State flows
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()
    
    private val _currentOfflineDuration = MutableStateFlow(0L)
    val currentOfflineDuration: StateFlow<Long> = _currentOfflineDuration.asStateFlow()
    
    private val _offlineAlertSent = MutableStateFlow(false)
    val offlineAlertSent: StateFlow<Boolean> = _offlineAlertSent.asStateFlow()
    
    // Jobs
    private var syncJob: Job? = null
    private var gpsSaveJob: Job? = null
    private var offlineMonitorJob: Job? = null
    private var lastLocationJob: Job? = null
    
    // Callbacks
    var onNetworkRestored: (() -> Unit)? = null
    var onNetworkLost: (() -> Unit)? = null
    var onOfflineAlertTimeout: ((driverId: String, bookingId: String, durationMs: Long) -> Unit)? = null
    var onSyncCompleted: ((syncedCount: Int) -> Unit)? = null
    
    init {
        startNetworkMonitoring()
    }
    
    // ==================== CONFIGURATION ====================
    
    fun updateConfig(newConfig: OfflineConfig) {
        config = newConfig
        Log.d(TAG, "Config updated: $config")
    }
    
    // ==================== NETWORK MONITORING ====================
    
    private fun startNetworkMonitoring() {
        networkMonitor.startMonitoring()
        
        scope.launch {
            networkMonitor.isOnline.collect { online ->
                val wasOffline = !_isOnline.value
                _isOnline.value = online
                
                if (online && wasOffline) {
                    Log.d(TAG, "Network restored!")
                    onNetworkRestored?.invoke()
                    syncPendingData()
                } else if (!online && wasOffline) {
                    Log.d(TAG, "Network lost!")
                    onNetworkLost?.invoke()
                    startOfflineMonitoring()
                }
            }
        }
    }
    
    private fun startOfflineMonitoring() {
        offlineMonitorJob?.cancel()
        offlineMonitorJob = scope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && !_isOnline.value) {
                val duration = System.currentTimeMillis() - startTime
                _currentOfflineDuration.value = duration
                
                // Check if we've exceeded the timeout
                if (duration >= config.maxOfflineTimeMs && !_offlineAlertSent.value) {
                    _offlineAlertSent.value = true
                    handleOfflineTimeout()
                }
                
                delay(1000) // Check every second
            }
        }
    }
    
    private fun handleOfflineTimeout() {
        scope.launch {
            val statusList = dao.getUnnotifiedOfflineDrivers()
            statusList.forEach { status ->
                if (status.bookingId != null) {
                    Log.d(TAG, "Offline timeout for driver: ${status.driverId}")
                    onOfflineAlertTimeout?.invoke(status.driverId, status.bookingId, config.maxOfflineTimeMs)
                    dao.markAdminNotified(status.driverId)
                }
            }
        }
    }
    
    // ==================== GPS LOCATION SAVING ====================
    
    /**
     * Start continuous GPS saving for a booking
     */
    fun startGpsSaving(driverId: String, bookingId: String) {
        gpsSaveJob?.cancel()
        gpsSaveJob = scope.launch {
            Log.d(TAG, "Starting GPS saving for booking: $bookingId")
            
            while (isActive) {
                // In production, this would get actual GPS location
                // For now, we'll use the callback pattern
                delay(config.gpsSaveIntervalMs)
            }
        }
    }
    
    /**
     * Stop GPS saving
     */
    fun stopGpsSaving() {
        gpsSaveJob?.cancel()
        gpsSaveJob = null
        Log.d(TAG, "GPS saving stopped")
    }
    
    /**
     * Save GPS location (called from GPS service)
     */
    suspend fun saveGpsLocation(location: Location, driverId: String, bookingId: String) {
        val pendingGps = PendingGpsLocation(
            bookingId = bookingId,
            driverId = driverId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            speed = location.speed,
            bearing = location.bearing,
            timestamp = location.time,
            isSynced = false
        )
        
        dao.insertGpsLocation(pendingGps)
        updatePendingCount()
        
        // Try to sync immediately if online
        if (_isOnline.value) {
            syncPendingGps()
        }
    }
    
    /**
     * Save GPS location from coordinates
     */
    suspend fun saveGpsCoordinates(
        latitude: Double,
        longitude: Double,
        driverId: String,
        bookingId: String,
        accuracy: Float? = null,
        speed: Float? = null
    ) {
        val pendingGps = PendingGpsLocation(
            bookingId = bookingId,
            driverId = driverId,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )
        
        dao.insertGpsLocation(pendingGps)
        updatePendingCount()
        
        if (_isOnline.value) {
            syncPendingGps()
        }
    }
    
    // ==================== BOOKING CACHING ====================
    
    /**
     * Cache a booking for offline access
     */
    suspend fun cacheBooking(booking: CachedBooking) {
        dao.insertBooking(booking.copy(isSynced = true))
        Log.d(TAG, "Booking cached: ${booking.bookingId}")
    }
    
    /**
     * Get cached booking
     */
    suspend fun getCachedBooking(bookingId: String): CachedBooking? {
        return dao.getBookingById(bookingId)
    }
    
    /**
     * Observe cached booking
     */
    fun observeCachedBooking(bookingId: String): Flow<CachedBooking?> {
        return dao.observeBookingById(bookingId)
    }
    
    /**
     * Get active booking for driver
     */
    suspend fun getActiveBookingForDriver(driverId: String): CachedBooking? {
        return dao.getActiveBookingForDriver(driverId)
    }
    
    /**
     * Get active booking for customer
     */
    suspend fun getActiveBookingForCustomer(customerId: String): CachedBooking? {
        return dao.getActiveBookingForCustomer(customerId)
    }
    
    /**
     * Update booking status locally
     */
    suspend fun updateBookingStatus(bookingId: String, status: String) {
        dao.updateBookingStatus(bookingId, status)
        
        // Queue action if offline
        if (!_isOnline.value) {
            queueAction(bookingId, ActionType.STATUS_UPDATE, mapOf("status" to status))
        }
    }
    
    // ==================== OFFLINE ACTIONS ====================
    
    /**
     * Queue an action to be executed when online
     */
    suspend fun queueAction(bookingId: String, actionType: ActionType, payload: Map<String, Any?>) {
        val payloadJson = gson.toJson(payload)
        val action = PendingAction(
            bookingId = bookingId,
            actionType = actionType,
            payload = payloadJson
        )
        dao.insertAction(action)
        updatePendingCount()
        Log.d(TAG, "Action queued: $actionType for booking: $bookingId")
    }
    
    /**
     * Start trip offline (with OTP verification)
     */
    suspend fun startTripOffline(bookingId: String, otp: String, customerId: String): Result<Boolean> {
        val booking = dao.getBookingById(bookingId)
        
        if (booking == null) {
            return Result.failure(Exception("Booking not found"))
        }
        
        if (booking.otp != otp) {
            return Result.failure(Exception("Invalid OTP"))
        }
        
        // Update local status
        dao.updateBookingStatus(bookingId, "TRIP_STARTED")
        
        // Queue the action
        queueAction(bookingId, ActionType.TRIP_START, mapOf(
            "otp" to otp,
            "startTime" to System.currentTimeMillis()
        ))
        
        updatePendingCount()
        Log.d(TAG, "Trip started offline: $bookingId")
        return Result.success(true)
    }
    
    /**
     * Complete trip offline
     */
    suspend fun completeTripOffline(
        bookingId: String,
        endLat: Double,
        endLng: Double,
        distance: Double,
        duration: Long
    ): Result<Boolean> {
        val booking = dao.getBookingById(bookingId)
        
        if (booking == null) {
            return Result.failure(Exception("Booking not found"))
        }
        
        // Update local status
        dao.updateBookingStatus(bookingId, "TRIP_COMPLETED")
        
        // Queue the action
        queueAction(bookingId, ActionType.TRIP_COMPLETE, mapOf(
            "endLat" to endLat,
            "endLng" to endLng,
            "distance" to distance,
            "duration" to duration,
            "endTime" to System.currentTimeMillis()
        ))
        
        // Stop GPS saving
        stopGpsSaving()
        
        updatePendingCount()
        Log.d(TAG, "Trip completed offline: $bookingId")
        return Result.success(true)
    }
    
    /**
     * Verify OTP offline
     */
    suspend fun verifyOtpOffline(bookingId: String, otp: String): Result<Boolean> {
        val booking = dao.getBookingById(bookingId)
        
        if (booking == null) {
            return Result.failure(Exception("Booking not found"))
        }
        
        if (booking.tripOtp != otp && booking.otp != otp) {
            return Result.failure(Exception("Invalid OTP"))
        }
        
        // Queue verification
        queueAction(bookingId, ActionType.OTP_VERIFY, mapOf("otp" to otp))
        
        Log.d(TAG, "OTP verified offline: $bookingId")
        return Result.success(true)
    }
    
    // ==================== OFFLINE STATUS TRACKING ====================
    
    /**
     * Mark driver as offline
     */
    suspend fun setDriverOffline(driverId: String, bookingId: String?, lastLat: Double?, lastLng: Double?) {
        val status = DriverOfflineStatus(
            driverId = driverId,
            bookingId = bookingId,
            wentOfflineAt = System.currentTimeMillis(),
            lastKnownOnlineAt = System.currentTimeMillis(),
            isCurrentlyOffline = true,
            lastLocationLat = lastLat,
            lastLocationLng = lastLng
        )
        dao.insertOfflineStatus(status)
        _offlineAlertSent.value = false
        Log.d(TAG, "Driver marked offline: $driverId")
    }
    
    /**
     * Mark driver as online
     */
    suspend fun setDriverOnline(driverId: String, lastLat: Double?, lastLng: Double?) {
        dao.updateOfflineState(driverId, false)
        dao.deleteOfflineStatus(driverId)
        _offlineAlertSent.value = false
        Log.d(TAG, "Driver marked online: $driverId")
    }
    
    /**
     * Get offline status for driver
     */
    suspend fun getDriverOfflineStatus(driverId: String): DriverOfflineStatus? {
        return dao.getOfflineStatus(driverId)
    }
    
    /**
     * Observe all offline drivers
     */
    fun observeOfflineDrivers(): Flow<List<DriverOfflineStatus>> {
        return dao.observeOfflineDrivers()
    }
    
    // ==================== SYNC OPERATIONS ====================
    
    /**
     * Sync all pending data when network is restored
     */
    private fun syncPendingData() {
        syncJob?.cancel()
        syncJob = scope.launch {
            Log.d(TAG, "Starting sync...")
            
            // Update offline status
            _offlineAlertSent.value = false
            
            // Sync GPS locations
            syncPendingGps()
            
            // Sync pending actions
            syncPendingActions()
            
            onSyncCompleted?.invoke(_pendingSyncCount.value)
            Log.d(TAG, "Sync completed. Pending count: ${_pendingSyncCount.value}")
        }
    }
    
    /**
     * Sync pending GPS locations
     */
    private suspend fun syncPendingGps() {
        if (!_isOnline.value) return
        
        val pendingLocations = dao.getPendingGpsLocations(config.batchSize)
        if (pendingLocations.isEmpty()) return
        
        Log.d(TAG, "Syncing ${pendingLocations.size} GPS locations")
        
        try {
            // In production, this would call SupabaseService
            // For now, just mark as synced
            val syncedIds = pendingLocations.map { it.id }
            dao.markGpsSynced(syncedIds)
            updatePendingCount()
            
            // Update sync status
            dao.updateSyncStatus("gps", syncStatus = "success", pendingCount = dao.getPendingGpsCount())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync GPS locations", e)
            val failedIds = pendingLocations.map { it.id }
            dao.incrementGpsRetryCount(failedIds)
        }
    }
    
    /**
     * Sync pending actions
     */
    private suspend fun syncPendingActions() {
        if (!_isOnline.value) return
        
        val pendingActions = dao.getPendingActions()
        if (pendingActions.isEmpty()) return
        
        Log.d(TAG, "Syncing ${pendingActions.size} pending actions")
        
        for (action in pendingActions) {
            try {
                dao.updateActionStatus(action.id, PendingActionStatus.IN_PROGRESS)
                
                // Process based on action type
                val success = processAction(action)
                
                if (success) {
                    dao.updateActionStatus(action.id, PendingActionStatus.COMPLETED)
                } else {
                    dao.incrementActionRetryCount(action.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process action: ${action.actionType}", e)
                dao.incrementActionRetryCount(action.id)
            }
        }
        
        updatePendingCount()
    }
    
    /**
     * Process a single pending action
     */
    private suspend fun processAction(action: PendingAction): Boolean {
        // In production, this would call the appropriate SupabaseService method
        return when (action.actionType) {
            ActionType.TRIP_START -> {
                val data = gson.fromJson(action.payload, Map::class.java)
                Log.d(TAG, "Syncing trip start: ${action.bookingId}")
                true
            }
            ActionType.TRIP_COMPLETE -> {
                val data = gson.fromJson(action.payload, Map::class.java)
                Log.d(TAG, "Syncing trip complete: ${action.bookingId}")
                true
            }
            ActionType.STATUS_UPDATE -> {
                val data = gson.fromJson(action.payload, Map::class.java)
                Log.d(TAG, "Syncing status update: ${action.bookingId}")
                true
            }
            ActionType.OTP_VERIFY -> {
                Log.d(TAG, "Syncing OTP verify: ${action.bookingId}")
                true
            }
            else -> {
                Log.w(TAG, "Unknown action type: ${action.actionType}")
                true
            }
        }
    }
    
    private suspend fun updatePendingCount() {
        _pendingSyncCount.value = dao.getTotalPendingCount()
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Clean up old data
     */
    suspend fun cleanup() {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        dao.deleteOldCompletedBookings(oneDayAgo)
        dao.deleteOldSyncedGps(oneDayAgo)
        dao.deleteOldCompletedActions(oneDayAgo)
        dao.deleteFailedGpsLocations(config.maxRetryCount)
        dao.deleteFailedActions(config.maxRetryCount)
        Log.d(TAG, "Cleanup completed")
    }
    
    /**
     * Release resources
     */
    fun release() {
        scope.cancel()
        networkMonitor.stopMonitoring()
        instance = null
        Log.d(TAG, "OfflineManager released")
    }
    
    companion object {
        private const val TAG = "OfflineManager"
        
        @Volatile
        private var instance: OfflineManager? = null
        
        fun getInstance(context: Context): OfflineManager {
            return instance ?: synchronized(this) {
                instance ?: OfflineManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
