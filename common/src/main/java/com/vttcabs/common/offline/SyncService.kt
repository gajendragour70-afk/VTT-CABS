package com.vttcabs.common.offline

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Background sync service that automatically syncs data when online
 */
class SyncService(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val networkMonitor = NetworkMonitor.getInstance(context)
    private val database = OfflineDatabase.getInstance(context)
    private val dao = database.offlineDao()
    
    private var syncJob: Job? = null
    private var isRunning = false
    
    // Configuration
    private var syncIntervalMs = 10_000L // 10 seconds
    private var batchSize = 50
    
    // State
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    private val _syncErrors = MutableStateFlow<List<SyncError>>(emptyList())
    val syncErrors: StateFlow<List<SyncError>> = _syncErrors.asStateFlow()
    
    data class SyncError(
        val timestamp: Long,
        val type: String,
        val message: String,
        val retryable: Boolean
    )
    
    /**
     * Start the sync service
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        
        Log.d(TAG, "SyncService started")
        
        // Monitor network and sync when available
        scope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    startPeriodicSync()
                } else {
                    stopPeriodicSync()
                }
            }
        }
    }
    
    /**
     * Stop the sync service
     */
    fun stop() {
        isRunning = false
        stopPeriodicSync()
        Log.d(TAG, "SyncService stopped")
    }
    
    /**
     * Start periodic sync
     */
    private fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                if (networkMonitor.isOnline.value) {
                    performSync()
                }
                delay(syncIntervalMs)
            }
        }
    }
    
    /**
     * Stop periodic sync
     */
    private fun stopPeriodicSync() {
        syncJob?.cancel()
        syncJob = null
    }
    
    /**
     * Force immediate sync
     */
    suspend fun forceSync() {
        if (!networkMonitor.isOnline.value) {
            Log.w(TAG, "Cannot force sync: offline")
            return
        }
        
        performSync()
    }
    
    /**
     * Perform sync operation
     */
    private suspend fun performSync() {
        if (_isSyncing.value) {
            Log.d(TAG, "Sync already in progress")
            return
        }
        
        _isSyncing.value = true
        
        try {
            var totalSynced = 0
            
            // Sync GPS locations
            totalSynced += syncGpsLocations()
            
            // Sync pending actions
            totalSynced += syncActions()
            
            _lastSyncTime.value = System.currentTimeMillis()
            
            if (totalSynced > 0) {
                Log.d(TAG, "Synced $totalSynced items")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            addSyncError("general", e.message ?: "Unknown error", true)
        } finally {
            _isSyncing.value = false
        }
    }
    
    /**
     * Sync pending GPS locations
     */
    private suspend fun syncGpsLocations(): Int {
        val pendingLocations = dao.getPendingGpsLocations(batchSize)
        if (pendingLocations.isEmpty()) return 0
        
        Log.d(TAG, "Syncing ${pendingLocations.size} GPS locations")
        
        // Group by booking for batch processing
        val byBooking = pendingLocations.groupBy { it.bookingId }
        var syncedCount = 0
        
        for ((bookingId, locations) in byBooking) {
            try {
                // In production, call SupabaseService to batch upload
                // For now, just mark as synced
                
                val ids = locations.map { it.id }
                dao.markGpsSynced(ids)
                syncedCount += ids.size
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync GPS for booking $bookingId", e)
                val ids = locations.map { it.id }
                dao.incrementGpsRetryCount(ids)
                addSyncError("gps", "Booking $bookingId: ${e.message}", true)
            }
        }
        
        return syncedCount
    }
    
    /**
     * Sync pending actions
     */
    private suspend fun syncActions(): Int {
        val pendingActions = dao.getPendingActions()
        if (pendingActions.isEmpty()) return 0
        
        Log.d(TAG, "Syncing ${pendingActions.size} actions")
        var syncedCount = 0
        
        for (action in pendingActions) {
            try {
                dao.updateActionStatus(action.id, PendingActionStatus.IN_PROGRESS)
                
                val success = processAction(action)
                
                if (success) {
                    dao.updateActionStatus(action.id, PendingActionStatus.COMPLETED)
                    syncedCount++
                } else {
                    dao.incrementActionRetryCount(action.id)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync action ${action.actionType}", e)
                dao.incrementActionRetryCount(action.id)
                addSyncError("action", "${action.actionType}: ${e.message}", true)
            }
        }
        
        return syncedCount
    }
    
    /**
     * Process a single action
     */
    private suspend fun processAction(action: PendingAction): Boolean {
        // In production, this would call appropriate SupabaseService methods
        // based on the action type
        
        return when (action.actionType) {
            ActionType.TRIP_START -> {
                Log.d(TAG, "Processing TRIP_START for ${action.bookingId}")
                true
            }
            ActionType.TRIP_COMPLETE -> {
                Log.d(TAG, "Processing TRIP_COMPLETE for ${action.bookingId}")
                true
            }
            ActionType.STATUS_UPDATE -> {
                Log.d(TAG, "Processing STATUS_UPDATE for ${action.bookingId}")
                true
            }
            ActionType.OTP_VERIFY -> {
                Log.d(TAG, "Processing OTP_VERIFY for ${action.bookingId}")
                true
            }
            ActionType.CANCEL_BOOKING -> {
                Log.d(TAG, "Processing CANCEL_BOOKING for ${action.bookingId}")
                true
            }
            ActionType.SOS_ALERT -> {
                Log.d(TAG, "Processing SOS_ALERT for ${action.bookingId}")
                true
            }
            ActionType.LOCATION_UPDATE -> {
                Log.d(TAG, "Processing LOCATION_UPDATE for ${action.bookingId}")
                true
            }
        }
    }
    
    /**
     * Add sync error
     */
    private fun addSyncError(type: String, message: String, retryable: Boolean) {
        val error = SyncError(
            timestamp = System.currentTimeMillis(),
            type = type,
            message = message,
            retryable = retryable
        )
        
        _syncErrors.value = (_syncErrors.value + error).takeLast(10)
    }
    
    /**
     * Clear sync errors
     */
    fun clearErrors() {
        _syncErrors.value = emptyList()
    }
    
    /**
     * Set sync interval
     */
    fun setSyncInterval(intervalMs: Long) {
        syncIntervalMs = intervalMs
        if (isRunning) {
            startPeriodicSync()
        }
    }
    
    /**
     * Set batch size
     */
    fun setBatchSize(size: Int) {
        batchSize = size
    }
    
    /**
     * Get pending counts
     */
    suspend fun getPendingCounts(): PendingCounts {
        return PendingCounts(
            gpsCount = dao.getPendingGpsCount(),
            actionCount = dao.getPendingActionsCount()
        )
    }
    
    data class PendingCounts(
        val gpsCount: Int,
        val actionCount: Int
    ) {
        val total: Int get() = gpsCount + actionCount
    }
    
    /**
     * Release resources
     */
    fun release() {
        stop()
        scope.cancel()
    }
    
    companion object {
        private const val TAG = "SyncService"
        
        @Volatile
        private var instance: SyncService? = null
        
        fun getInstance(context: Context): SyncService {
            return instance ?: synchronized(this) {
                instance ?: SyncService(context.applicationContext).also { instance = it }
            }
        }
    }
}
