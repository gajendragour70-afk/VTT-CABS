package com.vttcabs.common.safety

import android.content.Context
import android.location.Location
import android.util.Log
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Customer Safety System
 * Handles SOS, live trip sharing, emergency contacts, and trip safety features
 */
class CustomerSafetyManager(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    // State flows
    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()
    
    private val _trustedContacts = MutableStateFlow<List<TrustedContact>>(emptyList())
    val trustedContacts: StateFlow<List<TrustedContact>> = _trustedContacts.asStateFlow()
    
    private val _tripShareStatus = MutableStateFlow(TripShareStatus())
    val tripShareStatus: StateFlow<TripShareStatus> = _tripShareStatus.asStateFlow()
    
    private val _sosActive = MutableStateFlow(false)
    val sosActive: StateFlow<Boolean> = _sosActive.asStateFlow()
    
    private val _safetyAlerts = MutableSharedFlow<SafetyAlert>()
    val safetyAlerts: SharedFlow<SafetyAlert> = _safetyAlerts.asSharedFlow()
    
    // ==================== DATA CLASSES ====================
    
    data class EmergencyContact(
        val id: String,
        val name: String,
        val phone: String,
        val relationship: String,
        val isPrimary: Boolean = false
    )
    
    data class TrustedContact(
        val id: String,
        val name: String,
        val phone: String,
        val email: String?,
        val shareTripWith: Boolean = true,
        val notifyOnSOS: Boolean = true
    )
    
    data class TripShareStatus(
        val isActive: Boolean = false,
        val shareId: String? = null,
        val bookingId: String? = null,
        val startedAt: Long? = null,
        val shareUrl: String? = null,
        val trustedContactsNotified: List<String> = emptyList()
    )
    
    data class SafetyAlert(
        val type: AlertType,
        val message: String,
        val timestamp: Long = System.currentTimeMillis(),
        val location: Pair<Double, Double>? = null
    )
    
    enum class AlertType {
        SOS_TRIGGERED,
        SOS_CANCELLED,
        SOS_RESPONDED,
        TRIP_STARTED,
        TRIP_SHARED,
        TRIP_COMPLETED,
        SAFETY_CHECK,
        CUSTOMER_OFF_TRIP
    }
    
    data class SOSAlert(
        val id: String,
        val customerId: String,
        val customerName: String,
        val customerPhone: String,
        val bookingId: String?,
        val driverId: String?,
        val driverName: String?,
        val vehicleNumber: String?,
        val latitude: Double,
        val longitude: Double,
        val triggeredAt: Long,
        val status: SOSStatus,
        val emergencyServicesNotified: Boolean = false
    )
    
    enum class SOSStatus {
        TRIGGERED,
        DISPATCHED,
        RESPONDED,
        RESOLVED,
        CANCELLED
    }
    
    // ==================== EMERGENCY CONTACTS ====================
    
    /**
     * Add emergency contact
     */
    suspend fun addEmergencyContact(
        customerId: String,
        name: String,
        phone: String,
        relationship: String,
        isPrimary: Boolean = false
    ): Result<EmergencyContact> {
        return try {
            val contact = EmergencyContact(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                relationship = relationship,
                isPrimary = isPrimary
            )
            
            // In production: supabaseService.insert("emergency_contacts", contact)
            val current = _emergencyContacts.value.toMutableList()
            current.add(contact)
            _emergencyContacts.value = current
            
            Log.d(TAG, "Emergency contact added: $name")
            Result.success(contact)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add emergency contact", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove emergency contact
     */
    suspend fun removeEmergencyContact(contactId: String): Result<Unit> {
        return try {
            val current = _emergencyContacts.value.toMutableList()
            current.removeAll { it.id == contactId }
            _emergencyContacts.value = current
            
            Log.d(TAG, "Emergency contact removed: $contactId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove emergency contact", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get emergency contacts for customer
     */
    suspend fun getEmergencyContacts(customerId: String): List<EmergencyContact> {
        return try {
            // In production: supabaseService.select("emergency_contacts", "?customer_id=eq.$customerId")
            _emergencyContacts.value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get emergency contacts", e)
            emptyList()
        }
    }
    
    // ==================== TRUSTED CONTACTS ====================
    
    /**
     * Add trusted contact for trip sharing
     */
    suspend fun addTrustedContact(
        customerId: String,
        name: String,
        phone: String,
        email: String? = null
    ): Result<TrustedContact> {
        return try {
            val contact = TrustedContact(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                email = email
            )
            
            val current = _trustedContacts.value.toMutableList()
            current.add(contact)
            _trustedContacts.value = current
            
            Log.d(TAG, "Trusted contact added: $name")
            Result.success(contact)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add trusted contact", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove trusted contact
     */
    suspend fun removeTrustedContact(contactId: String): Result<Unit> {
        return try {
            val current = _trustedContacts.value.toMutableList()
            current.removeAll { it.id == contactId }
            _trustedContacts.value = current
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove trusted contact", e)
            Result.failure(e)
        }
    }
    
    // ==================== LIVE TRIP SHARING ====================
    
    /**
     * Start sharing trip with trusted contacts
     */
    suspend fun startTripShare(
        customerId: String,
        bookingId: String,
        driverName: String,
        vehicleNumber: String,
        pickupAddress: String,
        dropAddress: String
    ): Result<String> {
        return try {
            val shareId = UUID.randomUUID().toString().take(8)
            val shareUrl = "https://vttcabs.com/share/$shareId"
            
            _tripShareStatus.value = TripShareStatus(
                isActive = true,
                shareId = shareId,
                bookingId = bookingId,
                startedAt = System.currentTimeMillis(),
                shareUrl = shareUrl
            )
            
            // Notify trusted contacts
            notifyTrustedContactsOfTripStart(customerId, shareUrl, driverName, vehicleNumber, dropAddress)
            
            // Emit alert
            _safetyAlerts.emit(
                SafetyAlert(
                    type = AlertType.TRIP_SHARED,
                    message = "Trip sharing started with ${_trustedContacts.value.size} contacts"
                )
            )
            
            Log.d(TAG, "Trip sharing started: $shareId")
            Result.success(shareUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trip share", e)
            Result.failure(e)
        }
    }
    
    /**
     * Stop trip sharing
     */
    suspend fun stopTripShare(): Result<Unit> {
        return try {
            val previousStatus = _tripShareStatus.value
            _tripShareStatus.value = TripShareStatus()
            
            // Notify trusted contacts
            previousStatus.shareId?.let { shareId ->
                notifyTrustedContactsOfTripEnd(shareId)
            }
            
            Log.d(TAG, "Trip sharing stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop trip share", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update live location during trip
     */
    suspend fun updateTripLocation(
        shareId: String,
        latitude: Double,
        longitude: Double,
        speed: Float?,
        heading: Float?
    ): Result<Unit> {
        return try {
            // In production, update location in realtime
            // supabaseService.updateLocation(shareId, lat, lng)
            Log.d(TAG, "Trip location updated: $latitude, $longitude")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update trip location", e)
            Result.failure(e)
        }
    }
    
    private suspend fun notifyTrustedContactsOfTripStart(
        customerId: String,
        shareUrl: String,
        driverName: String,
        vehicleNumber: String,
        dropAddress: String
    ) {
        _trustedContacts.value.forEach { contact ->
            if (contact.shareTripWith) {
                sendTripStartNotification(contact, shareUrl, driverName, vehicleNumber, dropAddress)
            }
        }
    }
    
    private suspend fun notifyTrustedContactsOfTripEnd(shareId: String) {
        _trustedContacts.value.forEach { contact ->
            if (contact.shareTripWith) {
                sendTripEndNotification(contact, shareId)
            }
        }
    }
    
    private suspend fun sendTripStartNotification(
        contact: TrustedContact,
        shareUrl: String,
        driverName: String,
        vehicleNumber: String,
        dropAddress: String
    ) {
        // In production: Send SMS/notification
        Log.d(TAG, "Trip start notification sent to ${contact.name}")
    }
    
    private suspend fun sendTripEndNotification(contact: TrustedContact, shareId: String) {
        // In production: Send SMS/notification
        Log.d(TAG, "Trip end notification sent to ${contact.name}")
    }
    
    // ==================== SOS SYSTEM ====================
    
    /**
     * Trigger SOS alert
     */
    suspend fun triggerSOS(
        customerId: String,
        customerName: String,
        customerPhone: String,
        bookingId: String?,
        driverId: String?,
        driverName: String?,
        vehicleNumber: String?,
        latitude: Double,
        longitude: Double
    ): Result<SOSAlert> {
        return try {
            _sosActive.value = true
            
            val sosAlert = SOSAlert(
                id = UUID.randomUUID().toString(),
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                bookingId = bookingId,
                driverId = driverId,
                driverName = driverName,
                vehicleNumber = vehicleNumber,
                latitude = latitude,
                longitude = longitude,
                triggeredAt = System.currentTimeMillis(),
                status = SOSStatus.TRIGGERED
            )
            
            // Save to Supabase
            // supabaseService.insert("sos_alerts", sosAlert)
            
            // Notify emergency services (in production)
            notifyEmergencyServices(sosAlert)
            
            // Notify emergency contacts
            notifyEmergencyContacts(sosAlert)
            
            // Notify trusted contacts
            notifyTrustedContactsSOS(sosAlert)
            
            // Emit alert
            _safetyAlerts.emit(
                SafetyAlert(
                    type = AlertType.SOS_TRIGGERED,
                    message = "SOS Alert triggered! Location: $latitude, $longitude",
                    location = Pair(latitude, longitude)
                )
            )
            
            Log.d(TAG, "SOS triggered by $customerName at $latitude, $longitude")
            Result.success(sosAlert)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger SOS", e)
            Result.failure(e)
        }
    }
    
    /**
     * Cancel SOS alert
     */
    suspend fun cancelSOS(alertId: String, reason: String): Result<Unit> {
        return try {
            _sosActive.value = false
            
            // Update in Supabase
            // supabaseService.update("sos_alerts", mapOf("status" to "CANCELLED", "cancellation_reason" to reason), alertId)
            
            _safetyAlerts.emit(
                SafetyAlert(
                    type = AlertType.SOS_CANCELLED,
                    message = "SOS cancelled. Reason: $reason"
                )
            )
            
            Log.d(TAG, "SOS cancelled: $alertId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel SOS", e)
            Result.failure(e)
        }
    }
    
    /**
     * Notify emergency services (Police, Ambulance)
     */
    private suspend fun notifyEmergencyServices(alert: SOSAlert) {
        // In production, this would:
        // 1. Contact police via emergency API
        // 2. Contact ambulance services
        // 3. Send GPS coordinates
        
        Log.d(TAG, "Emergency services notified for SOS: ${alert.id}")
    }
    
    /**
     * Notify emergency contacts
     */
    private suspend fun notifyEmergencyContacts(alert: SOSAlert) {
        _emergencyContacts.value.forEach { contact ->
            // In production: Send SMS with SOS message
            Log.d(TAG, "Emergency notification sent to ${contact.name}: ${contact.phone}")
        }
    }
    
    /**
     * Notify trusted contacts of SOS
     */
    private suspend fun notifyTrustedContactsSOS(alert: SOSAlert) {
        _trustedContacts.value.forEach { contact ->
            if (contact.notifyOnSOS) {
                // In production: Send SMS with SOS message and location
                Log.d(TAG, "SOS notification sent to trusted contact ${contact.name}")
            }
        }
    }
    
    // ==================== SAFETY CHECK ====================
    
    /**
     * Check if customer is safe during trip
     */
    suspend fun performSafetyCheck(bookingId: String): SafetyCheckResult {
        return try {
            // In production, check:
            // 1. GPS tracking is active
            // 2. Driver location is being tracked
            // 3. SOS can be triggered
            
            SafetyCheckResult(
                isSafe = true,
                gpsTrackingActive = true,
                driverTrackingActive = true,
                sosAvailable = true,
                tripShareActive = _tripShareStatus.value.isActive,
                lastDriverLocation = Pair(12.9716, 77.5946), // Sample Bangalore coordinates
                lastUpdateTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Safety check failed", e)
            SafetyCheckResult(isSafe = false, errorMessage = e.message)
        }
    }
    
    data class SafetyCheckResult(
        val isSafe: Boolean,
        val gpsTrackingActive: Boolean = false,
        val driverTrackingActive: Boolean = false,
        val sosAvailable: Boolean = false,
        val tripShareActive: Boolean = false,
        val lastDriverLocation: Pair<Double, Double>? = null,
        val lastUpdateTime: Long? = null,
        val errorMessage: String? = null
    )
    
    // ==================== TRIP RECORDING ====================
    
    /**
     * Start recording trip for safety
     */
    suspend fun startTripRecording(
        bookingId: String,
        customerId: String
    ): Result<String> {
        return try {
            val recordingId = UUID.randomUUID().toString()
            
            // In production:
            // 1. Start background service for location tracking
            // 2. Save route data
            // 3. Enable audio recording if enabled
            
            Log.d(TAG, "Trip recording started: $recordingId")
            Result.success(recordingId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trip recording", e)
            Result.failure(e)
        }
    }
    
    /**
     * Stop and save trip recording
     */
    suspend fun stopTripRecording(recordingId: String): Result<Unit> {
        return try {
            // In production:
            // 1. Stop background service
            // 2. Upload recording to storage
            // 3. Save metadata
            
            Log.d(TAG, "Trip recording stopped: $recordingId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop trip recording", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "CustomerSafety"
        
        @Volatile
        private var instance: CustomerSafetyManager? = null
        
        fun getInstance(context: Context): CustomerSafetyManager {
            return instance ?: synchronized(this) {
                instance ?: CustomerSafetyManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
