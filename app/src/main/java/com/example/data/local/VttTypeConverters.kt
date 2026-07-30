package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.BookingStatus
import com.example.data.model.BookingType
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory

class VttTypeConverters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try { UserRole.valueOf(value) } catch (e: Exception) { UserRole.CUSTOMER }

    @TypeConverter
    fun fromDriverApprovalStatus(status: DriverApprovalStatus): String = status.name

    @TypeConverter
    fun toDriverApprovalStatus(value: String): DriverApprovalStatus = try { DriverApprovalStatus.valueOf(value) } catch (e: Exception) { DriverApprovalStatus.PENDING }

    @TypeConverter
    fun fromBookingType(type: BookingType): String = type.name

    @TypeConverter
    fun toBookingType(value: String): BookingType = try { BookingType.valueOf(value) } catch (e: Exception) { BookingType.ONE_WAY }

    @TypeConverter
    fun fromVehicleCategory(cat: VehicleCategory): String = cat.name

    @TypeConverter
    fun toVehicleCategory(value: String): VehicleCategory = try { VehicleCategory.valueOf(value) } catch (e: Exception) { VehicleCategory.SEDAN }

    @TypeConverter
    fun fromBookingStatus(status: BookingStatus): String = status.name

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus = try { BookingStatus.valueOf(value) } catch (e: Exception) { BookingStatus.SEARCHING }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try { PaymentMethod.valueOf(value) } catch (e: Exception) { PaymentMethod.UPI }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = try { PaymentStatus.valueOf(value) } catch (e: Exception) { PaymentStatus.PENDING }
}
