package com.vttcabs.common.marketing

import android.content.Context
import android.util.Log
import com.vttcabs.common.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Marketing System
 * Handles referrals, coupons, promo codes, loyalty points, and cashback
 */
class MarketingManager(private val context: Context) {
    
    private val supabaseService = SupabaseService.getInstance()
    
    private val _referralStats = MutableStateFlow(ReferralStats())
    val referralStats: StateFlow<ReferralStats> = _referralStats.asStateFlow()
    
    private val _loyaltyPoints = MutableStateFlow(LoyaltyPointsBalance())
    val loyaltyPoints: StateFlow<LoyaltyPointsBalance> = _loyaltyPoints.asStateFlow()
    
    private val _availableOffers = MutableStateFlow<List<Offer>>(emptyList())
    val availableOffers: StateFlow<List<Offer>> = _availableOffers.asStateFlow()
    
    // ==================== DATA CLASSES ====================
    
    data class ReferralStats(
        val referralCode: String = "",
        val totalReferrals: Int = 0,
        val successfulReferrals: Int = 0,
        val pendingRewards: Double = 0.0,
        val earnedRewards: Double = 0.0,
        val referredUsers: List<ReferredUser> = emptyList()
    )
    
    data class ReferredUser(
        val userId: String,
        val name: String,
        val phone: String,
        val status: ReferralStatus,
        val joinedAt: Long,
        val firstRideCompleted: Boolean = false,
        val rewardEarned: Double = 0.0
    )
    
    enum class ReferralStatus {
        PENDING,    // Signed up but no ride
        ACTIVE,     // Completed first ride
        REWARDED    // Reward credited
    }
    
    data class ReferralConfig(
        val referrerReward: Double = 50.0,
        val refereeReward: Double = 50.0,
        val minRideValue: Double = 100.0,
        val maxRewardPerReferral: Double = 200.0,
        val rewardType: RewardType = RewardType.FIXED
    )
    
    enum class RewardType {
        FIXED,
        PERCENTAGE
    }
    
    data class LoyaltyPointsBalance(
        val userId: String = "",
        val currentPoints: Int = 0,
        val lifetimePoints: Int = 0,
        val redeemedPoints: Int = 0,
        val tier: LoyaltyTier = LoyaltyTier.BRONZE,
        val pointsHistory: List<PointsTransaction> = emptyList()
    )
    
    enum class LoyaltyTier {
        BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    }
    
    data class PointsTransaction(
        val id: String,
        val type: TransactionType,
        val points: Int,
        val description: String,
        val timestamp: Long
    )
    
    enum class TransactionType {
        EARNED,
        REDEEMED,
        BONUS,
        EXPIRED
    }
    
    data class Offer(
        val id: String,
        val title: String,
        val description: String,
        val type: OfferType,
        val discountValue: Double,
        val maxDiscount: Double?,
        val minOrderValue: Double?,
        val promoCode: String?,
        val imageUrl: String?,
        val validFrom: Long,
        val validTo: Long,
        val terms: String?,
        val isActive: Boolean
    )
    
    enum class OfferType {
        PROMO_CODE,
        CASHBACK,
        FREE_RIDE,
        DISCOUNT
    }
    
    data class CashbackWallet(
        val userId: String,
        val balance: Double = 0.0,
        val transactions: List<CashbackTransaction> = emptyList()
    )
    
    data class CashbackTransaction(
        val id: String,
        val amount: Double,
        val type: CashbackType,
        val description: String,
        val timestamp: Long
    )
    
    enum class CashbackType {
        EARNED,
        WITHDRAWN,
        EXPIRED
    )
    
    // ==================== REFERRAL SYSTEM ====================
    
