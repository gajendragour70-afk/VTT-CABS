package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverEntity
import com.example.data.model.FareRuleEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface VttDao {
    // Users
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Drivers
    @Query("SELECT * FROM drivers")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE isOnline = 1 AND approvalStatus = 'APPROVED'")
    fun getOnlineDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getDriverById(id: String): DriverEntity?

    @Query("SELECT * FROM drivers WHERE email = :email LIMIT 1")
    suspend fun getDriverByEmail(email: String): DriverEntity?

    @Query("SELECT * FROM drivers WHERE phone = :phone LIMIT 1")
    suspend fun getDriverByPhone(phone: String): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Query("UPDATE drivers SET approvalStatus = :status, isApproved = :isApproved, rejectionReason = :reason WHERE id = :driverId")
    suspend fun updateDriverApprovalStatus(driverId: String, status: DriverApprovalStatus, isApproved: Boolean, reason: String = "")

    @Query("UPDATE drivers SET reuploadRequestedDocs = :docs WHERE id = :driverId")
    suspend fun updateReuploadRequestedDocs(driverId: String, docs: String)

    @Query("UPDATE drivers SET isOnline = :isOnline WHERE id = :driverId")
    suspend fun updateDriverOnlineStatus(driverId: String, isOnline: Boolean)

    @Query("UPDATE drivers SET currentLat = :lat, currentLng = :lng WHERE id = :driverId")
    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double)

    @Query("UPDATE drivers SET totalEarnings = totalEarnings + :amount, totalTrips = totalTrips + 1 WHERE id = :driverId")
    suspend fun addDriverEarnings(driverId: String, amount: Double)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerBookings(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getDriverBookings(driverId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun observeBookingById(id: String): Flow<BookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET bookingStatus = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

    @Query("UPDATE bookings SET driverId = :driverId, driverName = :driverName, driverPhone = :driverPhone, vehicleNumber = :vehicleNumber, vehicleModel = :vehicleModel, bookingStatus = 'ASSIGNED', assignedAt = :assignedAt WHERE id = :bookingId")
    suspend fun assignDriverToBooking(
        bookingId: String,
        driverId: String,
        driverName: String,
        driverPhone: String,
        vehicleNumber: String,
        vehicleModel: String,
        assignedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bookings SET driverId = NULL, driverName = NULL, driverPhone = NULL, vehicleNumber = NULL, vehicleModel = NULL, bookingStatus = 'PENDING', assignedAt = 0 WHERE id = :bookingId")
    suspend fun unassignDriverFromBooking(bookingId: String)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserNotifications(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    // Fare Rules
    @Query("SELECT * FROM fare_rules")
    fun getFareRules(): Flow<List<FareRuleEntity>>

    @Query("SELECT * FROM fare_rules WHERE vehicleCategory = :category LIMIT 1")
    suspend fun getFareRuleForCategory(category: VehicleCategory): FareRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFareRule(rule: FareRuleEntity)
}
