package com.vttcabs.common.offline

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineDao {
    
    // ==================== CACHED BOOKINGS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: CachedBooking)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<CachedBooking>)
    
    @Query("SELECT * FROM cached_bookings WHERE bookingId = :bookingId")
    suspend fun getBookingById(bookingId: String): CachedBooking?
    
    @Query("SELECT * FROM cached_bookings WHERE bookingId = :bookingId")
    fun observeBookingById(bookingId: String): Flow<CachedBooking?>
    
    @Query("SELECT * FROM cached_bookings WHERE driverId = :driverId AND status NOT IN ('COMPLETED', 'CANCELLED')")
    suspend fun getActiveBookingForDriver(driverId: String): CachedBooking?
    
    @Query("SELECT * FROM cached_bookings WHERE customerId = :customerId AND status NOT IN ('COMPLETED', 'CANCELLED')")
    suspend fun getActiveBookingForCustomer(customerId: String): CachedBooking?
    
    @Query("SELECT * FROM cached_bookings WHERE isSynced = 0")
    suspend fun getUnsyncedBookings(): List<CachedBooking>
    
    @Query("UPDATE cached_bookings SET isSynced = 1, lastSyncTime = :syncTime WHERE bookingId = :bookingId")
    suspend fun markBookingSynced(bookingId: String, syncTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE cached_bookings SET status = :status, updatedAt = :updatedAt WHERE bookingId = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM cached_bookings WHERE bookingId = :bookingId")
    suspend fun deleteBooking(bookingId: String)
    
    @Query("DELETE FROM cached_bookings WHERE updatedAt < :olderThan AND status IN ('COMPLETED', 'CANCELLED')")
    suspend fun deleteOldCompletedBookings(olderThan: Long)
    
    // ==================== PENDING GPS LOCATIONS ====================
    
    @Insert
    suspend fun insertGpsLocation(location: PendingGpsLocation)
    
    @Insert
    suspend fun insertGpsLocations(locations: List<PendingGpsLocation>)
    
    @Query("SELECT * FROM pending_gps_locations WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getPendingGpsLocations(limit: Int = 50): List<PendingGpsLocation>
    
    @Query("SELECT * FROM pending_gps_locations WHERE bookingId = :bookingId AND isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingGpsForBooking(bookingId: String): List<PendingGpsLocation>
    
    @Query("SELECT COUNT(*) FROM pending_gps_locations WHERE isSynced = 0")
    suspend fun getPendingGpsCount(): Int
    
    @Query("SELECT COUNT(*) FROM pending_gps_locations WHERE isSynced = 0")
    fun observePendingGpsCount(): Flow<Int>
    
    @Query("UPDATE pending_gps_locations SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markGpsSynced(ids: List<Long>)
    
    @Query("UPDATE pending_gps_locations SET retryCount = retryCount + 1, lastRetryTime = :retryTime WHERE id IN (:ids)")
    suspend fun incrementGpsRetryCount(ids: List<Long>, retryTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM pending_gps_locations WHERE isSynced = 1 AND timestamp < :olderThan")
    suspend fun deleteOldSyncedGps(olderThan: Long)
    
    @Query("DELETE FROM pending_gps_locations WHERE retryCount >= :maxRetries")
    suspend fun deleteFailedGpsLocations(maxRetries: Int = 5)
    
    // ==================== PENDING ACTIONS ====================
    
    @Insert
    suspend fun insertAction(action: PendingAction)
    
    @Query("SELECT * FROM pending_actions WHERE status = :status ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingActions(status: PendingActionStatus = PendingActionStatus.PENDING, limit: Int = 20): List<PendingAction>
    
    @Query("SELECT * FROM pending_actions WHERE bookingId = :bookingId ORDER BY createdAt ASC")
    suspend fun getActionsForBooking(bookingId: String): List<PendingAction>
    
    @Query("SELECT COUNT(*) FROM pending_actions WHERE status = 'PENDING'")
    suspend fun getPendingActionsCount(): Int
    
    @Query("SELECT COUNT(*) FROM pending_actions WHERE status = 'PENDING'")
    fun observePendingActionsCount(): Flow<Int>
    
    @Query("UPDATE pending_actions SET status = :status WHERE id = :id")
    suspend fun updateActionStatus(id: Long, status: PendingActionStatus)
    
    @Query("UPDATE pending_actions SET retryCount = retryCount + 1, lastRetryTime = :retryTime WHERE id = :id")
    suspend fun incrementActionRetryCount(id: Long, retryTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM pending_actions WHERE status = 'COMPLETED' AND createdAt < :olderThan")
    suspend fun deleteOldCompletedActions(olderThan: Long)
    
    @Query("DELETE FROM pending_actions WHERE retryCount >= :maxRetries")
    suspend fun deleteFailedActions(maxRetries: Int = 5)
    
    // ==================== DRIVER OFFLINE STATUS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineStatus(status: DriverOfflineStatus)
    
    @Query("SELECT * FROM driver_offline_status WHERE driverId = :driverId")
    suspend fun getOfflineStatus(driverId: String): DriverOfflineStatus?
    
    @Query("SELECT * FROM driver_offline_status WHERE isCurrentlyOffline = 1")
    fun observeOfflineDrivers(): Flow<List<DriverOfflineStatus>>
    
    @Query("SELECT * FROM driver_offline_status WHERE isCurrentlyOffline = 1 AND adminNotified = 0")
    suspend fun getUnnotifiedOfflineDrivers(): List<DriverOfflineStatus>
    
    @Query("UPDATE driver_offline_status SET adminNotified = 1 WHERE driverId = :driverId")
    suspend fun markAdminNotified(driverId: String)
    
    @Query("UPDATE driver_offline_status SET customerNotified = 1 WHERE driverId = :driverId")
    suspend fun markCustomerNotified(driverId: String)
    
    @Query("UPDATE driver_offline_status SET isCurrentlyOffline = :isOffline, wentOfflineAt = :wentOfflineAt WHERE driverId = :driverId")
    suspend fun updateOfflineState(driverId: String, isOffline: Boolean, wentOfflineAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM driver_offline_status WHERE driverId = :driverId")
    suspend fun deleteOfflineStatus(driverId: String)
    
    // ==================== SYNC STATUS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncStatus(status: SyncStatus)
    
    @Query("SELECT * FROM sync_status WHERE type = :type")
    suspend fun getSyncStatus(type: String): SyncStatus?
    
    @Query("UPDATE sync_status SET lastSyncTime = :syncTime, lastSyncStatus = :syncStatus, pendingCount = :pendingCount, lastError = :error WHERE type = :type")
    suspend fun updateSyncStatus(
        type: String,
        syncTime: Long = System.currentTimeMillis(),
        syncStatus: String,
        pendingCount: Int,
        error: String? = null
    )
    
    // ==================== UTILITY ====================
    
    @Query("SELECT COUNT(*) FROM pending_gps_locations")
    suspend fun getTotalPendingCount(): Int
    
    @Query("DELETE FROM pending_gps_locations")
    suspend fun clearAllGpsLocations()
    
    @Query("DELETE FROM pending_actions")
    suspend fun clearAllActions()
}
