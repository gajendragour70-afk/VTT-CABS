package com.vttcabs.common

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * Smart Ride Dispatch Service
 * Handles automatic driver matching and booking distribution
 */
class DispatchService private constructor() {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Dispatch configuration
    private var initialSearchRadiusKm = 5.0
    private var maxSearchRadiusKm = 20.0
    private var searchTimeoutSeconds = 30
    private var maxRejectionCount = 3
    private var offerValiditySeconds = 60

    // Active dispatch jobs
    private val activeDispatches = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Real-time updates
    private val _newBookingOffers = MutableStateFlow<List<BookingOffer>>(emptyList())
    val newBookingOffers: StateFlow<List<BookingOffer>> = _newBookingOffers.asStateFlow()

    private val _bookingStatusUpdates = MutableStateFlow<BookingStatusUpdate?>(null)
    val bookingStatusUpdates: StateFlow<BookingStatusUpdate?> = _bookingStatusUpdates.asStateFlow()

    companion object {
        @Volatile
        private var instance: DispatchService? = null

        fun getInstance(): DispatchService {
            return instance ?: synchronized(this) {
                instance ?: DispatchService().also { instance = it }
            }
        }
    }

    init {
        // Load dispatch config
        scope.launch {
            loadDispatchConfig()
        }
    }

    // ==================== CONFIG ====================

    suspend fun loadDispatchConfig() {
        try {
            val result = SupabaseService.getInstance().select<DispatchConfigRow>(
                "dispatch_config",
                ""
            )
            result.onSuccess { configs ->
                configs.forEach { config ->
                    when (config.config_key) {
                        "initial_search_radius_km" -> initialSearchRadiusKm = config.config_value.toDoubleOrNull() ?: 5.0
                        "max_search_radius_km" -> maxSearchRadiusKm = config.config_value.toDoubleOrNull() ?: 20.0
                        "search_timeout_seconds" -> searchTimeoutSeconds = config.config_value.toIntOrNull() ?: 30
                        "max_rejection_count" -> maxRejectionCount = config.config_value.toIntOrNull() ?: 3
                        "booking_offer_validity_seconds" -> offerValiditySeconds = config.config_value.toIntOrNull() ?: 60
                    }
                }
                Log.d("DispatchService", "Loaded config: radius=${initialSearchRadiusKm}km, timeout=${searchTimeoutSeconds}s")
            }
        } catch (e: Exception) {
            Log.e("DispatchService", "Error loading config, using defaults", e)
        }
    }

    // ==================== DISPATCH FLOW ====================

    /**
     * Start dispatch for a new booking
     */
    fun startDispatch(booking: BookingEntity) {
        if (activeDispatches.containsKey(booking.id)) {
            Log.w("DispatchService", "Dispatch already active for booking ${booking.id}")
            return
        }

        val job = scope.launch {
            try {
                dispatchLoop(booking)
            } catch (e: Exception) {
                Log.e("DispatchService", "Dispatch error for booking ${booking.id}", e)
            } finally {
                activeDispatches.remove(booking.id)
            }
        }
        activeDispatches[booking.id] = job
        Log.d("DispatchService", "Started dispatch for booking ${booking.id}")
    }

    /**
     * Cancel dispatch for a booking
     */
    fun cancelDispatch(bookingId: String) {
        activeDispatches[bookingId]?.cancel()
        activeDispatches.remove(bookingId)
        Log.d("DispatchService", "Cancelled dispatch for booking $bookingId")
    }

