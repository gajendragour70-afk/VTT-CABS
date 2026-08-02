package com.vttcabs.common.offline

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Admin Dashboard Helper for Offline Ride Status
 * Shows "Driver Offline (Ride Active)" status for drivers who go offline during trips
 */
class AdminOfflineHelper private constructor(context: Context) {
    
    private val applicationContext = context.applicationContext
    private val offlineManager = OfflineManager.getInstance(applicationContext)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Configuration
    private var alertThresholdMs = 5 * 60 * 1000L // Alert admin after 5 minutes offline
    
    // State
    private val _offlineDrivers = MutableStateFlow<List<OfflineDriverDisplay>>(emptyList())
    val offlineDrivers: StateFlow<List<OfflineDriverDisplay>> = _offlineDrivers.asStateFlow()
    
    private val _criticalAlerts = MutableStateFlow<List<CriticalOfflineAlert>>(emptyList())
    val criticalAlerts: StateFlow<List<CriticalOfflineAlert>> = _criticalAlerts.asStateFlow()
    
    data class OfflineDriverDisplay(
        val driverId: String,
        val driverName: String,
        val bookingId: String?,
        val pickupLocation: String?,
        val dropLocation: String?,
        val customerName: String?,
        val customerPhone: String?,
        val offlineSince: Long,
        val offlineDurationText: String,
        val lastKnownLocation: Pair<Double, Double>?,
        val status: OfflineStatus,
        val actionRequired: Boolean
    )
    
    enum class OfflineStatus {
        OFFLINE_RIDE_ACTIVE,      // Driver offline but ride is active (OK)
        OFFLINE_WARNING,          // Driver offline > 2 minutes (Warning)
        OFFLINE_CRITICAL,         // Driver offline > 5 minutes (Critical)
        RECONNECTING              // Driver just came back online
    }
    
    data class CriticalOfflineAlert(
        val driverId: String,
        val driverName: String,
        val bookingId: String,
        val offlineDurationMs: Long,
        val lastLocation: Pair<Double, Double>?,
        val timestamp: Long,
        val notificationSent: Boolean
    )
    
    init {
        startMonitoring()
    }
    
    /**
     * Start monitoring offline drivers
     */
    private fun startMonitoring() {
        scope.launch {
            offlineManager.observeOfflineDrivers().collect { drivers ->
                val displays = drivers.map { status ->
                    createOfflineDriverDisplay(status)
                }
                _offlineDrivers.value = displays
                
                // Update critical alerts
                updateCriticalAlerts(displays)
            }
        }
    }
    
    /**
     * Create display object from offline status
     */
    private suspend fun createOfflineDriverDisplay(status: DriverOfflineStatus): OfflineDriverDisplay {
        val offlineMs = System.currentTimeMillis() - status.wentOfflineAt
        val durationText = formatDuration(offlineMs)
        
        // Get driver and booking details from cache if available
        val driverName = "Driver ${status.driverId.take(8)}" // In production, fetch from cache
        
        val statusType = when {
            offlineMs < 2 * 60 * 1000 -> OfflineStatus.OFFLINE_RIDE_ACTIVE
            offlineMs < alertThresholdMs -> OfflineStatus.OFFLINE_WARNING
            else -> OfflineStatus.OFFLINE_CRITICAL
        }
        
        return OfflineDriverDisplay(
            driverId = status.driverId,
            driverName = driverName,
            bookingId = status.bookingId,
            pickupLocation = null, // Would be fetched from cached booking
            dropLocation = null,
            customerName = null,
            customerPhone = null,
            offlineSince = status.wentOfflineAt,
            offlineDurationText = durationText,
            lastKnownLocation = if (status.lastLocationLat != null && status.lastLocationLng != null) {
                Pair(status.lastLocationLat, status.lastLocationLng)
            } else null,
            status = statusType,
            actionRequired = offlineMs >= alertThresholdMs
        )
    }
    
    /**
     * Update critical alerts list
     */
    private fun updateCriticalAlerts(drivers: List<OfflineDriverDisplay>) {
        val critical = drivers
            .filter { it.status == OfflineStatus.OFFLINE_CRITICAL }
            .map { driver ->
                CriticalOfflineAlert(
                    driverId = driver.driverId,
                    driverName = driver.driverName,
                    bookingId = driver.bookingId ?: "",
                    offlineDurationMs = System.currentTimeMillis() - driver.offlineSince,
                    lastLocation = driver.lastKnownLocation,
                    timestamp = System.currentTimeMillis(),
                    notificationSent = false
                )
            }
        
        _criticalAlerts.value = critical
    }
    
