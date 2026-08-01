package com.vttcabs.admin.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vttcabs.admin.VttAdminApp
import com.vttcabs.admin.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    
    private val firestore = VttAdminApp.instance.firestore
    
    companion object {
        private const val DRIVERS_COLLECTION = "drivers"
        private const val CUSTOMERS_COLLECTION = "users"
        private const val BOOKINGS_COLLECTION = "bookings"
        private const val VEHICLES_COLLECTION = "vehicles"
        private const val FARE_RULES_COLLECTION = "fare_rules"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
    }
    
    // ========== DRIVERS ==========
    
    fun getAllDrivers(): Flow<List<Driver>> = flow {
        val snapshot = firestore.collection(DRIVERS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        val drivers = snapshot.documents.mapNotNull { doc ->
            try {
                Driver(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    vehicleCategory = doc.getString("vehicleCategory") ?: "",
                    vehicleModel = doc.getString("vehicleModel") ?: "",
                    vehicleNumber = doc.getString("vehicleNumber") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    totalTrips = doc.getLong("totalTrips")?.toInt() ?: 0,
                    isOnline = doc.getBoolean("isOnline") ?: false,
                    isActive = doc.getBoolean("isActive") ?: true,
                    approvalStatus = doc.getString("approvalStatus") ?: "PENDING",
                    currentLat = doc.getDouble("currentLat") ?: 0.0,
                    currentLng = doc.getDouble("currentLng") ?: 0.0,
                    totalEarnings = doc.getDouble("totalEarnings") ?: 0.0,
                    walletBalance = doc.getDouble("walletBalance") ?: 0.0,
                    documents = DriverDocuments(
                        aadhaarNumber = doc.getString("aadhaarNumber") ?: "",
                        aadhaarFrontUrl = doc.getString("aadhaarFrontUrl") ?: "",
                        aadhaarBackUrl = doc.getString("aadhaarBackUrl") ?: "",
                        panCardNumber = doc.getString("panCardNumber") ?: "",
                        panCardUrl = doc.getString("panCardUrl") ?: "",
                        licenceNumber = doc.getString("licenceNumber") ?: "",
                        licenceFrontUrl = doc.getString("licenceFrontUrl") ?: "",
                        licenceBackUrl = doc.getString("licenceBackUrl") ?: "",
                        rcUrl = doc.getString("rcUrl") ?: "",
                        insuranceUrl = doc.getString("insuranceUrl") ?: "",
                        pucUrl = doc.getString("pucUrl") ?: "",
                        permitUrl = doc.getString("permitUrl") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: ""
                    ),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(drivers)
    }
    
    suspend fun getDriverById(driverId: String): Driver? {
        val doc = firestore.collection(DRIVERS_COLLECTION).document(driverId).get().await()
        if (!doc.exists()) return null
        return Driver(
            id = doc.id,
            name = doc.getString("name") ?: "",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            vehicleCategory = doc.getString("vehicleCategory") ?: "",
            vehicleModel = doc.getString("vehicleModel") ?: "",
            vehicleNumber = doc.getString("vehicleNumber") ?: "",
            rating = doc.getDouble("rating") ?: 0.0,
            totalTrips = doc.getLong("totalTrips")?.toInt() ?: 0,
            isOnline = doc.getBoolean("isOnline") ?: false,
            isActive = doc.getBoolean("isActive") ?: true,
            approvalStatus = doc.getString("approvalStatus") ?: "PENDING",
            currentLat = doc.getDouble("currentLat") ?: 0.0,
            currentLng = doc.getDouble("currentLng") ?: 0.0,
            totalEarnings = doc.getDouble("totalEarnings") ?: 0.0,
            walletBalance = doc.getDouble("walletBalance") ?: 0.0,
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }
    
    suspend fun updateDriverStatus(driverId: String, status: String, isActive: Boolean = true) {
        val updates = mapOf(
            "approvalStatus" to status,
            "isActive" to isActive
        )
        firestore.collection(DRIVERS_COLLECTION).document(driverId).update(updates).await()
    }
    
    suspend fun updateDriverOnlineStatus(driverId: String, isOnline: Boolean) {
        firestore.collection(DRIVERS_COLLECTION)
            .document(driverId)
            .update("isOnline", isOnline)
            .await()
    }
    
    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        val updates = mapOf(
            "currentLat" to lat,
            "currentLng" to lng
        )
        firestore.collection(DRIVERS_COLLECTION).document(driverId).update(updates).await()
    }
    
    // ========== CUSTOMERS ==========
    
    fun getAllCustomers(): Flow<List<Customer>> = flow {
        val snapshot = firestore.collection(CUSTOMERS_COLLECTION)
            .whereEqualTo("role", "CUSTOMER")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        val customers = snapshot.documents.mapNotNull { doc ->
            try {
                Customer(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    isBlocked = doc.getBoolean("isBlocked") ?: false,
                    totalBookings = doc.getLong("totalBookings")?.toInt() ?: 0,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(customers)
    }
    
    suspend fun updateCustomerBlockedStatus(customerId: String, isBlocked: Boolean) {
        val updates = mapOf("isBlocked" to isBlocked)
        firestore.collection(CUSTOMERS_COLLECTION).document(customerId).update(updates).await()
    }
    
    // ========== BOOKINGS ==========
    
    fun getAllBookings(): Flow<List<Booking>> = flow {
        val snapshot = firestore.collection(BOOKINGS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        val bookings = snapshot.documents.mapNotNull { doc ->
            try {
                Booking(
                    id = doc.id,
                    customerId = doc.getString("customerId") ?: "",
                    customerName = doc.getString("customerName") ?: "",
                    customerPhone = doc.getString("customerPhone") ?: "",
                    driverId = doc.getString("driverId"),
                    driverName = doc.getString("driverName"),
                    driverPhone = doc.getString("driverPhone"),
                    vehicleNumber = doc.getString("vehicleNumber"),
                    vehicleModel = doc.getString("vehicleModel"),
                    vehicleCategory = doc.getString("vehicleCategory") ?: "",
                    bookingType = doc.getString("bookingType") ?: "",
                    pickupAddress = doc.getString("pickupAddress") ?: "",
                    dropAddress = doc.getString("dropAddress") ?: "",
                    pickupLat = doc.getDouble("pickupLat") ?: 0.0,
                    pickupLng = doc.getDouble("pickupLng") ?: 0.0,
                    dropLat = doc.getDouble("dropLat") ?: 0.0,
                    dropLng = doc.getDouble("dropLng") ?: 0.0,
                    distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                    durationMins = doc.getLong("durationMins")?.toInt() ?: 0,
                    totalFare = doc.getDouble("totalFare") ?: 0.0,
                    paymentMethod = doc.getString("paymentMethod") ?: "CASH",
                    paymentStatus = doc.getString("paymentStatus") ?: "PENDING",
                    bookingStatus = doc.getString("bookingStatus") ?: "PENDING",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    pickupDate = doc.getString("pickupDate") ?: "",
                    pickupTime = doc.getString("pickupTime") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(bookings)
    }
    
    suspend fun assignDriverToBooking(bookingId: String, driverId: String, driverName: String, driverPhone: String, vehicleNumber: String, vehicleModel: String) {
        val updates = mapOf(
            "driverId" to driverId,
            "driverName" to driverName,
            "driverPhone" to driverPhone,
            "vehicleNumber" to vehicleNumber,
            "vehicleModel" to vehicleModel,
            "bookingStatus" to "ASSIGNED"
        )
        firestore.collection(BOOKINGS_COLLECTION).document(bookingId).update(updates).await()
    }
    
    suspend fun updateBookingStatus(bookingId: String, status: String) {
        firestore.collection(BOOKINGS_COLLECTION)
            .document(bookingId)
            .update("bookingStatus", status)
            .await()
    }
    
    // ========== VEHICLES ==========
    
    fun getAllVehicles(): Flow<List<Vehicle>> = flow {
        val snapshot = firestore.collection(VEHICLES_COLLECTION)
            .orderBy("category")
            .get()
            .await()
        val vehicles = snapshot.documents.mapNotNull { doc ->
            try {
                Vehicle(
                    id = doc.id,
                    category = doc.getString("category") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    baseFare = doc.getDouble("baseFare") ?: 0.0,
                    perKmRate = doc.getDouble("perKmRate") ?: 0.0,
                    perMinRate = doc.getDouble("perMinRate") ?: 0.0,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(vehicles)
    }
    
    suspend fun addVehicle(vehicle: Vehicle) {
        val docRef = firestore.collection(VEHICLES_COLLECTION).document()
        val vehicleWithId = vehicle.copy(id = docRef.id)
        firestore.collection(VEHICLES_COLLECTION).document(docRef.id).set(vehicleWithId).await()
    }
    
    suspend fun updateVehicle(vehicle: Vehicle) {
        firestore.collection(VEHICLES_COLLECTION).document(vehicle.id).set(vehicle).await()
    }
    
    suspend fun deleteVehicle(vehicleId: String) {
        firestore.collection(VEHICLES_COLLECTION).document(vehicleId).delete().await()
    }
    
    // ========== FARE RULES ==========
    
    fun getAllFareRules(): Flow<List<FareRule>> = flow {
        val snapshot = firestore.collection(FARE_RULES_COLLECTION).get().await()
        val rules = snapshot.documents.mapNotNull { doc ->
            try {
                FareRule(
                    id = doc.id,
                    vehicleCategory = doc.getString("vehicleCategory") ?: "",
                    baseFare = doc.getDouble("baseFare") ?: 0.0,
                    perKmRate = doc.getDouble("perKmRate") ?: 0.0,
                    perMinRate = doc.getDouble("perMinRate") ?: 0.0,
                    minimumFare = doc.getDouble("minimumFare") ?: 0.0,
                    surgeMultiplier = doc.getDouble("surgeMultiplier") ?: 1.0
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(rules)
    }
    
    suspend fun updateFareRule(rule: FareRule) {
        firestore.collection(FARE_RULES_COLLECTION).document(rule.id).set(rule).await()
    }
    
    // ========== NOTIFICATIONS ==========
    
    suspend fun sendNotification(userId: String, title: String, message: String, type: String = "GENERAL") {
        val notification = Notification(
            userId = userId,
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection(NOTIFICATIONS_COLLECTION).add(notification).await()
    }
    
    suspend fun sendBroadcastNotification(title: String, message: String, type: String = "BROADCAST") {
        // Send to all users (simulated as single notification)
        val notification = Notification(
            userId = "all",
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection(NOTIFICATIONS_COLLECTION).add(notification).await()
    }
    
    // ========== DASHBOARD STATS ==========
    
    suspend fun getDashboardStats(): DashboardStats {
        val driversSnapshot = firestore.collection(DRIVERS_COLLECTION).get().await()
        val customersSnapshot = firestore.collection(CUSTOMERS_COLLECTION)
            .whereEqualTo("role", "CUSTOMER")
            .get().await()
        val bookingsSnapshot = firestore.collection(BOOKINGS_COLLECTION).get().await()
        
        val totalDrivers = driversSnapshot.size()
        val activeDrivers = driversSnapshot.documents.count { it.getBoolean("isOnline") == true }
        val totalCustomers = customersSnapshot.size()
        
        val allBookings = bookingsSnapshot.documents
        val totalBookings = allBookings.size
        val completedTrips = allBookings.count { 
            it.getString("bookingStatus") in listOf("COMPLETED", "TRIP_COMPLETED") 
        }
        val cancelledTrips = allBookings.count { 
            it.getString("bookingStatus") == "CANCELLED" 
        }
        val activeTrips = allBookings.count { 
            it.getString("bookingStatus") in listOf("ASSIGNED", "ACCEPTED", "DRIVER_ARRIVING", "ARRIVED", "IN_PROGRESS") 
        }
        
        val totalRevenue = allBookings
            .filter { it.getString("paymentStatus") == "COMPLETED" }
            .sumOf { it.getDouble("totalFare") ?: 0.0 }
        
        return DashboardStats(
            totalBookings = totalBookings,
            activeTrips = activeTrips,
            completedTrips = completedTrips,
            cancelledTrips = cancelledTrips,
            totalDrivers = totalDrivers,
            activeDrivers = activeDrivers,
            totalCustomers = totalCustomers,
            totalRevenue = totalRevenue
        )
    }
}
