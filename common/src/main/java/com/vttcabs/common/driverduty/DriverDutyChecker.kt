package com.vttcabs.common.driverduty

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Driver Duty System
 * Performs all required checks before allowing driver to go online
 */
class DriverDutyChecker(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    private val _checkResults = MutableStateFlow(DutyCheckResults())
    val checkResults: StateFlow<DutyCheckResults> = _checkResults.asStateFlow()
    
    private val _canGoOnline = MutableStateFlow(false)
    val canGoOnline: StateFlow<Boolean> = _canGoOnline.asStateFlow()
    
    /**
     * All checks required before going online
     */
    data class DutyCheckResults(
        val selfieVerified: CheckResult = CheckResult.NOT_CHECKED,
        val gpsEnabled: CheckResult = CheckResult.NOT_CHECKED,
        val internetConnected: CheckResult = CheckResult.NOT_CHECKED,
        val locationPermission: CheckResult = CheckResult.NOT_CHECKED,
        val vehicleDocuments: VehicleDocumentsStatus = VehicleDocumentsStatus(),
        val drivingLicense: DocumentStatus = DocumentStatus(),
        val rcStatus: DocumentStatus = DocumentStatus(),
        val insuranceStatus: DocumentStatus = DocumentStatus(),
        val pucStatus: DocumentStatus = DocumentStatus(),
        val overallStatus: OverallStatus = OverallStatus.UNKNOWN
    )
    
    data class VehicleDocumentsStatus(
        val isValid: Boolean = false,
        val rcExpiry: DocumentStatus = DocumentStatus(),
        val insuranceExpiry: DocumentStatus = DocumentStatus(),
        val pucExpiry: DocumentStatus = DocumentStatus(),
        val message: String = ""
    )
    
    data class DocumentStatus(
        val isValid: Boolean = false,
        val expiryDate: String? = null,
        val daysUntilExpiry: Int = 0,
        val isExpired: Boolean = false,
        val isExpiringSoon: Boolean = false, // Within 30 days
        val message: String = ""
    )
    
    enum class CheckResult {
        PASSED,
        FAILED,
        NOT_CHECKED
    }
    
    enum class OverallStatus {
        READY,           // All checks passed
        NOT_READY,       // Some checks failed
        WARNING,         // All passed but some expiring soon
        UNKNOWN
    }
    
    /**
     * Perform all pre-online checks
     */
    suspend fun performAllChecks(driverId: String, vehicleId: String): DutyCheckResults {
        Log.d(TAG, "Performing all duty checks for driver: $driverId")
        
        val results = DutyCheckResults(
            selfieVerified = checkSelfieVerification(driverId),
            gpsEnabled = checkGpsEnabled(),
            internetConnected = checkInternetConnection(),
            locationPermission = checkLocationPermission(),
            vehicleDocuments = checkVehicleDocuments(vehicleId),
            drivingLicense = DocumentStatus(), // Will be populated from driver data
            rcStatus = DocumentStatus(),
            insuranceStatus = DocumentStatus(),
            pucStatus = DocumentStatus()
        )
        
        _checkResults.value = results
        
        // Determine overall status
        val overallStatus = calculateOverallStatus(results)
        _checkResults.value = results.copy(overallStatus = overallStatus)
        
        _canGoOnline.value = overallStatus == OverallStatus.READY
        
        Log.d(TAG, "Duty check complete. Can go online: ${_canGoOnline.value}")
        return _checkResults.value
    }
    
    /**
     * Check 1: Selfie Verification
     */
    private suspend fun checkSelfieVerification(driverId: String): CheckResult {
        return try {
            // In production, check against stored selfie
            // val driver = supabaseService.getDriver(driverId)
            // if (driver.selfieVerified) CheckResult.PASSED else CheckResult.FAILED
            
            // For demo, check local storage
            val selfieVerified = isSelfieVerified(driverId)
            if (selfieVerified) CheckResult.PASSED else CheckResult.FAILED
        } catch (e: Exception) {
            Log.e(TAG, "Selfie check failed", e)
            CheckResult.FAILED
        }
    }
    
    /**
     * Check 2: GPS Enabled
     */
    private fun checkGpsEnabled(): CheckResult {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        return if (isGpsEnabled || isNetworkEnabled) {
            CheckResult.PASSED
        } else {
            CheckResult.FAILED
        }
    }
    
    /**
     * Check 3: Internet Connection
     */
    private fun checkInternetConnection(): CheckResult {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return CheckResult.FAILED
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return CheckResult.FAILED
        
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        return if (hasInternet && isValidated) {
            CheckResult.PASSED
        } else {
            CheckResult.FAILED
        }
    }
    
    /**
     * Check 4: Location Permission
     */
    private fun checkLocationPermission(): CheckResult {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return if (fineLocation && coarseLocation) {
            CheckResult.PASSED
        } else {
            CheckResult.FAILED
        }
    }
    
    /**
     * Check 5-9: Vehicle Documents
     */
    private suspend fun checkVehicleDocuments(vehicleId: String): VehicleDocumentsStatus {
        return try {
            // In production, fetch from Supabase
            // val vehicle = supabaseService.getVehicle(vehicleId)
            
            // For demo, use stored values
            val today = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // Check RC
            val rcExpiry = dateFormat.parse("2025-12-31") ?: today
            val rcDaysUntil = ((rcExpiry.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            
            // Check Insurance
            val insuranceExpiry = dateFormat.parse("2025-06-30") ?: today
            val insuranceDaysUntil = ((insuranceExpiry.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            
            // Check PUC
            val pucExpiry = dateFormat.parse("2024-12-31") ?: today
            val pucDaysUntil = ((pucExpiry.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            
            val rcValid = rcDaysUntil > 0
            val insuranceValid = insuranceDaysUntil > 0
            val pucValid = pucDaysUntil > 0
            
            VehicleDocumentsStatus(
                isValid = rcValid && insuranceValid && pucValid,
                rcExpiry = DocumentStatus(
                    isValid = rcValid,
                    expiryDate = "2025-12-31",
                    daysUntilExpiry = rcDaysUntil,
                    isExpired = rcDaysUntil <= 0,
                    isExpiringSoon = rcDaysUntil in 1..30,
                    message = if (rcDaysUntil <= 0) "RC has expired"
                    else if (rcDaysUntil <= 30) "RC expires in $rcDaysUntil days"
                    else "RC valid"
                ),
                insuranceExpiry = DocumentStatus(
                    isValid = insuranceValid,
                    expiryDate = "2025-06-30",
                    daysUntilExpiry = insuranceDaysUntil,
                    isExpired = insuranceDaysUntil <= 0,
                    isExpiringSoon = insuranceDaysUntil in 1..30,
                    message = if (insuranceDaysUntil <= 0) "Insurance has expired"
                    else if (insuranceDaysUntil <= 30) "Insurance expires in $insuranceDaysUntil days"
                    else "Insurance valid"
                ),
                pucExpiry = DocumentStatus(
                    isValid = pucValid,
                    expiryDate = "2024-12-31",
                    daysUntilExpiry = pucDaysUntil,
                    isExpired = pucDaysUntil <= 0,
                    isExpiringSoon = pucDaysUntil in 1..30,
                    message = if (pucDaysUntil <= 0) "PUC has expired"
                    else if (pucDaysUntil <= 30) "PUC expires in $pucDaysUntil days"
                    else "PUC valid"
                ),
                message = when {
                    !rcValid -> "RC has expired"
                    !insuranceValid -> "Insurance has expired"
                    !pucValid -> "PUC has expired"
                    else -> "All vehicle documents valid"
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Vehicle document check failed", e)
            VehicleDocumentsStatus(isValid = false, message = "Failed to verify documents")
        }
    }
    
    /**
     * Check Driving License
     */
    suspend fun checkDrivingLicense(driverId: String): DocumentStatus {
        return try {
            // In production, fetch from Supabase
            val today = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val licenseExpiry = dateFormat.parse("2026-01-15") ?: today
            val daysUntil = ((licenseExpiry.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            
            DocumentStatus(
                isValid = daysUntil > 0,
                expiryDate = "2026-01-15",
                daysUntilExpiry = daysUntil,
                isExpired = daysUntil <= 0,
                isExpiringSoon = daysUntil in 1..30,
                message = if (daysUntil <= 0) "License has expired"
                else if (daysUntil <= 30) "License expires in $daysUntil days"
                else "License valid"
            )
        } catch (e: Exception) {
            Log.e(TAG, "License check failed", e)
            DocumentStatus(isValid = false, message = "Failed to verify license")
        }
    }
    
    /**
     * Calculate overall status
     */
    private fun calculateOverallStatus(results: DutyCheckResults): OverallStatus {
        val checks = listOf(
            results.selfieVerified == CheckResult.PASSED,
            results.gpsEnabled == CheckResult.PASSED,
            results.internetConnected == CheckResult.PASSED,
            results.locationPermission == CheckResult.PASSED,
            results.vehicleDocuments.isValid
        )
        
        val allPassed = checks.all { it }
        val anyWarning = results.vehicleDocuments.rcExpiry.isExpiringSoon ||
                        results.vehicleDocuments.insuranceExpiry.isExpiringSoon ||
                        results.vehicleDocuments.pucExpiry.isExpiringSoon
        
        return when {
            allPassed && anyWarning -> OverallStatus.WARNING
            allPassed -> OverallStatus.READY
            else -> OverallStatus.NOT_READY
        }
    }
    
    /**
     * Get list of failed checks
     */
    fun getFailedChecks(): List<String> {
        val results = _checkResults.value
        val failed = mutableListOf<String>()
        
        if (results.selfieVerified == CheckResult.FAILED) {
            failed.add("Selfie not verified. Please verify your selfie.")
        }
        if (results.gpsEnabled == CheckResult.FAILED) {
            failed.add("GPS is disabled. Please enable GPS.")
        }
        if (results.internetConnected == CheckResult.FAILED) {
            failed.add("No internet connection. Please check your network.")
        }
        if (results.locationPermission == CheckResult.FAILED) {
            failed.add("Location permission not granted.")
        }
        if (!results.vehicleDocuments.rcExpiry.isValid) {
            failed.add("RC has expired or is invalid.")
        }
        if (!results.vehicleDocuments.insuranceExpiry.isValid) {
            failed.add("Insurance has expired or is invalid.")
        }
        if (!results.vehicleDocuments.pucExpiry.isValid) {
            failed.add("PUC has expired or is invalid.")
        }
        
        return failed
    }
    
    /**
     * Get warnings (expiring soon)
     */
    fun getWarnings(): List<String> {
        val results = _checkResults.value
        val warnings = mutableListOf<String>()
        
        if (results.vehicleDocuments.rcExpiry.isExpiringSoon) {
            warnings.add("RC expires in ${results.vehicleDocuments.rcExpiry.daysUntilExpiry} days")
        }
        if (results.vehicleDocuments.insuranceExpiry.isExpiringSoon) {
            warnings.add("Insurance expires in ${results.vehicleDocuments.insuranceExpiry.daysUntilExpiry} days")
        }
        if (results.vehicleDocuments.pucExpiry.isExpiringSoon) {
            warnings.add("PUC expires in ${results.vehicleDocuments.pucExpiry.daysUntilExpiry} days")
        }
        
        return warnings
    }
    
    /**
     * Check if selfie is verified (stored locally for demo)
     */
    private fun isSelfieVerified(driverId: String): Boolean {
        // In production, check against server
        val prefs = context.getSharedPreferences("driver_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("selfie_verified_$driverId", false)
    }
    
    /**
     * Mark selfie as verified
     */
    fun markSelfieVerified(driverId: String, verified: Boolean) {
        val prefs = context.getSharedPreferences("driver_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("selfie_verified_$driverId", verified).apply()
    }
    
    companion object {
        private const val TAG = "DriverDutyChecker"
        
        @Volatile
        private var instance: DriverDutyChecker? = null
        
        fun getInstance(context: Context): DriverDutyChecker {
            return instance ?: synchronized(this) {
                instance ?: DriverDutyChecker(context.applicationContext).also { instance = it }
            }
        }
    }
}
