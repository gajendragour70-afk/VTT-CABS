package com.vttcabs.common.superadmin

import android.content.Context
import android.util.Log
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Super Admin System
 * Company owner access for complete platform management
 */
class SuperAdminManager(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    private val _systemStats = MutableStateFlow(SystemStats())
    val systemStats: StateFlow<SystemStats> = _systemStats.asStateFlow()
    
    private val _admins = MutableStateFlow<List<AdminAccount>>(emptyList())
    val admins: StateFlow<List<AdminAccount>> = _admins.asStateFlow()
    
    private val _systemSettings = MutableStateFlow(SystemSettings())
    val systemSettings: StateFlow<SystemSettings> = _systemSettings.asStateFlow()
    
    // ==================== DATA CLASSES ====================
    
    data class AdminAccount(
        val id: String,
        val email: String,
        val name: String,
        val role: AdminRole,
        val permissions: List<String>,
        val isActive: Boolean,
        val lastLogin: Long?,
        val createdAt: Long
    )
    
    enum class AdminRole {
        SUPER_ADMIN,      // Full access
        CITY_ADMIN,      // City-specific access
        SUPPORT_ADMIN,    // Support tasks
        FINANCE_ADMIN    // Financial management
    }
    
    data class SystemStats(
        val totalCustomers: Int = 0,
        val totalDrivers: Int = 0,
        val totalFleetOwners: Int = 0,
        val activeDrivers: Int = 0,
        val totalTrips: Int = 0,
        val tripsToday: Int = 0,
        val tripsThisMonth: Int = 0,
        val totalRevenue: Double = 0.0,
        val revenueToday: Double = 0.0,
        val revenueThisMonth: Double = 0.0,
        val commissionCollected: Double = 0.0,
        val pendingPayouts: Double = 0.0,
        val customerGrowth: Float = 0f,
        val driverGrowth: Float = 0f,
        val averageRating: Float = 0f
    )
    
    data class SystemSettings(
        val companyName: String = "VTT Cabs",
        val commissionPercent: Double = 15.0,
        val gstPercent: Double = 5.0,
        val minBookingAmount: Double = 50.0,
        val minDriverAge: Int = 21,
        val maxDriverAge: Int = 65,
        val rideRequestTimeout: Int = 30,
        val driverSearchRadius: Double = 5.0,
        val maxSearchRadius: Double = 20.0,
        val sosEmergencyNumber: String = "100",
        val supportEmail: String = "support@vttcabs.com",
        val supportPhone: String = "1800-XXX-XXXX",
        val maintenanceMode: Boolean = false
    )
    
    data class City(
        val id: String,
        val name: String,
        val state: String,
        val isActive: Boolean,
        val baseFare: Double,
        val perKmRate: Double,
        val minFare: Double,
        val activeDrivers: Int,
        val totalTrips: Int
    )
    
    data class PricingConfig(
        val cityId: String,
        val cityName: String,
        val vehicleCategory: String,
        val baseFare: Double,
        val perKm: Double,
        val perMin: Double,
        val minFare: Double,
        val surgeMultiplier: Double = 1.0
    )
    
    data class PromoCode(
        val id: String,
        val code: String,
        val description: String,
        val discountType: DiscountType,
        val discountValue: Double,
        val maxDiscount: Double?,
        val minOrderValue: Double?,
        val usageLimit: Int,
        val usedCount: Int,
        val validFrom: Long,
        val validTo: Long,
        val isActive: Boolean
    )
    
    enum class DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT,
        FREE_RIDE
    }
    
    data class CommissionReport(
        val period: String,
        val totalTrips: Int,
        val grossRevenue: Double,
        val commissionPercent: Double,
        val commissionAmount: Double,
        val gstAmount: Double,
        val netCommission: Double,
        val payoutToDrivers: Double,
        val payoutToFleetOwners: Double
    )
    
    // ==================== ADMIN MANAGEMENT ====================
    
    /**
     * Create new admin account
     */
    suspend fun createAdmin(
        email: String,
        name: String,
        role: AdminRole,
        permissions: List<String>
    ): Result<String> {
        return try {
            val adminData = mapOf(
                "email" to email,
                "full_name" to name,
                "role" to role.name,
                "permissions" to permissions.joinToString(","),
                "is_active" to true,
                "created_at" to System.currentTimeMillis()
            )
            // In production: supabaseService.insert("admins", adminData)
            Log.d(TAG, "Admin created: $email")
            Result.success("Admin created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create admin", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update admin role/permissions
     */
    suspend fun updateAdmin(
        adminId: String,
        role: AdminRole?,
        permissions: List<String>?,
        isActive: Boolean?
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            role?.let { updates["role"] = it.name }
            permissions?.let { updates["permissions"] = it.joinToString(",") }
            isActive?.let { updates["is_active"] = it }
            updates["updated_at"] = System.currentTimeMillis()
            
            // In production: supabaseService.update("admins", updates, adminId)
            Log.d(TAG, "Admin updated: $adminId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update admin", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all admins
     */
    suspend fun getAllAdmins(): List<AdminAccount> {
        return try {
            // In production: supabaseService.select("admins")
            _admins.value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get admins", e)
            emptyList()
        }
    }
    
    // ==================== SYSTEM STATS ====================
    
    /**
     * Get comprehensive system statistics
     */
    suspend fun getSystemStats(): SystemStats {
        return try {
            // In production, aggregate from various tables
            SystemStats(
                totalCustomers = 1523,
                totalDrivers = 245,
                totalFleetOwners = 18,
                activeDrivers = 142,
                totalTrips = 45230,
                tripsToday = 234,
                tripsThisMonth = 8923,
                totalRevenue = 12500000.0,
                revenueToday = 85000.0,
                revenueThisMonth = 2500000.0,
                commissionCollected = 375000.0,
                pendingPayouts = 450000.0,
                customerGrowth = 12.5f,
                driverGrowth = 8.3f,
                averageRating = 4.6f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system stats", e)
            SystemStats()
        }
    }
    
    /**
     * Get analytics data
     */
    suspend fun getAnalytics(
        startDate: Long,
        endDate: Long
    ): AnalyticsData {
        return try {
            // In production, query and aggregate data
            AnalyticsData(
                dailyTrips = listOf(200, 220, 250, 230, 280, 300, 234),
                dailyRevenue = listOf(65000.0, 72000.0, 82000.0, 75000.0, 90000.0, 98000.0, 85000.0),
                customerAcquisition = listOf(15, 18, 22, 25, 30, 28, 35),
                driverAcquisition = listOf(5, 6, 8, 7, 9, 10, 8),
                popularRoutes = listOf(
                    RouteStat("Airport - City Center", 1250),
                    RouteStat("Railway Station - City Center", 980),
                    RouteStat("Mall - Residential Area", 750),
                    RouteStat("IT Park - City Center", 620),
                    RouteStat("Hospital - Residential Area", 450)
                ),
                vehicleCategoryUsage = listOf(
                    CategoryUsage("Sedan", 45),
                    CategoryUsage("Hatchback", 30),
                    CategoryUsage("SUV", 15),
                    CategoryUsage("Auto", 10)
                ),
                averageTripDistance = 12.5,
                averageTripDuration = 35,
                averageRating = 4.6f,
                cancellationRate = 5.2f,
                repeatCustomerRate = 68.5f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get analytics", e)
            AnalyticsData()
        }
    }
    
    data class AnalyticsData(
        val dailyTrips: List<Int>,
        val dailyRevenue: List<Double>,
        val customerAcquisition: List<Int>,
        val driverAcquisition: List<Int>,
        val popularRoutes: List<RouteStat>,
        val vehicleCategoryUsage: List<CategoryUsage>,
        val averageTripDistance: Double,
        val averageTripDuration: Int,
        val averageRating: Float,
        val cancellationRate: Float,
        val repeatCustomerRate: Float
    )
    
    data class RouteStat(val route: String, val trips: Int)
    data class CategoryUsage(val category: String, val percentage: Int)
    
    // ==================== CITY MANAGEMENT ====================
    
    /**
     * Get all cities
     */
    suspend fun getCities(): List<City> {
        return try {
            listOf(
                City("C001", "Bangalore", "Karnataka", true, 50.0, 12.0, 80.0, 85, 15230),
                City("C002", "Mumbai", "Maharashtra", true, 60.0, 14.0, 100.0, 120, 18500),
                City("C003", "Delhi", "Delhi NCR", true, 55.0, 13.0, 90.0, 95, 12000),
                City("C004", "Chennai", "Tamil Nadu", true, 50.0, 12.0, 80.0, 65, 8900),
                City("C005", "Hyderabad", "Telangana", true, 50.0, 11.0, 75.0, 55, 7500)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cities", e)
            emptyList()
        }
    }
    
    /**
     * Add new city
     */
    suspend fun addCity(
        name: String,
        state: String,
        baseFare: Double,
        perKmRate: Double,
        minFare: Double
    ): Result<String> {
        return try {
            val cityData = mapOf(
                "name" to name,
                "state" to state,
                "base_fare" to baseFare,
                "per_km_rate" to perKmRate,
                "min_fare" to minFare,
                "is_active" to true
            )
            // In production: supabaseService.insert("cities", cityData)
            Log.d(TAG, "City added: $name")
            Result.success("City added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add city", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update city pricing
     */
    suspend fun updateCityPricing(
        cityId: String,
        baseFare: Double?,
        perKmRate: Double?,
        minFare: Double?
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            baseFare?.let { updates["base_fare"] = it }
            perKmRate?.let { updates["per_km_rate"] = it }
            minFare?.let { updates["min_fare"] = it }
            
            // In production: supabaseService.update("cities", updates, cityId)
            Log.d(TAG, "City pricing updated: $cityId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update city pricing", e)
            Result.failure(e)
        }
    }
    
    // ==================== PRICING MANAGEMENT ====================
    
    /**
     * Get pricing for all cities and categories
     */
    suspend fun getAllPricing(): List<PricingConfig> {
        return try {
            listOf(
                // Bangalore
                PricingConfig("C001", "Bangalore", "Hatchback", 50.0, 10.0, 1.0, 80.0),
                PricingConfig("C001", "Bangalore", "Sedan", 60.0, 12.0, 1.5, 100.0),
                PricingConfig("C001", "Bangalore", "SUV", 80.0, 15.0, 2.0, 150.0),
                // Mumbai
                PricingConfig("C002", "Mumbai", "Hatchback", 60.0, 12.0, 1.5, 100.0),
                PricingConfig("C002", "Mumbai", "Sedan", 70.0, 14.0, 2.0, 120.0),
                PricingConfig("C002", "Mumbai", "SUV", 100.0, 18.0, 2.5, 180.0)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get pricing", e)
            emptyList()
        }
    }
    
    /**
     * Update vehicle category pricing
     */
    suspend fun updatePricing(
        cityId: String,
        vehicleCategory: String,
        baseFare: Double,
        perKm: Double,
        perMin: Double,
        minFare: Double
    ): Result<Unit> {
        return try {
            val pricingData = mapOf(
                "city_id" to cityId,
                "vehicle_category" to vehicleCategory,
                "base_fare" to baseFare,
                "per_km" to perKm,
                "per_min" to perMin,
                "min_fare" to minFare,
                "updated_at" to System.currentTimeMillis()
            )
            // In production: supabaseService.upsert("pricing_config", pricingData)
            Log.d(TAG, "Pricing updated for $cityId - $vehicleCategory")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update pricing", e)
            Result.failure(e)
        }
    }
    
    // ==================== PROMO CODES ====================
    
    /**
     * Get all promo codes
     */
    suspend fun getPromoCodes(): List<PromoCode> {
        return try {
            listOf(
                PromoCode(
                    "P001", "FIRST50", "First ride discount",
                    DiscountType.PERCENTAGE, 50.0, 100.0, 100.0,
                    1000, 456,
                    System.currentTimeMillis() - 86400000,
                    System.currentTimeMillis() + 86400000 * 30,
                    true
                ),
                PromoCode(
                    "P002", "FLAT100", "Flat ₹100 off",
                    DiscountType.FIXED_AMOUNT, 100.0, null, 200.0,
                    500, 234,
                    System.currentTimeMillis() - 86400000,
                    System.currentTimeMillis() + 86400000 * 60,
                    true
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get promo codes", e)
            emptyList()
        }
    }
    
    /**
     * Create promo code
     */
    suspend fun createPromoCode(
        code: String,
        description: String,
        discountType: DiscountType,
        discountValue: Double,
        maxDiscount: Double?,
        minOrderValue: Double?,
        usageLimit: Int,
        validDays: Int
    ): Result<String> {
        return try {
            val validFrom = System.currentTimeMillis()
            val validTo = validFrom + (validDays * 86400000L)
            
            val promoData = mapOf(
                "code" to code.uppercase(),
                "description" to description,
                "discount_type" to discountType.name,
                "discount_value" to discountValue,
                "max_discount" to (maxDiscount ?: 0),
                "min_order_value" to (minOrderValue ?: 0),
                "usage_limit" to usageLimit,
                "used_count" to 0,
                "valid_from" to validFrom,
                "valid_to" to validTo,
                "is_active" to true
            )
            // In production: supabaseService.insert("promo_codes", promoData)
            Log.d(TAG, "Promo code created: $code")
            Result.success("Promo code created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create promo code", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate promo code
     */
    suspend fun deactivatePromoCode(promoId: String): Result<Unit> {
        return try {
            // In production: supabaseService.update("promo_codes", mapOf("is_active" to false), promoId)
            Log.d(TAG, "Promo code deactivated: $promoId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deactivate promo code", e)
            Result.failure(e)
        }
    }
    
    // ==================== COMMISSION REPORTS ====================
    
    /**
     * Get commission reports
     */
    suspend fun getCommissionReports(
        startDate: Long,
        endDate: Long
    ): List<CommissionReport> {
        return try {
            listOf(
                CommissionReport(
                    "Jan 2024",
                    2500, 750000.0, 15.0, 112500.0, 5625.0, 106875.0, 637500.0, 0.0
                ),
                CommissionReport(
                    "Dec 2023",
                    2800, 820000.0, 15.0, 123000.0, 6150.0, 116850.0, 703000.0, 0.0
                ),
                CommissionReport(
                    "Nov 2023",
                    2300, 680000.0, 15.0, 102000.0, 5100.0, 96900.0, 583000.0, 0.0
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get commission reports", e)
            emptyList()
        }
    }
    
    // ==================== SYSTEM SETTINGS ====================
    
    /**
     * Get system settings
     */
    fun getSystemSettings(): SystemSettings {
        return _systemSettings.value
    }
    
    /**
     * Update system settings
     */
    suspend fun updateSystemSettings(settings: SystemSettings): Result<Unit> {
        return try {
            _systemSettings.value = settings
            // In production: supabaseService.update("system_settings", settingsMap, "default")
            Log.d(TAG, "System settings updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update system settings", e)
            Result.failure(e)
        }
    }
    
    // ==================== MAINTENANCE MODE ====================
    
    /**
     * Toggle maintenance mode
     */
    suspend fun toggleMaintenanceMode(enabled: Boolean): Result<Unit> {
        return try {
            val updates = mapOf("maintenance_mode" to enabled)
            // In production: supabaseService.update("system_settings", updates, "default")
            _systemSettings.value = _systemSettings.value.copy(maintenanceMode = enabled)
            Log.d(TAG, "Maintenance mode: $enabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle maintenance mode", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "SuperAdmin"
        
        @Volatile
        private var instance: SuperAdminManager? = null
        
        fun getInstance(context: Context): SuperAdminManager {
            return instance ?: synchronized(this) {
                instance ?: SuperAdminManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
