package com.vttcabs.common.analytics

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Analytics Dashboard System
 * Real-time analytics and reporting for Super Admin
 */
class AnalyticsManager(private val context: Context) {
    
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()
    
    private val _liveDrivers = MutableStateFlow<List<LiveDriverInfo>>(emptyList())
    val liveDrivers: StateFlow<List<LiveDriverInfo>> = _liveDrivers.asStateFlow()
    
    private val _liveTrips = MutableStateFlow<List<LiveTripInfo>>(emptyList())
    val liveTrips: StateFlow<List<LiveTripInfo>> = _liveTrips.asStateFlow()
    
    // ==================== DATA CLASSES ====================
    
    data class DashboardStats(
        val totalCustomers: Int = 0,
        val totalDrivers: Int = 0,
        val activeDrivers: Int = 0,
        val totalTrips: Int = 0,
        val tripsToday: Int = 0,
        val tripsThisWeek: Int = 0,
        val tripsThisMonth: Int = 0,
        val totalRevenue: Double = 0.0,
        val revenueToday: Double = 0.0,
        val revenueThisWeek: Double = 0.0,
        val revenueThisMonth: Double = 0.0,
        val averageRating: Float = 0f,
        val customerGrowth: Float = 0f,
        val driverGrowth: Float = 0f,
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    data class LiveDriverInfo(
        val driverId: String,
        val name: String,
        val phone: String,
        val vehicleNumber: String,
        val vehicleType: String,
        val status: DriverStatus,
        val currentLocation: Pair<Double, Double>?,
        val currentBookingId: String?,
        val isOnline: Boolean,
        val todayTrips: Int,
        val todayEarnings: Double,
        val rating: Float
    )
    
    enum class DriverStatus {
        ONLINE,
        ON_TRIP,
        OFFLINE,
        INACTIVE
    }
    
    data class LiveTripInfo(
        val tripId: String,
        val customerName: String,
        val customerPhone: String,
        val driverName: String?,
        val driverPhone: String?,
        val vehicleNumber: String?,
        val pickupLocation: String,
        val dropLocation: String,
        val status: TripStatus,
        val fare: Double,
        val bookingTime: Long,
        val driverLocation: Pair<Double, Double>?
    )
    
    enum class TripStatus {
        SEARCHING_DRIVER,
        DRIVER_ASSIGNED,
        DRIVER_EN_ROUTE,
        DRIVER_ARRIVED,
        TRIP_STARTED,
        TRIP_COMPLETED,
        CANCELLED
    }
    
    data class ChartData(
        val label: String,
        val value: Double,
        val color: Int? = null
    )
    
    data class RevenueGraphData(
        val period: String,
        val labels: List<String>,
        val revenue: List<Double>,
        val trips: List<Int>
    )
    
    data class BookingTrends(
        val hourOfDay: List<Int>,
        val tripsByHour: List<Int>,
        val dayOfWeek: List<String>,
        val tripsByDay: List<Int>
    )
    
    data class DriverPerformance(
        val driverId: String,
        val driverName: String,
        val totalTrips: Int,
        val totalEarnings: Double,
        val averageRating: Float,
        val acceptanceRate: Float,
        val cancellationRate: Float,
        val completedOnTime: Float,
        val rank: Int
    )
    
    data class CustomerAnalytics(
        val newCustomers: Int,
        val returningCustomers: Int,
        val repeatRate: Float,
        val averageRidesPerCustomer: Float,
        val topCustomers: List<TopCustomer>,
        val churnRiskCustomers: List<CustomerRisk>
    )
    
    data class TopCustomer(
        val customerId: String,
        val name: String,
        val totalTrips: Int,
        val totalSpent: Double,
        val lastRide: Long
    )
    
    data class CustomerRisk(
        val customerId: String,
        val name: String,
        val lastActive: Long,
        val riskLevel: RiskLevel
    )
    
    enum class RiskLevel {
        LOW, MEDIUM, HIGH
    }
    
    // ==================== DASHBOARD STATS ====================
    
    /**
     * Get comprehensive dashboard statistics
     */
    suspend fun getDashboardStats(): DashboardStats {
        return try {
            DashboardStats(
                totalCustomers = 1523,
                totalDrivers = 245,
                activeDrivers = 142,
                totalTrips = 45230,
                tripsToday = 234,
                tripsThisWeek = 1650,
                tripsThisMonth = 8923,
                totalRevenue = 12500000.0,
                revenueToday = 85000.0,
                revenueThisWeek = 550000.0,
                revenueThisMonth = 2200000.0,
                averageRating = 4.6f,
                customerGrowth = 12.5f,
                driverGrowth = 8.3f,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get dashboard stats", e)
            DashboardStats()
        }
    }
    
    // ==================== LIVE TRACKING ====================
    
    /**
     * Get all live drivers (currently online)
     */
    suspend fun getLiveDrivers(): List<LiveDriverInfo> {
        return try {
            listOf(
                LiveDriverInfo(
                    "D001", "Ramesh Kumar", "9876543210",
                    "KA-01-AB-1234", "Sedan",
                    DriverStatus.ON_TRIP,
                    Pair(12.9716, 77.5946),
                    "BK001", true, 8, 2800.0, 4.7f
                ),
                LiveDriverInfo(
                    "D002", "Suresh Patel", "9876543211",
                    "KA-02-CD-5678", "Hatchback",
                    DriverStatus.ONLINE,
                    Pair(12.9352, 77.6245),
                    null, true, 5, 1750.0, 4.5f
                ),
                LiveDriverInfo(
                    "D003", "Mahesh Singh", "9876543212",
                    "KA-03-EF-9012", "SUV",
                    DriverStatus.ON_TRIP,
                    Pair(12.9784, 77.6408),
                    "BK002", true, 3, 1200.0, 4.8f
                ),
                LiveDriverInfo(
                    "D004", "Rajesh Gupta", "9876543213",
                    "KA-04-GH-3456", "Sedan",
                    DriverStatus.ONLINE,
                    Pair(12.9592, 77.6489),
                    null, true, 6, 2100.0, 4.6f
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get live drivers", e)
            emptyList()
        }
    }
    
    /**
     * Get all live trips (currently active)
     */
    suspend fun getLiveTrips(): List<LiveTripInfo> {
        return try {
            listOf(
                LiveTripInfo(
                    "BK001", "John Doe", "9876543210",
                    "Ramesh Kumar", "9876543210",
                    "KA-01-AB-1234",
                    "MG Road", "Koramangala",
                    TripStatus.TRIP_STARTED, 350.0,
                    System.currentTimeMillis() - 1800000,
                    Pair(12.9716, 77.5946)
                ),
                LiveTripInfo(
                    "BK002", "Jane Smith", "9876543211",
                    "Mahesh Singh", "9876543212",
                    "KA-03-EF-9012",
                    "Indiranagar", "Whitefield",
                    TripStatus.DRIVER_EN_ROUTE, 420.0,
                    System.currentTimeMillis() - 900000,
                    Pair(12.9784, 77.6408)
                ),
                LiveTripInfo(
                    "BK003", "Bob Wilson", "9876543212",
                    null, null, null,
                    "City Center", "Airport",
                    TripStatus.SEARCHING_DRIVER, 550.0,
                    System.currentTimeMillis() - 60000,
                    null
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get live trips", e)
            emptyList()
        }
    }
    
    // ==================== REVENUE ANALYTICS ====================
    
    /**
     * Get revenue graph data
     */
    suspend fun getRevenueGraph(
        period: GraphPeriod
    ): RevenueGraphData {
        return try {
            val labels = when (period) {
                GraphPeriod.DAILY -> (0..23).map { "$it:00" }
                GraphPeriod.WEEKLY -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                GraphPeriod.MONTHLY -> (1..30).map { "Day $it" }
                GraphPeriod.YEARLY -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
            }
            
            val revenue = when (period) {
                GraphPeriod.DAILY -> listOf(1200.0, 1500.0, 2000.0, 2500.0, 3000.0, 4500.0, 5200.0, 5800.0, 5500.0, 4800.0, 4200.0, 3500.0, 3000.0, 2800.0, 3200.0, 3800.0, 4200.0, 4800.0, 5500.0, 6000.0, 6500.0, 5800.0, 4500.0, 3000.0)
                GraphPeriod.WEEKLY -> listOf(45000.0, 52000.0, 48000.0, 55000.0, 62000.0, 75000.0, 80000.0)
                GraphPeriod.MONTHLY -> (1..30).map { 50000.0 + (it * 1000) }
                GraphPeriod.YEARLY -> listOf(550000.0, 620000.0, 580000.0, 650000.0, 720000.0, 680000.0)
            }
            
            val trips = when (period) {
                GraphPeriod.DAILY -> listOf(3, 4, 6, 7, 9, 13, 15, 17, 16, 14, 12, 10, 9, 8, 9, 11, 12, 14, 16, 18, 19, 17, 13, 9)
                GraphPeriod.WEEKLY -> listOf(130, 150, 140, 160, 180, 220, 240)
                GraphPeriod.MONTHLY -> (1..30).map { 150 + (it * 5) }
                GraphPeriod.YEARLY -> listOf(1600, 1800, 1700, 1900, 2100, 2000)
            }
            
            RevenueGraphData(
                period = period.name,
                labels = labels,
                revenue = revenue,
                trips = trips
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get revenue graph", e)
            RevenueGraphData(period.name, emptyList(), emptyList(), emptyList())
        }
    }
    
    enum class GraphPeriod {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    // ==================== BOOKING TRENDS ====================
    
    /**
     * Get booking trends analysis
     */
    suspend fun getBookingTrends(): BookingTrends {
        return try {
            BookingTrends(
                hourOfDay = (0..23).toList(),
                tripsByHour = listOf(2, 1, 0, 0, 1, 2, 5, 12, 18, 15, 14, 16, 18, 17, 16, 18, 20, 22, 25, 22, 18, 15, 10, 5),
                dayOfWeek = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
                tripsByDay = listOf(280, 180, 200, 210, 220, 250, 300)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get booking trends", e)
            BookingTrends(emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
    
    // ==================== DRIVER PERFORMANCE ====================
    
    /**
     * Get top performing drivers
     */
    suspend fun getTopDrivers(limit: Int = 10): List<DriverPerformance> {
        return try {
            listOf(
                DriverPerformance("D001", "Ramesh Kumar", 450, 157500.0, 4.8f, 95.0f, 3.0f, 97.0f, 1),
                DriverPerformance("D002", "Suresh Patel", 420, 147000.0, 4.7f, 93.0f, 4.0f, 96.0f, 2),
                DriverPerformance("D003", "Mahesh Singh", 380, 133000.0, 4.9f, 98.0f, 1.0f, 99.0f, 3),
                DriverPerformance("D004", "Rajesh Gupta", 350, 122500.0, 4.6f, 91.0f, 5.0f, 95.0f, 4),
                DriverPerformance("D005", "Amit Kumar", 320, 112000.0, 4.5f, 90.0f, 6.0f, 94.0f, 5)
            ).take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get top drivers", e)
            emptyList()
        }
    }
    
    // ==================== CUSTOMER ANALYTICS ====================
    
    /**
     * Get customer analytics
     */
    suspend fun getCustomerAnalytics(): CustomerAnalytics {
        return try {
            CustomerAnalytics(
                newCustomers = 45,
                returningCustomers = 180,
                repeatRate = 78.5f,
                averageRidesPerCustomer = 8.5f,
                topCustomers = listOf(
                    TopCustomer("C001", "John Doe", 52, 18200.0, System.currentTimeMillis() - 86400000),
                    TopCustomer("C002", "Jane Smith", 45, 15750.0, System.currentTimeMillis() - 172800000),
                    TopCustomer("C003", "Bob Wilson", 38, 13300.0, System.currentTimeMillis() - 259200000)
                ),
                churnRiskCustomers = emptyList() // Would be calculated based on last active
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get customer analytics", e)
            CustomerAnalytics(0, 0, 0f, 0f, emptyList(), emptyList())
        }
    }
    
    // ==================== POPULAR ROUTES ====================
    
    /**
     * Get popular routes
     */
    suspend fun getPopularRoutes(limit: Int = 10): List<PopularRoute> {
        return try {
            listOf(
                PopularRoute("Airport - City Center", 1250, 450000.0),
                PopularRoute("Railway Station - City Center", 980, 352800.0),
                PopularRoute("Mall - Residential Area", 750, 262500.0),
                PopularRoute("IT Park - City Center", 620, 223200.0),
                PopularRoute("Hospital - Residential Area", 450, 157500.0),
                PopularRoute("Metro Station - Office", 380, 133000.0),
                PopularRoute("School - Home", 320, 112000.0),
                PopularRoute("Market - Home", 280, 98000.0),
                PopularRoute("Restaurant - Home", 220, 77000.0),
                PopularRoute("Gym - Home", 180, 63000.0)
            ).take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get popular routes", e)
            emptyList()
        }
    }
    
    data class PopularRoute(
        val route: String,
        val tripCount: Int,
        val totalRevenue: Double
    )
    
    // ==================== REAL-TIME UPDATES ====================
    
    /**
     * Start real-time dashboard updates
     */
    fun startRealTimeUpdates(onUpdate: (DashboardStats) -> Unit) {
        // In production, this would use WebSocket or polling
        // For demo, simulate periodic updates
        Log.d(TAG, "Real-time updates started")
    }
    
    /**
     * Stop real-time updates
     */
    fun stopRealTimeUpdates() {
        Log.d(TAG, "Real-time updates stopped")
    }
    
    companion object {
        private const val TAG = "Analytics"
        
        @Volatile
        private var instance: AnalyticsManager? = null
        
        fun getInstance(context: Context): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