    /**
     * Main dispatch loop - searches for drivers and expands radius on timeout
     */
    private suspend fun dispatchLoop(initialBooking: BookingEntity) {
        var booking = initialBooking
        var currentRadius = initialSearchRadiusKm
        var searchAttempts = 0
        var rejectionCount = 0

        // Update booking status to searching
        updateBookingStatus(booking.id, "searching_driver")

        while (isActive && currentRadius <= maxSearchRadiusKm && rejectionCount < maxRejectionCount) {
            // Find nearby drivers
            val nearbyDrivers = findNearbyDrivers(
                pickupLat = booking.pickupLatitude,
                pickupLon = booking.pickupLongitude,
                radiusKm = currentRadius,
                vehicleCategory = booking.vehicleCategory
            )

            if (nearbyDrivers.isNotEmpty()) {
                // Send offers to nearby drivers
                val offers = sendOffersToDrivers(booking, nearbyDrivers)
                
                if (offers.isNotEmpty()) {
                    // Wait for driver response
                    val response = waitForDriverResponse(booking.id, offers)
                    
                    when (response) {
                        is DriverResponse.Accepted -> {
                            // Driver accepted - complete dispatch
                            acceptBookingByDriver(booking.id, response.driverId, response.offerId)
                            emitBookingStatusUpdate(booking.id, "driver_accepted", response.driverId)
                            return
                        }
                        is DriverResponse.Rejected -> {
                            rejectionCount++
                            // Mark booking as rejected and search next
                            updateBookingStatus(booking.id, "driver_rejected")
                            emitBookingStatusUpdate(booking.id, "driver_rejected", response.driverId)
                            
                            // Small delay before next search
                            delay(1000)
                        }
                        is DriverResponse.Timeout -> {
                            // No response - continue to next drivers
                            Log.d("DispatchService", "Offer timeout for booking ${booking.id}")
                        }
                        is DriverResponse.None -> {
                            // No offers pending - continue to next search
                        }
                    }
                }
            }

            // Expand radius and retry
            searchAttempts++
            currentRadius = minOf(currentRadius + 5, maxSearchRadiusKm)
            updateBookingSearchRadius(booking.id, currentRadius, searchAttempts)
            
            // Wait for timeout before next search
            delay(searchTimeoutSeconds * 1000L)
        }

        // No driver found after all attempts
        Log.w("DispatchService", "No driver found for booking ${booking.id} after $searchAttempts attempts")
        emitBookingStatusUpdate(booking.id, "cancelled", null, "No drivers available")
    }

    // ==================== DRIVER MATCHING ====================

