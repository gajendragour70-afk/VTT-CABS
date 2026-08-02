package com.vttcabs.common.finance

import android.content.Context
import android.util.Log
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Finance Management System
 * Handles driver wallet, admin wallet, fleet owner wallet, and financial reports
 */
class FinanceManager(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    // ==================== DATA CLASSES ====================
    
    data class DriverWallet(
        val driverId: String,
        val balance: Double = 0.0,
        val pendingAmount: Double = 0.0,
        val totalEarnings: Double = 0.0,
        val totalCommission: Double = 0.0,
        val transactions: List<WalletTransaction> = emptyList()
    )
    
    data class WalletTransaction(
        val id: String,
        val type: TransactionType,
        val amount: Double,
        val description: String,
        val bookingId: String?,
        val timestamp: Long,
        val balanceAfter: Double
    )
    
    enum class TransactionType {
        TRIP_EARNING,
        COMMISSION_DEDUCTED,
        BONUS,
        INCENTIVE,
        PENALTY,
        PAYOUT,
        REFUND,
        CASHBACK
    }
    
    data class PayoutRequest(
        val id: String,
        val userId: String,
        val userType: UserType,
        val amount: Double,
        val status: PayoutStatus,
        val bankName: String,
        val accountNumber: String,
        val ifscCode: String,
        val upiId: String?,
        val requestedAt: Long,
        val processedAt: Long?,
        val failureReason: String?
    )
    
    enum class UserType {
        DRIVER,
        FLEET_OWNER
    }
    
    enum class PayoutStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
    
    data class AdminWallet(
        val balance: Double = 0.0,
        val totalCommissionCollected: Double = 0.0,
        val totalPayouts: Double = 0.0,
        val pendingPayouts: Double = 0.0,
        val transactions: List<AdminTransaction> = emptyList()
    )
    
    data class AdminTransaction(
        val id: String,
        val type: AdminTransactionType,
        val amount: Double,
        val description: String,
        val relatedUserId: String?,
        val relatedBookingId: String?,
        val timestamp: Long
    )
    
    enum class AdminTransactionType {
        COMMISSION,
        GST,
        PENALTY_RECEIVED,
        REFUND_ISSUED,
        PAYOUT_PROCESSED,
        INCENTIVE_PAID
    }
    
    data class CommissionReport(
        val period: String,
        val startDate: Long,
        val endDate: Long,
        val totalTrips: Int,
        val grossRevenue: Double,
        val commissionPercent: Double,
        val grossCommission: Double,
        val gstAmount: Double,
        val netCommission: Double,
        val driverPayouts: Double,
        val fleetOwnerPayouts: Double
    )
    
    data class GstInvoice(
        val invoiceNumber: String,
        val invoiceDate: Long,
        val customerName: String,
        val customerAddress: String,
        val customerGstin: String?,
        val bookingId: String,
        val rideDate: Long,
        val pickupLocation: String,
        val dropLocation: String,
        val vehicleType: String,
        val rideFare: Double,
        val cgstPercent: Double,
        val sgstPercent: Double,
        val cgstAmount: Double,
        val sgstAmount: Double,
        val totalGst: Double,
        val totalAmount: Double
    )
    
    // ==================== DRIVER WALLET ====================
    
    private val _driverWallets = MutableStateFlow<Map<String, DriverWallet>>(emptyMap())
    val driverWallets: StateFlow<Map<String, DriverWallet>> = _driverWallets.asStateFlow()
    
    /**
     * Get driver's wallet
     */
    suspend fun getDriverWallet(driverId: String): DriverWallet {
        return try {
            // In production: Query driver_wallet and driver_earnings
            DriverWallet(
                driverId = driverId,
                balance = 2450.75,
                pendingAmount = 350.0,
                totalEarnings = 45000.0,
                totalCommission = 6750.0,
                transactions = listOf(
                    WalletTransaction(
                        "TX001", TransactionType.TRIP_EARNING, 350.0,
                        "Trip completed - BK001", "BK001",
                        System.currentTimeMillis() - 3600000, 2450.75
                    ),
                    WalletTransaction(
                        "TX002", TransactionType.COMMISSION_DEDUCTED, -52.50,
                        "Platform commission (15%)", "BK001",
                        System.currentTimeMillis() - 3600000, 2100.75
                    ),
                    WalletTransaction(
                        "TX003", TransactionType.BONUS, 100.0,
                        "Daily incentive bonus", null,
                        System.currentTimeMillis() - 86400000, 2153.25
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get driver wallet", e)
            DriverWallet(driverId)
        }
    }
    
    /**
     * Add earning to driver wallet
     */
    suspend fun addEarning(
        driverId: String,
        bookingId: String,
        tripFare: Double,
        driverShare: Double
    ): Result<Unit> {
        return try {
            val commission = tripFare - driverShare
            val gst = commission * 0.05
            val netCommission = commission + gst
            
            // Create earning record
            val earningData = mapOf(
                "driver_id" to driverId,
                "booking_id" to bookingId,
                "trip_fare" to tripFare,
                "driver_share" to driverShare,
                "commission" to commission,
                "gst" to gst,
                "created_at" to System.currentTimeMillis()
            )
            
            // In production: supabaseService.insert("driver_earnings", earningData)
            
            // Update wallet balance
            val currentWallet = getDriverWallet(driverId)
            val newBalance = currentWallet.balance + driverShare
            
            // supabaseService.update("driver_wallet", mapOf("balance" to newBalance), driverId)
            
            Log.d(TAG, "Earning added for driver $driverId: ₹$driverShare")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add earning", e)
            Result.failure(e)
        }
    }
    
    /**
     * Request payout
     */
    suspend fun requestDriverPayout(
        driverId: String,
        amount: Double,
        bankName: String,
        accountNumber: String,
        ifscCode: String,
        upiId: String? = null
    ): Result<String> {
        return try {
            val wallet = getDriverWallet(driverId)
            
            if (amount > wallet.balance) {
                return Result.failure(Exception("Insufficient balance"))
            }
            
            if (amount < 500) {
                return Result.failure(Exception("Minimum payout is ₹500"))
            }
            
            val payoutData = mapOf(
                "user_id" to driverId,
                "user_type" to UserType.DRIVER.name,
                "amount" to amount,
                "status" to PayoutStatus.PENDING.name,
                "bank_name" to bankName,
                "account_number" to accountNumber,
                "ifsc_code" to ifscCode,
                "upi_id" to (upiId ?: ""),
                "requested_at" to System.currentTimeMillis()
            )
            
            // In production: supabaseService.insert("payout_requests", payoutData)
            
            // Deduct from wallet
            // supabaseService.update("driver_wallet", mapOf("balance" to wallet.balance - amount), driverId)
            
            Log.d(TAG, "Payout requested for driver $driverId: ₹$amount")
            Result.success("Payout request submitted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request payout", e)
            Result.failure(e)
        }
    }
    
    // ==================== FLEET OWNER WALLET ====================
    
    private val _fleetWallets = MutableStateFlow<Map<String, FleetWallet>>(emptyMap())
    val fleetWallets: StateFlow<Map<String, FleetWallet>> = _fleetWallets.asStateFlow()
    
    data class FleetWallet(
        val fleetOwnerId: String,
        val balance: Double = 0.0,
        val pendingAmount: Double = 0.0,
        val totalEarnings: Double = 0.0,
        val totalCommission: Double = 0.0,
        val transactions: List<FleetTransaction> = emptyList()
    )
    
    data class FleetTransaction(
        val id: String,
        val type: FleetTransactionType,
        val amount: Double,
        val description: String,
        val driverId: String?,
        val bookingId: String?,
        val timestamp: Long
    )
    
    enum class FleetTransactionType {
        DRIVER_EARNING_SHARE,
        COMMISSION_DEDUCTED,
        PAYOUT,
        INCENTIVE,
        PENALTY
    }
    
    /**
     * Get fleet owner's wallet
     */
    suspend fun getFleetWallet(fleetOwnerId: String): FleetWallet {
        return try {
            FleetWallet(
                fleetOwnerId = fleetOwnerId,
                balance = 12500.0,
                pendingAmount = 2500.0,
                totalEarnings = 150000.0,
                totalCommission = 22500.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get fleet wallet", e)
            FleetWallet(fleetOwnerId)
        }
    }
    
    /**
     * Request fleet owner payout
     */
    suspend fun requestFleetPayout(
        fleetOwnerId: String,
        amount: Double,
        bankName: String,
        accountNumber: String,
        ifscCode: String
    ): Result<String> {
        return try {
            val wallet = getFleetWallet(fleetOwnerId)
            
            if (amount > wallet.balance) {
                return Result.failure(Exception("Insufficient balance"))
            }
            
            val payoutData = mapOf(
                "user_id" to fleetOwnerId,
                "user_type" to UserType.FLEET_OWNER.name,
                "amount" to amount,
                "status" to PayoutStatus.PENDING.name,
                "bank_name" to bankName,
                "account_number" to accountNumber,
                "ifsc_code" to ifscCode,
                "requested_at" to System.currentTimeMillis()
            )
            
            // In production: supabaseService.insert("payout_requests", payoutData)
            
            Log.d(TAG, "Payout requested for fleet $fleetOwnerId: ₹$amount")
            Result.success("Payout request submitted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request fleet payout", e)
            Result.failure(e)
        }
    }
    
    // ==================== PAYOUT MANAGEMENT ====================
    
    private val _pendingPayouts = MutableStateFlow<List<PayoutRequest>>(emptyList())
    val pendingPayouts: StateFlow<List<PayoutRequest>> = _pendingPayouts.asStateFlow()
    
    /**
     * Get pending payout requests
     */
    suspend fun getPendingPayouts(): List<PayoutRequest> {
        return try {
            // In production: supabaseService.select("payout_requests", "?status=eq.PENDING")
            listOf(
                PayoutRequest(
                    "PR001", "D001", UserType.DRIVER, 2500.0,
                    PayoutStatus.PENDING, "HDFC Bank", "XXXX1234", "HDFC0001234", null,
                    System.currentTimeMillis() - 86400000, null, null
                ),
                PayoutRequest(
                    "PR002", "FO001", UserType.FLEET_OWNER, 15000.0,
                    PayoutStatus.PENDING, "ICICI Bank", "XXXX5678", "ICIC0001234", null,
                    System.currentTimeMillis() - 172800000, null, null
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get pending payouts", e)
            emptyList()
        }
    }
    
    /**
     * Process payout (approve/reject)
     */
    suspend fun processPayout(
        payoutId: String,
        approved: Boolean,
        failureReason: String? = null
    ): Result<Unit> {
        return try {
            val status = if (approved) PayoutStatus.COMPLETED else PayoutStatus.FAILED
            
            val updateData = mapOf(
                "status" to status.name,
                "processed_at" to System.currentTimeMillis(),
                "failure_reason" to (failureReason ?: "")
            )
            
            // In production:
            // supabaseService.update("payout_requests", updateData, payoutId)
            
            // If rejected, refund to wallet
            if (!approved) {
                val payout = _pendingPayouts.value.find { it.id == payoutId }
                payout?.let {
                    refundPayout(it)
                }
            }
            
            Log.d(TAG, "Payout $payoutId processed: $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process payout", e)
            Result.failure(e)
        }
    }
    
    private suspend fun refundPayout(payout: PayoutRequest) {
        // Refund amount to user's wallet
        // supabaseService.updateWalletBalance(payout.userId, payout.userType, payout.amount)
        Log.d(TAG, "Payout refunded to ${payout.userId}: ₹${payout.amount}")
    }
    
    // ==================== ADMIN WALLET ====================
    
    private val _adminWallet = MutableStateFlow(AdminWallet())
    val adminWallet: StateFlow<AdminWallet> = _adminWallet.asStateFlow()
    
    /**
     * Get admin wallet summary
     */
    suspend fun getAdminWallet(): AdminWallet {
        return try {
            AdminWallet(
                balance = 125000.0,
                totalCommissionCollected = 500000.0,
                totalPayouts = 375000.0,
                pendingPayouts = 45000.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get admin wallet", e)
            AdminWallet()
        }
    }
    
    // ==================== COMMISSION REPORTS ====================
    
    /**
     * Get commission report for period
     */
    suspend fun getCommissionReport(
        startDate: Long,
        endDate: Long
    ): CommissionReport {
        return try {
            // In production: Aggregate from driver_earnings and bookings
            CommissionReport(
                period = "January 2024",
                startDate = startDate,
                endDate = endDate,
                totalTrips = 2500,
                grossRevenue = 750000.0,
                commissionPercent = 15.0,
                grossCommission = 112500.0,
                gstAmount = 5625.0,
                netCommission = 106875.0,
                driverPayouts = 637125.0,
                fleetOwnerPayouts = 0.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get commission report", e)
            CommissionReport("Error", startDate, endDate, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }
    
    // ==================== GST INVOICE ====================
    
    /**
     * Generate GST invoice for booking
     */
    suspend fun generateGstInvoice(
        bookingId: String,
        customerName: String,
        customerAddress: String,
        customerGstin: String?
    ): GstInvoice {
        return try {
            // In production: Get from bookings and calculate GST
            val invoiceNumber = "INV-${System.currentTimeMillis()}"
            
            GstInvoice(
                invoiceNumber = invoiceNumber,
                invoiceDate = System.currentTimeMillis(),
                customerName = customerName,
                customerAddress = customerAddress,
                customerGstin = customerGstin,
                bookingId = bookingId,
                rideDate = System.currentTimeMillis() - 86400000,
                pickupLocation = "MG Road, Bangalore",
                dropLocation = "Koramangala, Bangalore",
                vehicleType = "Sedan",
                rideFare = 350.0,
                cgstPercent = 2.5,
                sgstPercent = 2.5,
                cgstAmount = 8.75,
                sgstAmount = 8.75,
                totalGst = 17.50,
                totalAmount = 367.50
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate GST invoice", e)
            throw e
        }
    }
    
    // ==================== FINANCIAL SUMMARY ====================
    
    /**
     * Get financial summary for admin dashboard
     */
    suspend fun getFinancialSummary(): FinancialSummary {
        return try {
            FinancialSummary(
                todayRevenue = 85000.0,
                todayTrips = 234,
                weekRevenue = 550000.0,
                monthRevenue = 2200000.0,
                yearRevenue = 12500000.0,
                pendingPayouts = 45000.0,
                commissionCollected = 375000.0,
                gstCollected = 18750.0,
                averageTripValue = 350.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get financial summary", e)
            FinancialSummary()
        }
    }
    
    data class FinancialSummary(
        val todayRevenue: Double,
        val todayTrips: Int,
        val weekRevenue: Double,
        val monthRevenue: Double,
        val yearRevenue: Double,
        val pendingPayouts: Double,
        val commissionCollected: Double,
        val gstCollected: Double,
        val averageTripValue: Double
    )
    
    companion object {
        private const val TAG = "Finance"
        
        @Volatile
        private var instance: FinanceManager? = null
        
        fun getInstance(context: Context): FinanceManager {
            return instance ?: synchronized(this) {
                instance ?: FinanceManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
