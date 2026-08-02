package com.vttcabs.common.antifraud

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress
import java.util.*

/**
 * Anti-Fraud System
 * Detects fake GPS, multiple logins, rooted devices, emulators, and prevents fraud
 */
class AntiFraudManager(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    private val _deviceStatus = MutableStateFlow(DeviceSecurityStatus())
    val deviceStatus: StateFlow<DeviceSecurityStatus> = _deviceStatus.asStateFlow()
    
    private val _activeSessions = MutableStateFlow<List<UserSession>>(emptyList())
    val activeSessions: StateFlow<List<UserSession>> = _activeSessions.asStateFlow()
    
    private val _fraudAlerts = MutableStateFlow<List<FraudAlert>>(emptyList())
    val fraudAlerts: StateFlow<List<FraudAlert>> = _fraudAlerts.asStateFlow()
    
    // ==================== DATA CLASSES ====================
    
    data class DeviceSecurityStatus(
        val isSecure: Boolean = true,
        val isRooted: Boolean = false,
        val isEmulator: Boolean = false,
        val hasMockLocation: Boolean = false,
        val gpsAccuracyValid: Boolean = true,
        val issues: List<String> = emptyList()
    )
    
    data class UserSession(
        val sessionId: String,
        val userId: String,
        val deviceId: String,
        val deviceName: String,
        val ipAddress: String,
        val loginTime: Long,
        val lastActivity: Long,
        val isCurrent: Boolean,
        val location: Pair<Double, Double>?
    )
    
    data class FraudAlert(
        val id: String,
        val type: FraudType,
        val severity: FraudSeverity,
        val userId: String?,
        val description: String,
        val timestamp: Long,
        val resolved: Boolean = false,
        val actionTaken: String?
    )
    
    enum class FraudType {
        FAKE_GPS,
        MULTIPLE_LOGIN,
        ROOTED_DEVICE,
        EMULATOR,
        DUPLICATE_BOOKING,
        MULTIPLE_RIDE_ACCEPTANCE,
        SUSPICIOUS_LOCATION,
        RAPID_CANCELLATION,
        PAYMENT_FRAUD
    }
    
    enum class FraudSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    // ==================== DEVICE SECURITY CHECKS ====================
    
    /**
     * Perform all device security checks
     */
    fun performSecurityCheck(): DeviceSecurityStatus {
        val issues = mutableListOf<String>()
        var isSecure = true
        
        // Check 1: Rooted Device
        val isRooted = checkIsRooted()
        if (isRooted) {
            issues.add("Device appears to be rooted")
            isSecure = false
        }
        
        // Check 2: Emulator
        val isEmulator = checkIsEmulator()
        if (isEmulator) {
            issues.add("Running on emulator")
            isSecure = false
        }
        
        // Check 3: Mock Location
        val hasMockLocation = checkMockLocation()
        if (hasMockLocation) {
            issues.add("Mock location enabled")
            isSecure = false
        }
        
        val status = DeviceSecurityStatus(
            isSecure = isSecure,
            isRooted = isRooted,
            isEmulator = isEmulator,
            hasMockLocation = hasMockLocation,
            issues = issues
        )
        
        _deviceStatus.value = status
        return status
    }
    
    /**
     * Check if device is rooted
     */
    private fun checkIsRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        // Check for su binary
        for (path in paths) {
            if (java.io.File(path).exists()) {
                Log.w(TAG, "Rooted: Found $path")
                return true
            }
        }
        
        // Check build properties
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            Log.w(TAG, "Rooted: Test keys found")
            return true
        }
        
        // Check for root management apps
        val packageManager = context.packageManager
        val rootPackages = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk"
        )
        
        for (packageName in rootPackages) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                Log.w(TAG, "Rooted: Found root app $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Not found, continue
            }
        }
        
        return false
    }
    
    /**
     * Check if running on emulator
     */
    private fun checkIsEmulator(): Boolean {
        // Check hardware info
        val hardware = Build.HARDWARE
        if (hardware.contains("goldfish") || hardware.contains("ranchu")) {
            Log.w(TAG, "Emulator: goldfish/ranchu hardware")
            return true
        }
        
        // Check manufacturer
        val manufacturer = Build.MANUFACTURER.lowercase()
        if (manufacturer == "genymotion" || manufacturer == "generic") {
            Log.w(TAG, "Emulator: $manufacturer")
            return true
        }
        
        // Check model
        val model = Build.MODEL
        if (model.contains("Emulator") || model.contains("sdk") || model.contains("x86")) {
            Log.w(TAG, "Emulator: $model")
            return true
        }
        
        // Check for emulator-specific features
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            Log.w(TAG, "Emulator: No accelerometer")
            return true
        }
        
        // Check for Google Play Services
        try {
            val gms = context.packageManager.getPackageInfo("com.google.android.gms", 0)
            if (gms == null) {
                Log.w(TAG, "Emulator: No Google Play Services")
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Emulator: No Google Play Services")
            return true
        }
        
        return false
    }
    
    /**
     * Check if mock location is enabled
     */
    private fun checkMockLocation(): Boolean {
        // Check if developer options mock location is enabled
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } else {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ALLOW_MOCK_LOCATION
                )?.toBoolean() ?: false
            }
            
            if (isMock) {
                Log.w(TAG, "Mock location is enabled")
            }
            
            return isMock
        } catch (e: Exception) {
            Log.e(TAG, "Error checking mock location", e)
            return false
        }
    }
    
    // ==================== GPS SPOOFING DETECTION ====================
    
    /**
     * Detect fake GPS by analyzing location data
     */
    suspend fun detectFakeGPS(
        location: Location,
        previousLocations: List<Location>
    ): GPSAnalysisResult {
        val issues = mutableListOf<String>()
        var riskScore = 0
        
        // Check 1: Accuracy too good
        if (location.accuracy < 3) {
            // Very high accuracy might indicate spoofing
            issues.add("Unusually high GPS accuracy")
            riskScore += 10
        }
        
        // Check 2: Speed impossible
        if (previousLocations.isNotEmpty()) {
            val lastLocation = previousLocations.last()
            val distance = FloatArray(1)
            android.location.Location.distanceBetween(
                lastLocation.latitude, lastLocation.longitude,
                location.latitude, location.longitude,
                distance
            )
            val timeDiff = (location.time - lastLocation.time) / 1000f // seconds
            val speed = if (timeDiff > 0) distance[0] / timeDiff else 0f // m/s
            
            // Speed > 200 m/s (720 km/h) is impossible
            if (speed > 200) {
                issues.add("Impossible travel speed detected: ${speed * 3.6} km/h")
                riskScore += 50
            }
            
            // Check for teleportation
            if (distance[0] > 10000 && timeDiff < 60000) {
                issues.add("Possible teleportation: ${distance[0]}m in ${timeDiff}s")
                riskScore += 80
            }
        }
        
        // Check 3: Provider
        if (location.provider == "fake" || location.provider == "mock") {
            issues.add("Location from mock provider")
            riskScore += 100
        }
        
        // Check 4: Altitude anomalies
        if (location.hasAltitude()) {
            val altitude = location.altitude
            // Impossible altitudes
            if (altitude < -500 || altitude > 10000) {
                issues.add("Impossible altitude: ${altitude}m")
                riskScore += 30
            }
        }
        
        return GPSAnalysisResult(
            isSuspicious = riskScore > 30,
            riskScore = riskScore,
            issues = issues,
            recommendation = when {
                riskScore > 70 -> "BLOCK"
                riskScore > 30 -> "REVIEW"
                else -> "ALLOW"
            }
        )
    }
    
    data class GPSAnalysisResult(
        val isSuspicious: Boolean,
        val riskScore: Int,
        val issues: List<String>,
        val recommendation: String // ALLOW, REVIEW, BLOCK
    )
    
    // ==================== MULTIPLE LOGIN DETECTION ====================
    
    /**
     * Check for multiple active sessions
     */
    suspend fun checkMultipleLogins(userId: String, currentDeviceId: String): List<UserSession> {
        return try {
            // In production, query active sessions from Supabase
            // For demo, return empty
            val sessions = _activeSessions.value.filter { it.userId == userId }
            
            if (sessions.size > 1) {
                val fraudAlert = FraudAlert(
                    id = UUID.randomUUID().toString(),
                    type = FraudType.MULTIPLE_LOGIN,
                    severity = FraudSeverity.MEDIUM,
                    userId = userId,
                    description = "Multiple login detected: ${sessions.size} active sessions",
                    timestamp = System.currentTimeMillis(),
                    actionTaken = null
                )
                addFraudAlert(fraudAlert)
            }
            
            sessions
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check multiple logins", e)
            emptyList()
        }
    }
    
    /**
     * Terminate other sessions
     */
    suspend fun terminateOtherSessions(userId: String, keepSessionId: String): Result<Int> {
        return try {
            val toTerminate = _activeSessions.value.filter {
                it.userId == userId && it.sessionId != keepSessionId
            }
            
            toTerminate.forEach { session ->
                // In production: supabaseService.revokeSession(session.sessionId)
                Log.d(TAG, "Terminated session: ${session.sessionId}")
            }
            
            Result.success(toTerminate.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to terminate sessions", e)
            Result.failure(e)
        }
    }
    
    /**
     * Register new session
     */
    suspend fun registerSession(
        userId: String,
        deviceId: String,
        deviceName: String,
        ipAddress: String
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val session = UserSession(
            sessionId = sessionId,
            userId = userId,
            deviceId = deviceId,
            deviceName = deviceName,
            ipAddress = ipAddress,
            loginTime = System.currentTimeMillis(),
            lastActivity = System.currentTimeMillis(),
            isCurrent = true,
            location = null
        )
        
        val current = _activeSessions.value.toMutableList()
        // Mark old sessions as not current
        current.forEach { s ->
            if (s.userId == userId) {
                current[current.indexOf(s)] = s.copy(isCurrent = false)
            }
        }
        current.add(session)
        _activeSessions.value = current
        
        return sessionId
    }
    
    // ==================== DUPLICATE BOOKING PREVENTION ====================
    
    private val activeBookingAttempts = mutableMapOf<String, Long>()
    
    /**
     * Check for duplicate booking attempt
     */
    suspend fun checkDuplicateBooking(customerId: String): BookingCheckResult {
        val now = System.currentTimeMillis()
        
        // Clean old attempts (older than 5 minutes)
        activeBookingAttempts.entries.removeIf { (_, time) -> now - time > 300000 }
        
        if (activeBookingAttempts.containsKey(customerId)) {
            val lastAttempt = activeBookingAttempts[customerId]!!
            val timeSinceLast = now - lastAttempt
            
            if (timeSinceLast < 60000) { // Less than 1 minute
                val fraudAlert = FraudAlert(
                    id = UUID.randomUUID().toString(),
                    type = FraudType.DUPLICATE_BOOKING,
                    severity = FraudSeverity.LOW,
                    userId = customerId,
                    description = "Rapid booking attempt detected: ${timeSinceLast / 1000}s since last",
                    timestamp = now,
                    actionTaken = null
                )
                addFraudAlert(fraudAlert)
                
                return BookingCheckResult(
                    isAllowed = false,
                    reason = "Please wait ${60 - timeSinceLast / 1000} seconds before booking again"
                )
            }
        }
        
        activeBookingAttempts[customerId] = now
        return BookingCheckResult(isAllowed = true)
    }
    
    data class BookingCheckResult(
        val isAllowed: Boolean,
        val reason: String? = null
    )
    
    // ==================== MULTIPLE RIDE ACCEPTANCE ====================
    
    private var lastAcceptanceTime = 0L
    private var lastAcceptedBookingId: String? = null
    
    /**
     * Check for multiple ride acceptance
     */
    suspend fun checkMultipleRideAcceptance(driverId: String, bookingId: String): RideAcceptanceResult {
        val now = System.currentTimeMillis()
        
        // Check if driver already accepted a ride recently
        if (lastAcceptedBookingId != null && lastAcceptedBookingId != bookingId) {
            val timeSince = now - lastAcceptanceTime
            
            if (timeSince < 300000) { // Less than 5 minutes
                val fraudAlert = FraudAlert(
                    id = UUID.randomUUID().toString(),
                    type = FraudType.MULTIPLE_RIDE_ACCEPTANCE,
                    severity = FraudSeverity.HIGH,
                    userId = driverId,
                    description = "Driver accepted multiple rides within 5 minutes",
                    timestamp = now,
                    actionTaken = "BLOCKED"
                )
                addFraudAlert(fraudAlert)
                
                return RideAcceptanceResult(
                    isAllowed = false,
                    reason = "You already have an active ride"
                )
            }
        }
        
        lastAcceptanceTime = now
        lastAcceptedBookingId = bookingId
        return RideAcceptanceResult(isAllowed = true)
    }
    
    data class RideAcceptanceResult(
        val isAllowed: Boolean,
        val reason: String? = null
    )
    
    // ==================== FRAUD ALERTS ====================
    
    /**
     * Add fraud alert
     */
    private fun addFraudAlert(alert: FraudAlert) {
        val current = _fraudAlerts.value.toMutableList()
        current.add(0, alert) // Add to beginning
        _fraudAlerts.value = current.take(100) // Keep last 100
        
        // Log for monitoring
        Log.w(TAG, "FRAUD ALERT: ${alert.type} - ${alert.description}")
        
        // In production, send to monitoring system
    }
    
    /**
     * Get fraud alerts for admin
     */
    suspend fun getFraudAlerts(
        startDate: Long? = null,
        endDate: Long? = null,
        severity: FraudSeverity? = null
    ): List<FraudAlert> {
        return _fraudAlerts.value.filter { alert ->
            val byDate = if (startDate != null && endDate != null) {
                alert.timestamp in startDate..endDate
            } else true
            
            val bySeverity = severity == null || alert.severity == severity
            
            byDate && bySeverity
        }
    }
    
    /**
     * Resolve fraud alert
     */
    suspend fun resolveFraudAlert(alertId: String, action: String): Result<Unit> {
        return try {
            val current = _fraudAlerts.value.toMutableList()
            val index = current.indexOfFirst { it.id == alertId }
            
            if (index >= 0) {
                current[index] = current[index].copy(resolved = true, actionTaken = action)
                _fraudAlerts.value = current
            }
            
            Log.d(TAG, "Fraud alert resolved: $alertId - Action: $action")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve fraud alert", e)
            Result.failure(e)
        }
    }
    
    // ==================== DEVICE ID ====================
    
    /**
     * Get unique device ID
     */
    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return androidId ?: UUID.randomUUID().toString()
    }
    
    companion object {
        private const val TAG = "AntiFraud"
        
        @Volatile
        private var instance: AntiFraudManager? = null
        
        fun getInstance(context: Context): AntiFraudManager {
            return instance ?: synchronized(this) {
                instance ?: AntiFraudManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
