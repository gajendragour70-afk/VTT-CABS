package com.vttcabs.common.fleetowner

import android.content.Context
import android.util.Log
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Vehicle Maintenance System
 * Tracks document expirations and sends reminders
 */
class VehicleMaintenanceManager(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    private val _upcomingExpirations = MutableStateFlow<List<DocumentExpiry>>(emptyList())
    val upcomingExpirations: StateFlow<List<DocumentExpiry>> = _upcomingExpirations.asStateFlow()
    
    private val _serviceReminders = MutableStateFlow<List<ServiceReminder>>(emptyList())
    val serviceReminders: StateFlow<List<ServiceReminder>> = _serviceReminders.asStateFlow()
    
    // ==================== DATA CLASSES ====================
    
    data class DocumentExpiry(
        val id: String,
        val vehicleId: String,
        val vehicleNumber: String,
        val driverId: String?,
        val driverName: String?,
        val fleetOwnerId: String?,
        val documentType: DocumentType,
        val expiryDate: String,
        val daysUntilExpiry: Int,
        val isExpired: Boolean,
        val isExpiringSoon: Boolean,
        val reminderSent: Boolean,
        val notificationLevel: NotificationLevel
    )
    
    enum class DocumentType {
        INSURANCE,
        PUC,
        RC,
        LICENSE,
        FITNESS_CERTIFICATE
    }
    
    enum class NotificationLevel {
        GREEN,   // > 30 days
        YELLOW,  // 15-30 days
        ORANGE,  // 7-14 days
        RED,     // 1-6 days
        EXPIRED  // 0 or negative
    }
    
    data class ServiceReminder(
        val id: String,
        val vehicleId: String,
        val vehicleNumber: String,
        val driverId: String?,
        val driverName: String?,
        val reminderType: ServiceType,
        val dueDate: String,
        val dueKms: Int?,
        val currentKms: Int?,
        val isOverdue: Boolean,
        val reminderSent: Boolean
    )
    
    enum class ServiceType {
        ROUTINE_SERVICE,
        OIL_CHANGE,
        TIRE_CHANGE,
        BRAKE_CHECK,
        FULL_INSPECTION,
        OTHER
    }
    
    data class ReminderNotification(
        val recipientType: RecipientType,
        val recipientId: String,
        val vehicleId: String,
        val vehicleNumber: String,
        val message: String,
        val documentType: DocumentType?,
        val serviceType: ServiceType?,
        val daysUntilExpiry: Int?,
        val sentAt: Long
    )
    
    enum class RecipientType {
        DRIVER,
        FLEET_OWNER,
        ADMIN
    }
    
    // ==================== DOCUMENT TRACKING ====================
    
    /**
     * Check all vehicle documents for upcoming expirations
     */
    suspend fun checkAllDocumentExpirations(): List<DocumentExpiry> {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expirations = mutableListOf<DocumentExpiry>()
        
        try {
            // In production, fetch all vehicles and check documents
            // For demo, create sample data
            
            val sampleDocuments = listOf(
                DocumentCheck("V001", "KA-01-AB-1234", "D001", "Ramesh Kumar", "FO001",
                    DocumentType.INSURANCE, "2025-06-30"),
                DocumentCheck("V001", "KA-01-AB-1234", "D001", "Ramesh Kumar", "FO001",
                    DocumentType.PUC, "2024-12-31"),
                DocumentCheck("V002", "KA-02-CD-5678", "D002", "Suresh Patel", "FO001",
                    DocumentType.INSURANCE, "2025-03-15"),
                DocumentCheck("V002", "KA-02-CD-5678", "D002", "Suresh Patel", "FO001",
                    DocumentType.RC, "2025-08-20"),
                DocumentCheck("V003", "KA-03-EF-9012", "D003", "Mahesh Singh", "FO001",
                    DocumentType.INSURANCE, "2025-01-10"),
                DocumentCheck("V003", "KA-03-EF-9012", "D003", "Mahesh Singh", "FO001",
                    DocumentType.PUC, "2024-11-30"),
                DocumentCheck("D004", "KA-04-GH-3456", "D004", "Rajesh Gupta", "FO001",
                    DocumentType.LICENSE, "2026-01-15"),
                DocumentCheck("V005", "KA-05-IJ-7890", "D005", "Amit Kumar", "FO001",
                    DocumentType.INSURANCE, "2025-02-28")
            )
            
            sampleDocuments.forEach { doc ->
                try {
                    val expiryDate = dateFormat.parse(doc.expiryDate) ?: today
                    val daysUntil = ((expiryDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                    
                    expirations.add(
                        DocumentExpiry(
                            id = "${doc.id}_${doc.type.name}",
                            vehicleId = doc.vehicleId,
                            vehicleNumber = doc.vehicleNumber,
                            driverId = doc.driverId,
                            driverName = doc.driverName,
                            fleetOwnerId = doc.fleetOwnerId,
                            documentType = doc.type,
                            expiryDate = doc.expiryDate,
                            daysUntilExpiry = daysUntil,
                            isExpired = daysUntil <= 0,
                            isExpiringSoon = daysUntil in 1..30,
                            reminderSent = daysUntil > 60,
                            notificationLevel = getNotificationLevel(daysUntil)
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date for ${doc.vehicleId}", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check document expirations", e)
        }
        
        _upcomingExpirations.value = expirations.sortedBy { it.daysUntilExpiry }
        return _upcomingExpirations.value
    }
    
    private data class DocumentCheck(
        val id: String,
        val vehicleNumber: String,
        val driverId: String,
        val driverName: String,
        val fleetOwnerId: String,
        val type: DocumentType,
        val expiryDate: String
    )
    
    /**
     * Get notification level based on days until expiry
     */
    private fun getNotificationLevel(daysUntil: Int): NotificationLevel {
        return when {
            daysUntil <= 0 -> NotificationLevel.EXPIRED
            daysUntil <= 6 -> NotificationLevel.RED
            daysUntil <= 14 -> NotificationLevel.ORANGE
            daysUntil <= 30 -> NotificationLevel.YELLOW
            else -> NotificationLevel.GREEN
        }
    }
    
    // ==================== REMINDER SYSTEM ====================
    
    /**
     * Send reminders for expiring documents
     */
    suspend fun sendDocumentReminders(): List<ReminderNotification> {
        val notifications = mutableListOf<ReminderNotification>()
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val expirations = checkAllDocumentExpirations()
        
        expirations.forEach { expiry ->
            if (expiry.daysUntilExpiry <= 30 && !expiry.reminderSent) {
                // Send to Driver
                expiry.driverId?.let { driverId ->
                    notifications.add(createDriverNotification(expiry))
                }
                
                // Send to Fleet Owner
                expiry.fleetOwnerId?.let { fleetId ->
                    notifications.add(createFleetOwnerNotification(expiry))
                }
                
                // Send to Admin if critical
                if (expiry.daysUntilExpiry <= 7) {
                    notifications.add(createAdminNotification(expiry))
                }
            }
        }
        
        return notifications
    }
    
    private fun createDriverNotification(expiry: DocumentExpiry): ReminderNotification {
        val message = when (expiry.documentType) {
            DocumentType.INSURANCE -> "Your vehicle insurance expires in ${expiry.daysUntilExpiry} days. Please renew."
            DocumentType.PUC -> "Your PUC certificate expires in ${expiry.daysUntilExpiry} days. Please renew."
            DocumentType.RC -> "Your RC expires in ${expiry.daysUntilExpiry} days. Please renew."
            DocumentType.LICENSE -> "Your driving license expires in ${expiry.daysUntilExpiry} days. Please renew."
            DocumentType.FITNESS_CERTIFICATE -> "Your fitness certificate expires in ${expiry.daysUntilExpiry} days."
        }
        
        return ReminderNotification(
            recipientType = RecipientType.DRIVER,
            recipientId = expiry.driverId ?: "",
            vehicleId = expiry.vehicleId,
            vehicleNumber = expiry.vehicleNumber,
            message = message,
            documentType = expiry.documentType,
            serviceType = null,
            daysUntilExpiry = expiry.daysUntilExpiry,
            sentAt = System.currentTimeMillis()
        )
    }
    
    private fun createFleetOwnerNotification(expiry: DocumentExpiry): ReminderNotification {
        val message = "Vehicle ${expiry.vehicleNumber} has ${expiry.documentType.name.lowercase()} expiring in ${expiry.daysUntilExpiry} days."
        
        return ReminderNotification(
            recipientType = RecipientType.FLEET_OWNER,
            recipientId = expiry.fleetOwnerId ?: "",
            vehicleId = expiry.vehicleId,
            vehicleNumber = expiry.vehicleNumber,
            message = message,
            documentType = expiry.documentType,
            serviceType = null,
            daysUntilExpiry = expiry.daysUntilExpiry,
            sentAt = System.currentTimeMillis()
        )
    }
    
    private fun createAdminNotification(expiry: DocumentExpiry): ReminderNotification {
        val message = "URGENT: Vehicle ${expiry.vehicleNumber} (Driver: ${expiry.driverName}) has ${expiry.documentType.name.lowercase()} expiring in ${expiry.daysUntilExpiry} days!"
        
        return ReminderNotification(
            recipientType = RecipientType.ADMIN,
            recipientId = "admin",
            vehicleId = expiry.vehicleId,
            vehicleNumber = expiry.vehicleNumber,
            message = message,
            documentType = expiry.documentType,
            serviceType = null,
            daysUntilExpiry = expiry.daysUntilExpiry,
            sentAt = System.currentTimeMillis()
        )
    }
    
    // ==================== SERVICE REMINDERS ====================
    
    /**
     * Check for upcoming service due dates
     */
    suspend fun checkServiceReminders(): List<ServiceReminder> {
        val reminders = mutableListOf<ServiceReminder>()
        
        try {
            // In production, query service records
            // For demo, create sample data
            
            reminders.addAll(listOf(
                ServiceReminder(
                    id = "SR001",
                    vehicleId = "V001",
                    vehicleNumber = "KA-01-AB-1234",
                    driverId = "D001",
                    driverName = "Ramesh Kumar",
                    reminderType = ServiceType.OIL_CHANGE,
                    dueDate = "2024-02-01",
                    dueKms = 5000,
                    currentKms = 4850,
                    isOverdue = false,
                    reminderSent = false
                ),
                ServiceReminder(
                    id = "SR002",
                    vehicleId = "V002",
                    vehicleNumber = "KA-02-CD-5678",
                    driverId = "D002",
                    driverName = "Suresh Patel",
                    reminderType = ServiceType.ROUTINE_SERVICE,
                    dueDate = "2024-01-15",
                    dueKms = 10000,
                    currentKms = 10500,
                    isOverdue = true,
                    reminderSent = false
                ),
                ServiceReminder(
                    id = "SR003",
                    vehicleId = "V003",
                    vehicleNumber = "KA-03-EF-9012",
                    driverId = "D003",
                    driverName = "Mahesh Singh",
                    reminderType = ServiceType.TIRE_CHANGE,
                    dueDate = "2024-03-01",
                    dueKms = 15000,
                    currentKms = 14800,
                    isOverdue = false,
                    reminderSent = false
                )
            ))
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check service reminders", e)
        }
        
        _serviceReminders.value = reminders.sortedBy { 
            if (it.isOverdue) 0 else 1 
        }
        return _serviceReminders.value
    }
    
    /**
     * Schedule a service reminder
     */
    suspend fun scheduleServiceReminder(
        vehicleId: String,
        serviceType: ServiceType,
        dueDate: String? = null,
        dueKms: Int? = null
    ): Result<String> {
        return try {
            val reminderData = mapOf(
                "vehicle_id" to vehicleId,
                "service_type" to serviceType.name,
                "due_date" to (dueDate ?: ""),
                "due_kms" to (dueKms ?: 0),
                "is_completed" to false,
                "created_at" to System.currentTimeMillis()
            )
            // In production: supabaseService.insert("service_reminders", reminderData)
            Log.d(TAG, "Service reminder scheduled for vehicle: $vehicleId")
            Result.success("Reminder scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule service reminder", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark service as completed
     */
    suspend fun markServiceCompleted(
        reminderId: String,
        completedKms: Int,
        notes: String? = null
    ): Result<Unit> {
        return try {
            val updateData = mapOf(
                "is_completed" to true,
                "completed_kms" to completedKms,
                "completed_at" to System.currentTimeMillis(),
                "notes" to (notes ?: "")
            )
            // In production: supabaseService.update("service_reminders", updateData, reminderId)
            
            // Schedule next reminder
            Log.d(TAG, "Service marked completed: $reminderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark service completed", e)
            Result.failure(e)
        }
    }
    
    // ==================== SUMMARY ====================
    
    /**
     * Get maintenance summary for admin dashboard
     */
    suspend fun getMaintenanceSummary(): MaintenanceSummary {
        val expirations = checkAllDocumentExpirations()
        val services = checkServiceReminders()
        
        return MaintenanceSummary(
            totalExpiringDocuments = expirations.count { it.isExpiringSoon || it.isExpired },
            expiredDocuments = expirations.count { it.isExpired },
            expiringWithin7Days = expirations.count { it.daysUntilExpiry in 1..7 },
            expiringWithin30Days = expirations.count { it.daysUntilExpiry in 1..30 },
            overdueServices = services.count { it.isOverdue },
            upcomingServices = services.count { !it.isOverdue }
        )
    }
    
    data class MaintenanceSummary(
        val totalExpiringDocuments: Int,
        val expiredDocuments: Int,
        val expiringWithin7Days: Int,
        val expiringWithin30Days: Int,
        val overdueServices: Int,
        val upcomingServices: Int
    )
    
    companion object {
        private const val TAG = "VehicleMaintenance"
        
        @Volatile
        private var instance: VehicleMaintenanceManager? = null
        
        fun getInstance(context: Context): VehicleMaintenanceManager {
            return instance ?: synchronized(this) {
                instance ?: VehicleMaintenanceManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