    /**
     * Format duration as human readable text
     */
    private fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    /**
     * Get dashboard summary for admin
     */
    fun getDashboardSummary(): DashboardSummary {
        val drivers = _offlineDrivers.value
        return DashboardSummary(
            totalOfflineDrivers = drivers.size,
            activeRideOffline = drivers.count { it.status == OfflineStatus.OFFLINE_RIDE_ACTIVE },
            warningOffline = drivers.count { it.status == OfflineStatus.OFFLINE_WARNING },
            criticalOffline = drivers.count { it.status == OfflineStatus.OFFLINE_CRITICAL },
            criticalAlerts = _criticalAlerts.value.size,
            lastUpdateTime = System.currentTimeMillis()
        )
    }
    
    data class DashboardSummary(
        val totalOfflineDrivers: Int,
        val activeRideOffline: Int,
        val warningOffline: Int,
        val criticalOffline: Int,
        val criticalAlerts: Int,
        val lastUpdateTime: Long
    )
    
    /**
     * Send notification to customer about driver being offline
     */
    suspend fun notifyCustomerAboutDriverOffline(bookingId: String, customerId: String) {
        Log.d(TAG, "Notifying customer $customerId about driver offline for booking $bookingId")
        // In production, this would send a notification via SupabaseService
    }
    
    /**
     * Check if admin has been notified about a driver's offline status
     */
    suspend fun hasAdminBeenNotified(driverId: String): Boolean {
        val status = offlineManager.getDriverOfflineStatus(driverId)
        return status?.adminNotified == true
    }
    
    /**
     * Mark admin as notified
     */
    suspend fun markAdminNotified(driverId: String) {
        // This would be handled by OfflineManager
    }
    
    /**
     * Get all drivers with active rides who are offline
     */
    fun getOfflineDriversWithActiveRides(): Flow<List<OfflineDriverDisplay>> {
        return _offlineDrivers.map { drivers ->
            drivers.filter { it.bookingId != null && it.status != OfflineStatus.RECONNECTING }
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        scope.cancel()
    }
    
    companion object {
        private const val TAG = "AdminOfflineHelper"
        
        @Volatile
        private var instance: AdminOfflineHelper? = null
        
        fun getInstance(context: Context): AdminOfflineHelper {
            return instance ?: synchronized(this) {
                instance ?: AdminOfflineHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Extension functions for easy access in Compose
 */
object OfflineStatusExtensions {
    
    fun AdminOfflineHelper.OfflineStatus.getDisplayColor(): Long {
        return when (this) {
            AdminOfflineHelper.OfflineStatus.OFFLINE_RIDE_ACTIVE -> 0xFF4CAF50 // Green
            AdminOfflineHelper.OfflineStatus.OFFLINE_WARNING -> 0xFFFF9800 // Orange
            AdminOfflineHelper.OfflineStatus.OFFLINE_CRITICAL -> 0xFFF44336 // Red
            AdminOfflineHelper.OfflineStatus.RECONNECTING -> 0xFF2196F3 // Blue
        }
    }
    
    fun AdminOfflineHelper.OfflineStatus.getDisplayText(): String {
        return when (this) {
            AdminOfflineHelper.OfflineStatus.OFFLINE_RIDE_ACTIVE -> "Driver Offline (Ride Active)"
            AdminOfflineHelper.OfflineStatus.OFFLINE_WARNING -> "Driver Offline (Warning)"
            AdminOfflineHelper.OfflineStatus.OFFLINE_CRITICAL -> "Driver Offline (Critical)"
            AdminOfflineHelper.OfflineStatus.RECONNECTING -> "Reconnecting..."
        }
    }
    
    fun AdminOfflineHelper.OfflineStatus.getIcon(): String {
        return when (this) {
            AdminOfflineHelper.OfflineStatus.OFFLINE_RIDE_ACTIVE -> "✓"
            AdminOfflineHelper.OfflineStatus.OFFLINE_WARNING -> "⚠"
            AdminOfflineHelper.OfflineStatus.OFFLINE_CRITICAL -> "🚨"
            AdminOfflineHelper.OfflineStatus.RECONNECTING -> "↻"
        }
    }
}
