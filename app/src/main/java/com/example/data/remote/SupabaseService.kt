package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.BookingEntity
import com.example.data.model.DriverEntity
import com.example.data.model.UserEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    private val supabaseKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("your-supabase-project") &&
                supabaseKey.isNotBlank() &&
                !supabaseKey.contains("your-supabase-anon-key")

    suspend fun signUp(email: String, pass: String, name: String): String? = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseService", "Supabase not configured. Using local fallback mode.")
            return@withContext "local_user_${System.currentTimeMillis()}"
        }

        try {
            val json = """
                {
                    "email": "$email",
                    "password": "$pass",
                    "data": {
                        "full_name": "$name"
                    }
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/signup")
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    Log.d("SupabaseService", "Sign Up Success: $bodyString")
                    return@withContext "sb_usr_${System.currentTimeMillis()}"
                } else {
                    Log.e("SupabaseService", "Sign Up Failed: ${response.code} ${response.message}")
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sign Up Exception: ${e.localizedMessage}")
            return@withContext null
        }
    }

    suspend fun syncBookingToSupabase(booking: BookingEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false

        try {
            val json = """
                {
                    "id": "${booking.id}",
                    "customer_id": "${booking.customerId}",
                    "customer_name": "${booking.customerName}",
                    "customer_phone": "${booking.customerPhone}",
                    "driver_id": ${if (booking.driverId != null) "\"${booking.driverId}\"" else "null"},
                    "driver_name": ${if (booking.driverName != null) "\"${booking.driverName}\"" else "null"},
                    "vehicle_category": "${booking.vehicleCategory.name}",
                    "booking_type": "${booking.bookingType.name}",
                    "pickup_address": "${booking.pickupAddress.replace("\"", "\\\"")}",
                    "drop_address": "${booking.dropAddress.replace("\"", "\\\"")}",
                    "pickup_lat": ${booking.pickupLat},
                    "pickup_lng": ${booking.pickupLng},
                    "drop_lat": ${booking.dropLat},
                    "drop_lng": ${booking.dropLng},
                    "distance_km": ${booking.distanceKm},
                    "total_fare": ${booking.totalFare},
                    "booking_status": "${booking.bookingStatus.name}",
                    "otp": "${booking.otp}",
                    "created_at": ${booking.createdAt},
                    "pickup_date": "${booking.pickupDate}",
                    "pickup_time": "${booking.pickupTime}"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/bookings")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val isOk = response.isSuccessful
                Log.d("SupabaseService", "Sync Booking to Supabase status: ${response.code}")
                return@withContext isOk
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sync booking error: ${e.localizedMessage}")
            return@withContext false
        }
    }

    suspend fun fetchBookingsFromSupabase(): List<BookingEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()

        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/bookings?select=*")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@withContext emptyList()
                    val jsonArray = org.json.JSONArray(bodyString)
                    val result = mutableListOf<BookingEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.optString("id")
                        val customerId = obj.optString("customer_id", "cust_101")
                        val customerName = obj.optString("customer_name", "Customer")
                        val customerPhone = obj.optString("customer_phone", "+91 9876543210")
                        val driverId = if (obj.isNull("driver_id")) null else obj.optString("driver_id")
                        val driverName = if (obj.isNull("driver_name")) null else obj.optString("driver_name")
                        val vehicleCategoryStr = obj.optString("vehicle_category", "SEDAN")
                        val vehicleCategory = try { com.example.data.model.VehicleCategory.valueOf(vehicleCategoryStr) } catch (e: Exception) { com.example.data.model.VehicleCategory.SEDAN }
                        val bookingTypeStr = obj.optString("booking_type", "ONE_WAY")
                        val bookingType = try { com.example.data.model.BookingType.valueOf(bookingTypeStr) } catch (e: Exception) { com.example.data.model.BookingType.ONE_WAY }
                        val pickupAddress = obj.optString("pickup_address", "")
                        val dropAddress = obj.optString("drop_address", "")
                        val pickupLat = obj.optDouble("pickup_lat", 12.9781)
                        val pickupLng = obj.optDouble("pickup_lng", 77.5697)
                        val dropLat = obj.optDouble("drop_lat", 13.1986)
                        val dropLng = obj.optDouble("drop_lng", 77.7066)
                        val distanceKm = obj.optDouble("distance_km", 0.0)
                        val totalFare = obj.optDouble("total_fare", 0.0)
                        val bookingStatusStr = obj.optString("booking_status", "SEARCHING")
                        val bookingStatus = try { com.example.data.model.BookingStatus.valueOf(bookingStatusStr) } catch (e: Exception) { com.example.data.model.BookingStatus.SEARCHING }
                        val otp = obj.optString("otp", "1234")
                        val createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        val pickupDate = obj.optString("pickup_date", "")
                        val pickupTime = obj.optString("pickup_time", "")

                        val booking = BookingEntity(
                            id = id,
                            customerId = customerId,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            driverId = driverId,
                            driverName = driverName,
                            vehicleCategory = vehicleCategory,
                            bookingType = bookingType,
                            pickupAddress = pickupAddress,
                            dropAddress = dropAddress,
                            pickupLat = pickupLat,
                            pickupLng = pickupLng,
                            dropLat = dropLat,
                            dropLng = dropLng,
                            distanceKm = distanceKm,
                            durationMins = com.example.domain.calculator.LocationUtils.estimateTripDurationMins(distanceKm),
                            totalFare = totalFare,
                            baseFare = 100.0,
                            distanceFare = totalFare - 100.0,
                            timeFare = 0.0,
                            paymentMethod = com.example.data.model.PaymentMethod.UPI,
                            bookingStatus = bookingStatus,
                            otp = otp,
                            createdAt = createdAt,
                            pickupDate = pickupDate,
                            pickupTime = pickupTime
                        )
                        result.add(booking)
                    }
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Fetch bookings error: ${e.localizedMessage}")
        }
        emptyList()
    }
}
