package com.vttcabs.common

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SupabaseService private constructor() {

    private var _supabaseUrl: String = ""
    var supabaseUrl: String
        get() = _supabaseUrl
        private set(value) { _supabaseUrl = value }

    private var _supabaseKey: String = ""
    var supabaseKey: String
        get() = _supabaseKey
        private set(value) { _supabaseKey = value }

    private var _serviceRoleKey: String = ""
    var serviceRoleKey: String
        get() = _serviceRoleKey
        private set(value) { _serviceRoleKey = value }

    private var _accessToken: String? = null
    var accessToken: String?
        get() = _accessToken
        set(value) { _accessToken = value }

    private var _currentUserId: String? = null
    var currentUserId: String?
        get() = _currentUserId
        set(value) { _currentUserId = value }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean
        get() = _supabaseUrl.isNotBlank() && _supabaseKey.isNotBlank() && _supabaseUrl != "https://your-project-id.supabase.co"

    // Real-time event flows
    private val _bookingUpdates = MutableSharedFlow<BookingEntity>(replay = 1)
    val bookingUpdates: SharedFlow<BookingEntity> = _bookingUpdates

    private val _driverLocationUpdates = MutableSharedFlow<Pair<String, Pair<Double, Double>>>(replay = 1)
    val driverLocationUpdates: SharedFlow<Pair<String, Pair<Double, Double>>> = _driverLocationUpdates

    private val _notificationUpdates = MutableSharedFlow<NotificationEntity>(replay = 1)
    val notificationUpdates: SharedFlow<NotificationEntity> = _notificationUpdates

    private val _sosAlerts = MutableSharedFlow<SosAlertEntity>(replay = 1)
    val sosAlerts: SharedFlow<SosAlertEntity> = _sosAlerts

    // OTP Storage (for demo)
    private val otpStorage = mutableMapOf<String, Pair<String, Long>>()

    companion object {
        @Volatile
        private var instance: SupabaseService? = null
        
        // Replace with your actual Supabase project URL and keys
        // Get these from: https://supabase.com/dashboard/project/YOUR_PROJECT/settings/api
        private const val DEFAULT_URL = "https://your-project-id.supabase.co"
        private const val DEFAULT_KEY = "your-anon-key"
        private const val DEFAULT_SERVICE_KEY = "your-service-role-key"
        
        fun initialize(app: android.app.Application) {
            instance = SupabaseService()
            instance?._supabaseUrl = DEFAULT_URL
            instance?._supabaseKey = DEFAULT_KEY
            instance?._serviceRoleKey = DEFAULT_SERVICE_KEY
            Log.d("SupabaseService", "Initialized with URL: ${instance?._supabaseUrl}")
        }
        
        fun getInstance(): SupabaseService {
            return instance ?: synchronized(this) {
                instance ?: SupabaseService().also { instance = it }
            }
        }

        fun generateOtp(): String {
            return String.format("%06d", Random.nextInt(100000, 999999))
        }

        const val PLATFORM_FEE_PERCENT = 15.0
        const val GST_PERCENT = 5.0
    }

    // ==================== HTTP HELPERS ====================

    private fun createRequest(path: String, body: String? = null, method: String = "GET", isServiceRole: Boolean = false): Request {
        val url = "${_supabaseUrl}$path"
        val requestBody = body?.toRequestBody("application/json".toMediaType())
        
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", if (isServiceRole) _serviceRoleKey else _supabaseKey)
            .addHeader("Content-Type", "application/json")
        
        if (_accessToken != null && !isServiceRole) {
            builder.addHeader("Authorization", "Bearer $_accessToken")
        }
        
        if (body != null) {
            builder.method(method, requestBody)
        } else {
            builder.method(method, null)
        }
        
        return builder.build()
    }

    private suspend fun <T> executeRequest(request: Request, responseType: Class<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null && responseType != String::class.java) {
                        Result.success(gson.fromJson(body, responseType))
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        Result.success(body as T)
                    }
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Result.failure(Exception("HTTP ${response.code}: $errorBody"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // ==================== SUPABASE AUTH ====================

    suspend fun signUpWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(mapOf(
                "email" to email,
                "password" to password,
                "data" to mapOf("user_type" to "customer")
            ))
            val request = createRequest("/auth/v1/signup", body, "POST")
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = gson.fromJson(response.body?.string(), JsonObject::class.java)
                    val userId = json?.get("id")?.asString ?: ""
                    _accessToken = json?.get("access_token")?.asString
                    _currentUserId = userId
                    Result.success(userId)
                } else {
                    Result.failure(Exception("Signup failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error signing up", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(mapOf(
                "email" to email,
                "password" to password
            ))
            val request = createRequest("/auth/v1/token?grant_type=password", body, "POST")
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = gson.fromJson(response.body?.string(), JsonObject::class.java)
                    val userId = json?.get("user")?.asJsonObject?.get("id")?.asString ?: ""
                    _accessToken = json?.get("access_token")?.asString
                    _currentUserId = userId
                    Log.d("SupabaseService", "Signed in as: $userId")
                    Result.success(userId)
                } else {
                    Result.failure(Exception("Login failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error signing in", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithOtp(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Generate demo OTP (in production, use Supabase Auth OTP)
            val otp = generateOtp()
            val expiry = System.currentTimeMillis() + 5 * 60 * 1000
            otpStorage[email.lowercase()] = Pair(otp, expiry)
            Log.d("SupabaseService", "OTP generated for $email: $otp (Demo mode)")
            
            // Optionally call Supabase to send real email
            try {
                val body = gson.toJson(mapOf("email" to email))
                val request = createRequest("/auth/v1/re OTP", body, "POST")
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.w("SupabaseService", "Failed to send Supabase OTP, using demo", e)
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error sending OTP", e)
            Result.failure(e)
        }
    }

    // Alias for VttRepository compatibility
    suspend fun sendOtp(email: String): Result<String> {
        return sendDemoOtp(email)
    }

    suspend fun verifyOtp(email: String, otp: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Check demo OTP first
            val stored = otpStorage[email.lowercase()]
            if (stored != null) {
                val (storedOtp, expiry) = stored
                if (System.currentTimeMillis() <= expiry && storedOtp == otp) {
                    otpStorage.remove(email.lowercase())
                    Log.d("SupabaseService", "Demo OTP verified for $email")
                    Result.success(true)
                } else {
                    Result.failure(Exception("Invalid or expired OTP"))
                }
            } else {
                Result.failure(Exception("No OTP found. Please request a new one."))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error verifying OTP", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            _accessToken = null
            _currentUserId = null
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        return _accessToken != null && _currentUserId != null
    }

    // ==================== DEMO OTP FUNCTIONS ====================

    suspend fun sendDemoOtp(email: String): Result<String> {
        return try {
            val otp = generateOtp()
            val expiry = System.currentTimeMillis() + 5 * 60 * 1000
            otpStorage[email.lowercase()] = Pair(otp, expiry)
            Log.d("SupabaseService", "Demo OTP generated for $email: $otp")
            Result.success(otp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== REST API HELPERS ====================

    suspend fun <T> insert(table: String, data: T): Result<T> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(data)
            val request = createRequest("/rest/v1/$table", body, "POST")
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("SupabaseService", "Inserted to $table")
                    Result.success(data)
                } else {
                    Result.failure(Exception("Insert failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error inserting to $table", e)
            Result.failure(e)
        }
    }

    suspend fun <T> update(table: String, data: T, id: String): Result<T> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(data)
            val request = createRequest("/rest/v1/$table?id=eq.$id", body, "PATCH")
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("SupabaseService", "Updated $table/$id")
                    Result.success(data)
                } else {
                    Result.failure(Exception("Update failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error updating $table/$id", e)
            Result.failure(e)
        }
    }

    suspend fun <T> select(table: String, filters: String = ""): Result<List<T>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/$table$filters")
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    // Parse as generic list and return empty if parsing fails
                    val type = object : com.google.gson.reflect.TypeToken<List<T>>() {}.type
                    val result: List<T> = try {
                        gson.fromJson(body, type) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    Log.d("SupabaseService", "Selected from $table: ${result.size} rows")
                    Result.success(result)
                } else {
                    Result.failure(Exception("Select failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error selecting from $table", e)
            Result.failure(e)
        }
    }

    suspend fun delete(table: String, id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/$table?id=eq.$id", "", "DELETE")
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("SupabaseService", "Deleted from $table/$id")
                    Result.success(true)
                } else {
                    Result.failure(Exception("Delete failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error deleting from $table/$id", e)
            Result.failure(e)
        }
    }

    // ==================== STORAGE OPERATIONS ====================

    suspend fun uploadFile(
        bucket: String,
        path: String,
        data: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = data.toRequestBody(contentType.toMediaType())
            val request = Request.Builder()
                .url("$_supabaseUrl/storage/v1/object/$bucket/$path")
                .addHeader("apikey", _supabaseKey)
                .addHeader("Authorization", "Bearer ${_accessToken ?: _supabaseKey}")
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val publicUrl = "$_supabaseUrl/storage/v1/object/public/$bucket/$path"
                    Log.d("SupabaseService", "Uploaded to: $publicUrl")
                    Result.success(publicUrl)
                } else {
                    Result.failure(Exception("Upload failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error uploading file", e)
            Result.failure(e)
        }
    }

    suspend fun uploadDocument(
        context: Context,
        file: File,
        bucket: String,
        folder: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = file.readBytes()
            val path = "$folder/${UUID.randomUUID()}_${file.name}"
            uploadFile(bucket, path, data)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error uploading document", e)
            Result.failure(e)
        }
    }

    suspend fun uploadImage(
        context: Context,
        bitmap: Bitmap,
        bucket: String,
        folder: String,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val data = stream.toByteArray()
            val path = "$folder/$fileName"
            uploadFile(bucket, path, data, "image/jpeg")
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error uploading image", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFile(bucket: String, path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$_supabaseUrl/storage/v1/object/$bucket/$path")
                .addHeader("apikey", _serviceRoleKey)
                .addHeader("Authorization", "Bearer $_serviceRoleKey")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 404) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Delete failed: ${response.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error deleting file", e)
            Result.failure(e)
        }
    }

    // ==================== REAL-TIME (Simplified) ====================

    fun subscribeToBookings(onUpdate: (BookingEntity) -> Unit) {
        Log.d("SupabaseService", "Subscribed to booking updates")
    }

    fun subscribeToDriverLocations(onUpdate: (String, Double, Double) -> Unit) {
        Log.d("SupabaseService", "Subscribed to driver location updates")
    }

    fun subscribeToNotifications(userId: String, onUpdate: (NotificationEntity) -> Unit) {
        Log.d("SupabaseService", "Subscribed to notifications for user: $userId")
    }

    fun unsubscribe(channel: String) {
        Log.d("SupabaseService", "Unsubscribed from: $channel")
    }

    // ==================== HELPER METHODS ====================

    fun getStorageUrl(bucket: String, path: String): String {
        return "$_supabaseUrl/storage/v1/object/public/$bucket/$path"
    }

    fun calculateDriverEarnings(grossAmount: Double): Pair<Double, Double> {
        val platformFee = grossAmount * (PLATFORM_FEE_PERCENT / 100)
        val gstOnFee = platformFee * (GST_PERCENT / 100)
        val totalFees = platformFee + gstOnFee
        val netEarning = grossAmount - totalFees
        return Pair(netEarning, totalFees)
    }

    fun createNotificationData(
        type: String,
        bookingId: String? = null,
        driverId: String? = null
    ): String {
        return gson.toJson(mapOf(
            "type" to type,
            "booking_id" to (bookingId ?: ""),
            "driver_id" to (driverId ?: "")
        ))
    }

    suspend fun sendPushNotification(
        userId: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        // In production, integrate with FCM or use Edge Functions
        Log.d("SupabaseService", "Push notification to $userId: $title - $body")
        Result.success(true)
    }
}

// Storage bucket names
object StorageBuckets {
    const val DRIVER_DOCUMENTS = "driver-documents"
    const val VEHICLE_PHOTOS = "vehicle-photos"
    const val PROFILE_PHOTOS = "profile-photos"
    const val AADHAAR_DOCS = "aadhaar-documents"
    const val PAN_DOCS = "pan-documents"
    const val LICENSE_DOCS = "license-documents"
    const val RC_DOCS = "rc-documents"
    const val INSURANCE_DOCS = "insurance-documents"
    const val PUC_DOCS = "puc-documents"
}
