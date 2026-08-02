package com.vttcabs.common.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Cached booking for offline access
 */
@Entity(tableName = "cached_bookings")
@Index(value = ["bookingId"], unique = true)
data class CachedBooking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val driverId: String?,
    val driverName: String?,
    val driverPhone: String?,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropAddress: String,
    val dropLat: Double,
    val dropLng: Double,
    val bookingType: String,
    val vehicleCategory: String,
    val status: String,
    val otp: String,
    val fare: Double,
    val paymentMethod: String,
    val tripStartTime: Long?,
    val tripEndTime: Long?,
    val tripOtp: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis()
)

/**
 * Pending GPS location to be synced
 */
@Entity(tableName = "pending_gps_locations")
@Index(value = ["bookingId"])
data class PendingGpsLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: String,
    val driverId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val speed: Float?,
    val bearing: Float?,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val retryCount: Int = 0
)

/**
 * Pending action to be synced when online
 */
@Entity(tableName = "pending_actions")
@Index(value = ["bookingId"])
data class PendingAction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: String,
    val actionType: ActionType,
    val payload: String, // JSON string with action data
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastRetryTime: Long? = null,
    val status: PendingActionStatus = PendingActionStatus.PENDING
)

enum class ActionType {
    TRIP_START,
    TRIP_COMPLETE,
    STATUS_UPDATE,
    OTP_VERIFY,
    CANCEL_BOOKING,
    LOCATION_UPDATE,
    SOS_ALERT
}

enum class PendingActionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

/**
 * Driver offline status tracking
 */
@Entity(tableName = "driver_offline_status")
data class DriverOfflineStatus(
    @PrimaryKey
    val driverId: String,
    val bookingId: String?,
    val wentOfflineAt: Long,
    val lastKnownOnlineAt: Long,
    val isCurrentlyOffline: Boolean,
    val lastLocationLat: Double?,
    val lastLocationLng: Double?,
    val pendingSyncCount: Int = 0,
    val adminNotified: Boolean = false,
    val customerNotified: Boolean = false
)

/**
 * Sync status tracking
 */
@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey
    val type: String, // 'gps', 'actions', 'bookings'
    val lastSyncTime: Long,
    val lastSyncStatus: String, // 'success', 'failed', 'partial'
    val pendingCount: Int = 0,
    val lastError: String? = null
)

/**
 * Offline configuration
 */
data class OfflineConfig(
    val gpsSaveIntervalMs: Long = 5000L,        // Save GPS every 5 seconds
    val syncIntervalMs: Long = 10000L,           // Sync every 10 seconds when online
    val maxOfflineTimeMs: Long = 5 * 60 * 1000L, // 5 minutes max offline
    val maxRetryCount: Int = 5,
    val batchSize: Int = 50
)