    /**
     * Find nearby available drivers
     */
    suspend fun findNearbyDrivers(
        pickupLat: Double,
        pickupLon: Double,
        radiusKm: Double,
        vehicleCategory: String? = null
    ): List<NearbyDriver> {
        val nearbyDrivers = mutableListOf<NearbyDriver>()
        
        try {
            // Get all online, available, approved drivers
            val result = SupabaseService.getInstance().select<DriverRow>("drivers", 
                "?is_online=eq.true&is_available=eq.true&status=eq.active&current_booking_id=is.null"
            )
            
            result.onSuccess { drivers ->
                drivers.forEach { driver ->
                    if (driver.currentLatitude != null && driver.currentLongitude != null) {
                        val distance = calculateDistance(
                            pickupLat, pickupLon,
                            driver.currentLatitude, driver.currentLongitude
                        )
                        
                        if (distance <= radiusKm) {
                            nearbyDrivers.add(NearbyDriver(
                                driverId = driver.id,
                                name = driver.fullName ?: "",
                                distanceKm = distance,
                                rating = driver.averageRating ?: 0.0,
                                vehicleType = driver.vehicleType,
                                latitude = driver.currentLatitude,
                                longitude = driver.currentLongitude
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DispatchService", "Error finding nearby drivers", e)
        }

        // Sort by distance
        return nearbyDrivers.sortedBy { it.distanceKm }
    }

    /**
     * Calculate distance using Haversine formula
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return r * c
    }

    // ==================== OFFER MANAGEMENT ====================

    /**
     * Send booking offers to selected drivers
     */
    private suspend fun sendOffersToDrivers(
        booking: BookingEntity,
        drivers: List<NearbyDriver>
    ): List<BookingOffer> {
        val offers = mutableListOf<BookingOffer>()
        val expiresAt = System.currentTimeMillis() + (offerValiditySeconds * 1000)

        drivers.take(5).forEach { driver ->
            try {
                val offer = BookingOffer(
                    id = UUID.randomUUID().toString(),
                    bookingId = booking.id,
                    driverId = driver.driverId,
                    status = "pending",
                    offeredAt = System.currentTimeMillis(),
                    expiresAt = expiresAt,
                    distanceKm = driver.distanceKm,
                    etaMinutes = estimateEta(driver.distanceKm)
                )

                // Save offer to database
                SupabaseService.getInstance().insert("booking_offers", offer)
                
                // Send notification to driver
                sendDispatchNotification(
                    driverId = driver.driverId,
                    title = "New Ride Request!",
                    body = "A ${booking.bookingType} trip is available ${String.format("%.1f", driver.distanceKm)} km away. Tap to accept!",
                    bookingId = booking.id
                )

                offers.add(offer)
            } catch (e: Exception) {
                Log.e("DispatchService", "Error sending offer to driver ${driver.driverId}", e)
            }
        }

        return offers
    }

    /**
     * Estimate ETA based on distance
     */
    private fun estimateEta(distanceKm: Double): Int {
        // Assume average speed of 30 km/h in city
        return ((distanceKm / 30) * 60).toInt().coerceAtLeast(1)
    }

    /**
     * Wait for driver to respond to offer
     */
    private suspend fun waitForDriverResponse(
        bookingId: String,
        offers: List<BookingOffer>
    ): DriverResponse {
        val startTime = System.currentTimeMillis()
        val timeout = offerValiditySeconds * 1000L

        while (System.currentTimeMillis() - startTime < timeout) {
            // Check offer statuses
            val result = SupabaseService.getInstance().select<BookingOffer>(
                "booking_offers",
                "?booking_id=eq.$bookingId&status=in.(accepted,rejected)"
            )

            result.onSuccess { updatedOffers ->
                updatedOffers.forEach { offer ->
                    when (offer.status) {
                        "accepted" -> return DriverResponse.Accepted(offer.driverId, offer.id)
                        "rejected" -> return DriverResponse.Rejected(offer.driverId, offer.id)
                    }
                }
            }

            // Also check for new offers that might have been created
            val pendingOffers = SupabaseService.getInstance().select<BookingOffer>(
                "booking_offers",
                "?booking_id=eq.$bookingId&status=eq.pending"
            )
            
            if (pendingOffers.getOrNull()?.isEmpty() == true) {
                return DriverResponse.None
            }

            delay(500) // Poll every 500ms
        }

        return DriverResponse.Timeout
    }

    // ==================== BOOKING ACTIONS ====================

    /**
     * Accept booking - called when driver accepts
     */
    suspend fun acceptBooking(bookingId: String, driverId: String): Boolean {
        return try {
            // Find pending offer for this driver
            val offers = SupabaseService.getInstance().select<BookingOffer>(
                "booking_offers",
                "?booking_id=eq.$bookingId&driver_id=eq.$driverId&status=eq.pending"
            )

            val offer = offers.getOrNull()?.firstOrNull() ?: return false

            // Update booking
            val bookingUpdate = mapOf(
                "driver_id" to driverId,
                "status" to "driver_accepted",
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("bookings", bookingUpdate, bookingId)

            // Update driver
            val driverUpdate = mapOf(
                "is_available" to false,
                "current_booking_id" to bookingId,
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("drivers", driverUpdate, driverId)

            // Update offer
            val offerUpdate = mapOf(
                "status" to "accepted",
                "responded_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("booking_offers", offerUpdate, offer.id)

            // Cancel other offers
            cancelOtherOffers(bookingId, offer.id)

            // Notify customer
            val booking = getBookingById(bookingId)
            booking?.customerId?.let { customerId ->
                sendDispatchNotification(
                    userId = customerId,
                    userType = "customer",
                    title = "Driver Found!",
                    body = "A driver has accepted your ride. Check the app for details.",
                    bookingId = bookingId,
                    driverId = driverId
                )
            }

            emitBookingStatusUpdate(bookingId, "driver_accepted", driverId)
            true
        } catch (e: Exception) {
            Log.e("DispatchService", "Error accepting booking", e)
            false
        }
    }

    /**
     * Reject booking - called when driver rejects
     */
    suspend fun rejectBooking(bookingId: String, driverId: String, reason: String? = null): Boolean {
        return try {
            // Update offer
            val offers = SupabaseService.getInstance().select<BookingOffer>(
                "booking_offers",
                "?booking_id=eq.$bookingId&driver_id=eq.$driverId&status=eq.pending"
            )
            
            offers.getOrNull()?.firstOrNull()?.let { offer ->
                val offerUpdate = mapOf(
                    "status" to "rejected",
                    "responded_at" to System.currentTimeMillis().toString()
                )
                SupabaseService.getInstance().update("booking_offers", offerUpdate, offer.id)
            }

            // Update booking rejection count
            val booking = getBookingById(bookingId)
            booking?.let {
                val newRejectionCount = (it.rejectionCount ?: 0) + 1
                val bookingUpdate = mapOf(
                    "rejection_count" to newRejectionCount,
                    "status" to "driver_rejected",
                    "updated_at" to System.currentTimeMillis().toString()
                )
                SupabaseService.getInstance().update("bookings", bookingUpdate, bookingId)
                
                emitBookingStatusUpdate(bookingId, "driver_rejected", driverId)
            }

            true
        } catch (e: Exception) {
            Log.e("DispatchService", "Error rejecting booking", e)
            false
        }
    }

    /**
     * Cancel other pending offers for a booking
     */
    private suspend fun cancelOtherOffers(bookingId: String, acceptedOfferId: String) {
        try {
            val offers = SupabaseService.getInstance().select<BookingOffer>(
                "booking_offers",
                "?booking_id=eq.$bookingId&status=eq.pending"
            )
            
            offers.getOrNull()?.forEach { offer ->
                if (offer.id != acceptedOfferId) {
                    val update = mapOf("status" to "cancelled")
                    SupabaseService.getInstance().update("booking_offers", update, offer.id)
                    
                    // Notify driver that offer was cancelled
                    sendDispatchNotification(
                        driverId = offer.driverId,
                        title = "Ride Taken",
                        body = "Another driver has accepted this ride.",
                        bookingId = bookingId
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DispatchService", "Error cancelling other offers", e)
        }
    }

    // ==================== ADMIN ACTIONS ====================

    /**
     * Admin manually assigns a driver
     */
    suspend fun adminAssignDriver(bookingId: String, driverId: String, adminId: String): Boolean {
        return try {
            // Check booking is in assignable state
            val booking = getBookingById(bookingId)
            if (booking == null || !booking.status in listOf("new", "searching_driver", "driver_rejected", "driver_assigned")) {
                Log.w("DispatchService", "Cannot assign driver to booking ${bookingId} with status ${booking?.status}")
                return false
            }

            // Cancel any active dispatch
            cancelDispatch(bookingId)

            // Update booking
            val bookingUpdate = mapOf(
                "driver_id" to driverId,
                "status" to "driver_assigned",
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("bookings", bookingUpdate, bookingId)

            // Update driver
            val driverUpdate = mapOf(
                "is_available" to false,
                "current_booking_id" to bookingId,
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("drivers", driverUpdate, driverId)

            // Notify driver
            sendDispatchNotification(
                driverId = driverId,
                title = "Manual Assignment",
                body = "You have been assigned to a ride by admin. Please proceed to pickup.",
                bookingId = bookingId
            )

            // Record status change
            recordStatusChange(bookingId, booking?.status ?: "new", "driver_assigned", adminId, "admin")

            emitBookingStatusUpdate(bookingId, "driver_assigned", driverId)
            true
        } catch (e: Exception) {
            Log.e("DispatchService", "Error in admin assign driver", e)
            false
        }
    }

    /**
     * Admin cancels booking
     */
    suspend fun adminCancelBooking(bookingId: String, reason: String, adminId: String): Boolean {
        return try {
            cancelDispatch(bookingId)

            val booking = getBookingById(bookingId)
            val previousStatus = booking?.status ?: "unknown"

            // Update booking
            val bookingUpdate = mapOf(
                "status" to "cancelled",
                "cancelled_by" to "admin",
                "cancellation_reason" to reason,
                "cancellation_time" to System.currentTimeMillis().toString(),
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("bookings", bookingUpdate, bookingId)

            // Free up driver if assigned
            booking?.driverId?.let { driverId ->
                freeDriver(driverId)
            }

            // Cancel all offers
            val offers = SupabaseService.getInstance().select<BookingOffer>(
                "booking_offers",
                "?booking_id=eq.$bookingId&status=eq.pending"
            )
            offers.getOrNull()?.forEach { offer ->
                val update = mapOf("status" to "cancelled")
                SupabaseService.getInstance().update("booking_offers", update, offer.id)
            }

            // Record status change
            recordStatusChange(bookingId, previousStatus, "cancelled", adminId, "admin", reason)

            emitBookingStatusUpdate(bookingId, "cancelled", null, reason)
            true
        } catch (e: Exception) {
            Log.e("DispatchService", "Error cancelling booking", e)
            false
        }
    }

    // ==================== HELPERS ====================

    private suspend fun updateBookingStatus(bookingId: String, status: String) {
        try {
            val update = mapOf(
                "status" to status,
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("bookings", update, bookingId)
        } catch (e: Exception) {
            Log.e("DispatchService", "Error updating booking status", e)
        }
    }

    private suspend fun updateBookingSearchRadius(bookingId: String, radius: Double, attempts: Int) {
        try {
            val update = mapOf(
                "current_search_radius_km" to radius,
                "search_attempts" to attempts,
                "last_driver_notified_at" to System.currentTimeMillis().toString(),
                "timeout_at" to (System.currentTimeMillis() + searchTimeoutSeconds * 1000).toString()
            )
            SupabaseService.getInstance().update("bookings", update, bookingId)
        } catch (e: Exception) {
            Log.e("DispatchService", "Error updating search radius", e)
        }
    }

    private suspend fun getBookingById(bookingId: String): BookingEntity? {
        val result = SupabaseService.getInstance().select<BookingEntity>(
            "bookings",
            "?id=eq.$bookingId"
        )
        return result.getOrNull()?.firstOrNull()
    }

    private suspend fun freeDriver(driverId: String) {
        try {
            val update = mapOf(
                "is_available" to true,
                "current_booking_id" to null,
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("drivers", update, driverId)
        } catch (e: Exception) {
            Log.e("DispatchService", "Error freeing driver", e)
        }
    }

    private suspend fun acceptBookingByDriver(bookingId: String, driverId: String, offerId: String) {
        try {
            val update = mapOf(
                "driver_id" to driverId,
                "status" to "driver_accepted",
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("bookings", update, bookingId)
            
            val driverUpdate = mapOf(
                "is_available" to false,
                "current_booking_id" to bookingId,
                "updated_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().update("drivers", driverUpdate, driverId)
        } catch (e: Exception) {
            Log.e("DispatchService", "Error accepting booking", e)
        }
    }

    private suspend fun recordStatusChange(
        bookingId: String,
        oldStatus: String,
        newStatus: String,
        changedBy: String,
        changedByType: String,
        notes: String? = null
    ) {
        try {
            val history = mapOf(
                "booking_id" to bookingId,
                "old_status" to oldStatus,
                "new_status" to newStatus,
                "changed_by" to changedBy,
                "changed_by_type" to changedByType,
                "notes" to notes,
                "created_at" to System.currentTimeMillis().toString()
            )
            SupabaseService.getInstance().insert("booking_status_history", history)
        } catch (e: Exception) {
            Log.e("DispatchService", "Error recording status change", e)
        }
    }

    private fun sendDispatchNotification(
        userId: String? = null,
        userType: String? = null,
        driverId: String? = null,
        title: String,
        body: String,
        bookingId: String,
        driverIdForData: String? = null
    ) {
        scope.launch {
            try {
                val targetUserId = userId ?: driverId ?: return@launch
                val targetUserType = userType ?: if (driverId != null) "driver" else "customer"
                
                val notification = mapOf(
                    "user_id" to targetUserId,
                    "user_type" to targetUserType,
                    "title" to title,
                    "body" to body,
                    "data" to mapOf(
                        "type" to "dispatch",
                        "booking_id" to bookingId,
                        "driver_id" to (driverIdForData ?: "")
                    ),
                    "notification_type" to "dispatch",
                    "created_at" to System.currentTimeMillis().toString()
                )
                SupabaseService.getInstance().insert("notifications", notification)
            } catch (e: Exception) {
                Log.e("DispatchService", "Error sending notification", e)
            }
        }
    }

    private fun emitBookingStatusUpdate(bookingId: String, status: String, driverId: String?, reason: String? = null) {
        _bookingStatusUpdates.value = BookingStatusUpdate(
            bookingId = bookingId,
            status = status,
            driverId = driverId,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
    }

    // ==================== CLEANUP ====================

    fun cleanup() {
        activeDispatches.values.forEach { it.cancel() }
        activeDispatches.clear()
        scope.cancel()
    }
}

// ==================== DATA CLASSES ====================

data class NearbyDriver(
    val driverId: String,
    val name: String,
    val distanceKm: Double,
    val rating: Double,
    val vehicleType: String?,
    val latitude: Double,
    val longitude: Double
)

data class BookingOffer(
    val id: String,
    val booking_id: String,
    val driver_id: String,
    val status: String,
    val offered_at: Long,
    val expires_at: Long,
    val distance_km: Double?,
    val eta_minutes: Int?
)

data class BookingEntity(
    val id: String,
    val booking_number: String?,
    val customer_id: String?,
    val driver_id: String?,
    val vehicle_id: String?,
    val booking_type: String,
    val vehicle_category: String?,
    val pickup_address: String?,
    val pickup_latitude: Double,
    val pickup_longitude: Double,
    val drop_address: String?,
    val drop_latitude: Double?,
    val drop_longitude: Double?,
    val trip_date: String?,
    val total_fare: Double?,
    val status: String,
    val current_search_radius_km: Double?,
    val search_attempts: Int?,
    val rejection_count: Int?,
    val trip_otp: String?,
    val created_at: Long?
)

data class DriverRow(
    val id: String,
    val full_name: String?,
    val current_latitude: Double?,
    val current_longitude: Double?,
    val average_rating: Double?,
    val vehicle_type: String?
)

data class DispatchConfigRow(
    val config_key: String,
    val config_value: String
)

data class BookingStatusUpdate(
    val bookingId: String,
    val status: String,
    val driverId: String?,
    val reason: String?,
    val timestamp: Long
)

sealed class DriverResponse {
    data class Accepted(val driverId: String, val offerId: String) : DriverResponse()
    data class Rejected(val driverId: String, val offerId: String) : DriverResponse()
    data object Timeout : DriverResponse()
    data object None : DriverResponse()
}