    /**
     * Get user's referral code and stats
     */
    suspend fun getReferralInfo(userId: String): ReferralStats {
        return try {
            // In production: Query referral data
            val code = "VTT${userId.take(6).uppercase()}"
            ReferralStats(
                referralCode = code,
                totalReferrals = 5,
                successfulReferrals = 3,
                pendingRewards = 100.0,
                earnedRewards = 250.0,
                referredUsers = listOf(
                    ReferredUser("R001", "John D", "9876543210", ReferralStatus.ACTIVE, System.currentTimeMillis() - 86400000, true, 50.0),
                    ReferredUser("R002", "Jane S", "9876543211", ReferralStatus.PENDING, System.currentTimeMillis() - 43200000, false, 0.0)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get referral info", e)
            ReferralStats()
        }
    }
    
    /**
     * Generate referral code for user
     */
    suspend fun generateReferralCode(userId: String): String {
        val code = "VTT${UUID.randomUUID().toString().take(6).uppercase()}"
        // In production: Save to user profile
        return code
    }
    
    /**
     * Apply referral code (for new user)
     */
    suspend fun applyReferralCode(
        newUserId: String,
        referralCode: String
    ): Result<ReferralApplyResult> {
        return try {
            // Verify referral code exists
            val referrerId = verifyReferralCode(referralCode)
            
            if (referrerId == null) {
                return Result.failure(Exception("Invalid referral code"))
            }
            
            // Create referral record
            val referralData = mapOf(
                "referrer_id" to referrerId,
                "referred_user_id" to newUserId,
                "status" to ReferralStatus.PENDING.name,
                "created_at" to System.currentTimeMillis()
            )
            // In production: supabaseService.insert("referrals", referralData)
            
            // Credit signup bonus to new user
            // creditCashback(newUserId, 50.0, "Referral signup bonus")
            
            Result.success(ReferralApplyResult(true, "Referral code applied! You got ₹50 off your first ride.", 50.0))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply referral code", e)
            Result.failure(e)
        }
    }
    
    private suspend fun verifyReferralCode(code: String): String? {
        // In production: Query database for valid referral code
        return if (code.startsWith("VTT")) code else null
    }
    
    data class ReferralApplyResult(
        val success: Boolean,
        val message: String,
        val bonusAmount: Double
    )
    
    // ==================== LOYALTY PROGRAM ====================
    
    /**
     * Get user's loyalty points balance
     */
    suspend fun getLoyaltyPoints(userId: String): LoyaltyPointsBalance {
        return try {
            // In production: Query loyalty points
            LoyaltyPointsBalance(
                userId = userId,
                currentPoints = 1250,
                lifetimePoints = 5000,
                redeemedPoints = 3750,
                tier = LoyaltyTier.SILVER,
                pointsHistory = listOf(
                    PointsTransaction("T001", TransactionType.EARNED, 50, "Completed ride", System.currentTimeMillis() - 86400000),
                    PointsTransaction("T002", TransactionType.REDEMED, -200, "Redeemed for discount", System.currentTimeMillis() - 172800000)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get loyalty points", e)
            LoyaltyPointsBalance()
        }
    }
    
    /**
     * Earn points for ride
     */
    suspend fun earnPoints(userId: String, rideFare: Double): Int {
        // 1 point per ₹10 spent
        val pointsEarned = (rideFare / 10).toInt()
        
        // Update in database
        // supabaseService.updateLoyaltyPoints(userId, pointsEarned)
        
        // Check for tier upgrade
        checkTierUpgrade(userId)
        
        Log.d(TAG, "User $userId earned $pointsEarned points")
        return pointsEarned
    }
    
    /**
     * Redeem points
     */
    suspend fun redeemPoints(userId: String, points: Int): Result<Double> {
        val currentBalance = _loyaltyPoints.value.currentPoints
        
        if (points > currentBalance) {
            return Result.failure(Exception("Insufficient points"))
        }
        
        // 10 points = ₹1
        val cashValue = points / 10.0
        
        // Deduct points
        // supabaseService.updateLoyaltyPoints(userId, -points)
        
        Log.d(TAG, "User $userId redeemed $points points = ₹$cashValue")
        return Result.success(cashValue)
    }
    
    private suspend fun checkTierUpgrade(userId: String) {
        val lifetimePoints = _loyaltyPoints.value.lifetimePoints
        val newTier = when {
            lifetimePoints >= 10000 -> LoyaltyTier.DIAMOND
            lifetimePoints >= 5000 -> LoyaltyTier.PLATINUM
            lifetimePoints >= 2000 -> LoyaltyTier.GOLD
            lifetimePoints >= 500 -> LoyaltyTier.SILVER
            else -> LoyaltyTier.BRONZE
        }
        
        if (newTier != _loyaltyPoints.value.tier) {
            // Notify user of tier upgrade
            Log.d(TAG, "User $userId upgraded to $newTier tier")
        }
    }
    
    // ==================== OFFERS & PROMOS ====================
    
    /**
     * Get available offers for user
     */
    suspend fun getAvailableOffers(userId: String): List<Offer> {
        return try {
            val now = System.currentTimeMillis()
            listOf(
                Offer(
                    "O001",
                    "First Ride Special",
                    "Get 50% off up to ₹100 on your first ride",
                    OfferType.DISCOUNT,
                    50.0, 100.0, 0.0,
                    "FIRST50",
                    null,
                    now - 86400000,
                    now + 86400000 * 30,
                    "Valid for new users only",
                    true
                ),
                Offer(
                    "O002",
                    "Weekend Cashback",
                    "Get 20% cashback on all rides this weekend",
                    OfferType.CASHBACK,
                    20.0, 50.0, 200.0,
                    "WEEKEND20",
                    null,
                    now,
                    now + 86400000 * 3,
                    "Cashback applicable on rides above ₹200",
                    true
                ),
                Offer(
                    "O003",
                    "Refer & Earn",
                    "Refer a friend and both get ₹50 off",
                    OfferType.FREE_RIDE,
                    50.0, null, null,
                    null,
                    null,
                    now,
                    now + 86400000 * 365,
                    "Both users must complete a ride",
                    true
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get offers", e)
            emptyList()
        }
    }
    
    /**
     * Apply promo code
     */
    suspend fun applyPromoCode(
        code: String,
        rideFare: Double
    ): PromoCodeResult {
        return try {
            // Verify promo code
            // In production: Query promo_codes table
            val promo = verifyPromoCode(code)
            
            if (promo == null) {
                return PromoCodeResult(false, "Invalid promo code", 0.0, 0.0)
            }
            
            // Check validity
            val now = System.currentTimeMillis()
            if (now < promo.validFrom || now > promo.validTo) {
                return PromoCodeResult(false, "Promo code has expired", 0.0, 0.0)
            }
            
            // Check minimum order value
            promo.minOrderValue?.let { minValue ->
                if (rideFare < minValue) {
                    return PromoCodeResult(false, "Minimum ride value is ₹$minValue", 0.0, 0.0)
                }
            }
            
            // Calculate discount
            val discount = when (promo.discountValue) {
                // Calculate based on type
                else -> calculateDiscount(promo.discountValue, promo.type, rideFare, promo.maxDiscount)
            }
            
            val finalFare = rideFare - discount
            
            PromoCodeResult(
                success = true,
                message = "Promo code applied! You save ₹${discount.toInt()}",
                discount = discount,
                finalFare = finalFare.coerceAtLeast(0.0)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply promo code", e)
            PromoCodeResult(false, "Error applying promo code", 0.0, 0.0)
        }
    }
    
    private fun verifyPromoCode(code: String): Offer? {
        // Simplified verification
        return when (code.uppercase()) {
            "FIRST50" -> Offer(
                "P001", "First Ride", "50% off",
                OfferType.DISCOUNT, 50.0, 100.0, 0.0,
                "FIRST50", null, 0, Long.MAX_VALUE, null, true
            )
            "WEEKEND20" -> Offer(
                "P002", "Weekend", "20% off",
                OfferType.CASHBACK, 20.0, 50.0, 200.0,
                "WEEKEND20", null, 0, Long.MAX_VALUE, null, true
            )
            else -> null
        }
    }
    
    private fun calculateDiscount(
        value: Double,
        type: OfferType,
        rideFare: Double,
        maxDiscount: Double?
    ): Double {
        val discount = when (type) {
            OfferType.PERCENTAGE, OfferType.DISCOUNT -> {
                val calculated = rideFare * (value / 100)
                maxDiscount?.let { calculated.coerceAtMost(it) } ?: calculated
            }
            OfferType.FREE_RIDE -> value.coerceAtMost(rideFare)
            else -> 0.0
        }
        return discount
    }
    
    data class PromoCodeResult(
        val success: Boolean,
        val message: String,
        val discount: Double,
        val finalFare: Double
    )
    
    // ==================== CASHBACK ====================
    
    /**
     * Get cashback wallet balance
     */
    suspend fun getCashbackBalance(userId: String): CashbackWallet {
        return try {
            CashbackWallet(
                userId = userId,
                balance = 125.50,
                transactions = listOf(
                    CashbackTransaction("C001", 50.0, CashbackType.EARNED, "Cashback from ride", System.currentTimeMillis() - 86400000),
                    CashbackTransaction("C002", 75.50, CashbackType.EARNED, "Cashback from ride", System.currentTimeMillis() - 172800000)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cashback balance", e)
            CashbackWallet(userId)
        }
    }
    
    /**
     * Withdraw cashback to wallet
     */
    suspend fun withdrawCashback(userId: String, amount: Double): Result<Unit> {
        return try {
            val balance = getCashbackBalance(userId).balance
            
            if (amount > balance) {
                return Result.failure(Exception("Insufficient cashback balance"))
            }
            
            // In production:
            // 1. Deduct from cashback_wallet
            // 2. Add to main wallet
            // 3. Create transaction record
            
            Log.d(TAG, "User $userId withdrew ₹$amount cashback")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to withdraw cashback", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "Marketing"
        
        @Volatile
        private var instance: MarketingManager? = null
        
        fun getInstance(context: Context): MarketingManager {
            return instance ?: synchronized(this) {
                instance ?: MarketingManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
