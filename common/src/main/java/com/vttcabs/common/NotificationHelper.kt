package com.vttcabs.common

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage

/**
 * Notification Helper for creating professional notifications
 * with custom sounds, full-screen intents, and vibration
 */
object NotificationHelper {
    private const val TAG = "NotificationHelper"
    
    // Notification IDs
    object Ids {
        const val INCOMING_RIDE = 1000
        const val BOOKING_UPDATE = 1001
        const val DRIVER_LOCATION = 1002
        const val SOS_ALERT = 1003
        const val PAYMENT = 1004
        const val GENERAL = 1005
        const val NEW_BOOKING = 2000
        const val DRIVER_REGISTRATION = 2001
        const val DOCUMENT_REVIEW = 2002
    }
    
    /**
     * Initialize notification channels
     */
    fun initialize(context: Context) {
        NotificationSoundManager.initialize(context)
        createChannels(context)
    }
    
    /**
     * Create all notification channels for Android O+
     */
    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                // Customer channels
                createChannel("customer_ride", "Ride Notifications", "Notifications for ride status updates", NotificationManager.IMPORTANCE_HIGH),
                createChannel("customer_driver", "Driver Updates", "Driver location and arrival notifications", NotificationManager.IMPORTANCE_MAX),
                createChannel("customer_trip", "Trip Updates", "Current trip status notifications", NotificationManager.IMPORTANCE_HIGH),
                createChannel("customer_payment", "Payment Notifications", "Payment success and wallet updates", NotificationManager.IMPORTANCE_DEFAULT),
                createChannel("customer_general", "General Notifications", "Other app notifications", NotificationManager.IMPORTANCE_DEFAULT),
                
                // Driver channels
                createChannel("driver_ride", "Ride Requests", "Incoming ride requests", NotificationManager.IMPORTANCE_MAX),
                createChannel("driver_cancelled", "Cancelled Rides", "When a ride is cancelled", NotificationManager.IMPORTANCE_HIGH),
                createChannel("driver_message", "Messages", "Customer messages", NotificationManager.IMPORTANCE_DEFAULT),
                createChannel("driver_start", "Trip Notifications", "Trip start and complete", NotificationManager.IMPORTANCE_HIGH),
                createChannel("driver_payment", "Earnings", "Payment and earnings notifications", NotificationManager.IMPORTANCE_DEFAULT),
                
                // Admin channels
                createChannel("admin_booking", "New Bookings", "New booking alerts", NotificationManager.IMPORTANCE_MAX),
                createChannel("admin_driver", "Driver Registrations", "New driver registration alerts", NotificationManager.IMPORTANCE_HIGH),
                createChannel("admin_document", "Document Reviews", "Document upload notifications", NotificationManager.IMPORTANCE_DEFAULT),
                createChannel("admin_approved", "Approvals", "Driver approval notifications", NotificationManager.IMPORTANCE_DEFAULT),
                createChannel("admin_rejected", "Rejections", "Driver rejection notifications", NotificationManager.IMPORTANCE_HIGH),
                createChannel("admin_ride_cancelled", "Cancelled Rides", "Cancelled booking alerts", NotificationManager.IMPORTANCE_HIGH),
                createChannel("admin_sos", "Emergency SOS", "Emergency SOS alerts", NotificationManager.IMPORTANCE_MAX),
                
                // SOS channel (high priority for all apps)
                createChannel("sos_emergency", "Emergency Alerts", "SOS and emergency notifications", NotificationManager.IMPORTANCE_MAX)
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    private fun createChannel(
        id: String,
        name: String,
        description: String,
        importance: Int
    ): android.app.NotificationChannel {
        return android.app.NotificationChannel(id, name, importance).apply {
            this.description = description
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
    }
    
    // ==================== Customer Notifications ====================
    
    /**
     * Show ride accepted notification
     */
    fun showRideAcceptedNotification(
        context: Context,
        driverName: String,
        vehicleNumber: String,
        eta: String,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.RIDE_ACCEPTED)
        
        val notification = NotificationCompat.Builder(context, "customer_ride")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ride Accepted!")
            .setContentText("$driverName is on the way in $vehicleNumber. ETA: $eta")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "booking", bookingId))
            .addAction(R.drawable.ic_navigation, "Track", createPendingIntent(context, "track", bookingId))
            .build()
        
        showNotification(context, Ids.BOOKING_UPDATE, notification)
    }
    
    /**
     * Show driver arrived notification with full-screen intent
     */
    fun showDriverArrivedNotification(
        context: Context,
        driverName: String,
        phone: String,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.DRIVER_ARRIVED)
        
        val notification = NotificationCompat.Builder(context, "customer_driver")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Driver Arrived! 🎉")
            .setContentText("$driverName has arrived at your pickup location")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(createFullScreenIntent(context, "arrived", bookingId), true)
            .setContentIntent(createPendingIntent(context, "booking", bookingId))
            .addAction(R.drawable.ic_call, "Call Driver", createCallIntent(context, phone))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        showNotification(context, Ids.BOOKING_UPDATE, notification)
    }
    
    /**
     * Show trip started notification
     */
    fun showTripStartedNotification(
        context: Context,
        driverName: String,
        destination: String,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.TRIP_STARTED)
        
        val notification = NotificationCompat.Builder(context, "customer_trip")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Trip Started! 🚗")
            .setContentText("Heading to $destination with $driverName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "tracking", bookingId))
            .addAction(R.drawable.ic_navigation, "Track Live", createPendingIntent(context, "track", bookingId))
            .build()
        
        showNotification(context, Ids.BOOKING_UPDATE, notification)
    }
    
    /**
     * Show trip completed notification
     */
    fun showTripCompletedNotification(
        context: Context,
        fare: String,
        ratingPrompt: Boolean,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.TRIP_COMPLETED)
        
        val builder = NotificationCompat.Builder(context, "customer_trip")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Trip Completed! ✅")
            .setContentText("Total fare: ₹$fare")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "booking_detail", bookingId))
        
        if (ratingPrompt) {
            builder.addAction(R.drawable.ic_star, "Rate Driver", createPendingIntent(context, "rate", bookingId))
        }
        builder.addAction(R.drawable.ic_receipt, "View Receipt", createPendingIntent(context, "invoice", bookingId))
        
        showNotification(context, Ids.BOOKING_UPDATE, notification)
    }
    
    /**
     * Show payment success notification
     */
    fun showPaymentSuccessNotification(
        context: Context,
        amount: String,
        paymentMethod: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.PAYMENT_SUCCESS)
        
        val notification = NotificationCompat.Builder(context, "customer_payment")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Payment Successful! 💰")
            .setContentText("₹$amount paid via $paymentMethod")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_TRANSACTION)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "wallet", ""))
            .build()
        
        showNotification(context, Ids.PAYMENT, notification)
    }
    
    // ==================== Driver Notifications ====================
    
    /**
     * Show incoming ride notification with full-screen alert and loud sound
     */
    fun showIncomingRideNotification(
        context: Context,
        pickupAddress: String,
        dropAddress: String,
        distance: String,
        fare: String,
        eta: String,
        bookingId: String
    ) {
        // Play loud continuous sound
        NotificationSoundManager.playIncomingRideSound(context)
        
        // Wake up screen
        wakeUpScreen(context)
        
        val notification = NotificationCompat.Builder(context, "driver_ride")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🚗 NEW RIDE REQUEST! 🚗")
            .setContentText("Pickup: $pickupAddress")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("📍 Pickup: $pickupAddress\n📍 Drop: $dropAddress\n" +
                         "📏 Distance: $distance\n💰 Fare: ₹$fare\n⏱️ ETA: $eta"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(createFullScreenIntent(context, "incoming_ride", bookingId), true)
            .setContentIntent(createPendingIntent(context, "accept_ride", bookingId))
            .addAction(R.drawable.ic_check, "Accept", createPendingIntent(context, "accept_ride", bookingId))
            .addAction(R.drawable.ic_close, "Reject", createPendingIntent(context, "reject_ride", bookingId))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOnlyAlertOnce(false)
            .build()
        
        showNotification(context, Ids.INCOMING_RIDE, notification)
    }
    
    /**
     * Cancel incoming ride notification
     */
    fun cancelIncomingRideNotification(context: Context) {
        NotificationSoundManager.stopContinuousSound()
        NotificationManagerCompat.from(context).cancel(Ids.INCOMING_RIDE)
    }
    
    /**
     * Show ride cancelled notification
     */
    fun showRideCancelledNotification(
        context: Context,
        reason: String?,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.RIDE_CANCELLED)
        
        val notification = NotificationCompat.Builder(context, "driver_cancelled")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ride Cancelled ❌")
            .setContentText(reason ?: "The ride was cancelled by the customer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "dashboard", ""))
            .build()
        
        showNotification(context, Ids.BOOKING_UPDATE, notification)
    }
    
    /**
     * Show payment received notification
     */
    fun showPaymentReceivedNotification(
        context: Context,
        amount: String,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.PAYMENT_RECEIVED)
        
        val notification = NotificationCompat.Builder(context, "driver_payment")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Payment Received! 💵")
            .setContentText("₹$amount added to your wallet")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_TRANSACTION)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "wallet", ""))
            .build()
        
        showNotification(context, Ids.PAYMENT, notification)
    }
    
    // ==================== Admin Notifications ====================
    
    /**
     * Show new booking alert notification
     */
    fun showNewBookingAlert(
        context: Context,
        pickupAddress: String,
        dropAddress: String,
        customerName: String,
        fare: String,
        bookingId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.NEW_BOOKING_ALERT)
        
        val notification = NotificationCompat.Builder(context, "admin_booking")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🆕 NEW BOOKING! 🆕")
            .setContentText("$customerName booked a ride")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("📍 From: $pickupAddress\n📍 To: $dropAddress\n" +
                         "👤 Customer: $customerName\n💰 Fare: ₹$fare"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "admin_booking_detail", bookingId))
            .addAction(R.drawable.ic_assign, "Assign Driver", createPendingIntent(context, "admin_assign", bookingId))
            .addAction(R.drawable.ic_view, "View", createPendingIntent(context, "admin_booking_detail", bookingId))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        showNotification(context, Ids.NEW_BOOKING, notification)
    }
    
    /**
     * Show new driver registration notification
     */
    fun showNewDriverRegistration(
        context: Context,
        driverName: String,
        phone: String,
        vehicleNumber: String,
        driverId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.NEW_DRIVER_REGISTRATION)
        
        val notification = NotificationCompat.Builder(context, "admin_driver")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🆕 New Driver Registration!")
            .setContentText("$driverName registered as driver")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("👤 Name: $driverName\n📱 Phone: $phone\n" +
                         "🚗 Vehicle: $vehicleNumber"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "admin_driver_detail", driverId))
            .addAction(R.drawable.ic_verify, "Review", createPendingIntent(context, "admin_review_driver", driverId))
            .build()
        
        showNotification(context, Ids.DRIVER_REGISTRATION, notification)
    }
    
    /**
     * Show document uploaded notification
     */
    fun showDocumentUploadedNotification(
        context: Context,
        driverName: String,
        documentType: String,
        driverId: String
    ) {
        NotificationSoundManager.playSound(context, NotificationSoundManager.SoundType.DOCUMENT_UPLOADED)
        
        val notification = NotificationCompat.Builder(context, "admin_document")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📄 Document Uploaded")
            .setContentText("$driverName uploaded $documentType")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntent(context, "admin_review_driver", driverId))
            .build()
        
        showNotification(context, Ids.DOCUMENT_REVIEW, notification)
    }
    
    /**
     * Show SOS emergency notification
     */
    fun showSOSNotification(
        context: Context,
        customerName: String,
        driverName: String?,
        location: String,
        bookingId: String
    ) {
        NotificationSoundManager.playEmergencyAlarm(context)
        
        val notification = NotificationCompat.Builder(context, "admin_sos")
            .setSmallIcon(R.drawable.ic_sos)
            .setContentTitle("🚨🚨 SOS EMERGENCY! 🚨🚨")
            .setContentText("Emergency alert from $customerName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🚨 SOS ALERT!\n\n👤 Customer: $customerName\n" +
                         (if (driverName != null) "🚗 Driver: $driverName\n" else "") +
                         "📍 Location: $location\n" +
                         "⏰ Time: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(createFullScreenIntent(context, "sos", bookingId), true)
            .setContentIntent(createPendingIntent(context, "sos_detail", bookingId))
            .addAction(R.drawable.ic_call, "Call Customer", createPendingIntent(context, "call_customer", bookingId))
            .addAction(R.drawable.ic_location, "View Location", createPendingIntent(context, "track_sos", bookingId))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        showNotification(context, Ids.SOS_ALERT, notification)
    }
    
    // ==================== Helper Methods ====================
    
    private fun showNotification(context: Context, id: Int, notification: android.app.Notification) {
        try {
            // Check if notifications are allowed
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Log.w(TAG, "Notifications are disabled")
                return
            }
            
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission denied: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}")
        }
    }
    
    private fun createPendingIntent(
        context: Context,
        action: String,
        data: String
    ): PendingIntent {
        val intent = Intent(context, getMainActivityClass(context)).apply {
            this.action = action
            putExtra("data", data)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createFullScreenIntent(
        context: Context,
        action: String,
        data: String
    ): PendingIntent {
        val intent = Intent(context, getMainActivityClass(context)).apply {
            this.action = action
            putExtra("data", data)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            action.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createCallIntent(context: Context, phone: String): PendingIntent {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            phone.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun getMainActivityClass(context: Context): Class<*> {
        // Return the appropriate MainActivity based on app
        // In production, this would be set based on the application ID
        return try {
            Class.forName("com.vttcabs.MainActivity")
        } catch (e: Exception) {
            Class.forName("android.app.Activity")
        }
    }
    
    /**
     * Wake up the device screen
     */
    private fun wakeUpScreen(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "VTT:VTTWakeLock"
            )
            wakeLock.acquire(60000) // 60 seconds max
        } catch (e: Exception) {
            Log.e(TAG, "Error waking up screen: ${e.message}")
        }
    }
    
    /**
     * Handle FCM message
     */
    fun handleFCMMessage(context: Context, message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return
        
        when (type) {
            "ride_accepted" -> {
                showRideAcceptedNotification(
                    context,
                    data["driver_name"] ?: "",
                    data["vehicle_number"] ?: "",
                    data["eta"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
            "driver_arrived" -> {
                showDriverArrivedNotification(
                    context,
                    data["driver_name"] ?: "",
                    data["phone"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
            "trip_started" -> {
                showTripStartedNotification(
                    context,
                    data["driver_name"] ?: "",
                    data["destination"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
            "trip_completed" -> {
                showTripCompletedNotification(
                    context,
                    data["fare"] ?: "",
                    data["show_rating"]?.toBoolean() ?: true,
                    data["booking_id"] ?: ""
                )
            }
            "payment_success" -> {
                showPaymentSuccessNotification(
                    context,
                    data["amount"] ?: "",
                    data["payment_method"] ?: "Cash"
                )
            }
            "incoming_ride" -> {
                showIncomingRideNotification(
                    context,
                    data["pickup_address"] ?: "",
                    data["drop_address"] ?: "",
                    data["distance"] ?: "",
                    data["fare"] ?: "",
                    data["eta"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
            "ride_cancelled" -> {
                cancelIncomingRideNotification(context)
                showRideCancelledNotification(
                    context,
                    data["reason"],
                    data["booking_id"] ?: ""
                )
            }
            "payment_received" -> {
                showPaymentReceivedNotification(
                    context,
                    data["amount"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
            "new_booking" -> {
                showNewBookingAlert(
                    context,
                    data["pickup_address"] ?: "",
                    data["drop_address"] ?: "",
                    data["customer_name"] ?: "",
                    data["fare"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
            "new_driver_registration" -> {
                showNewDriverRegistration(
                    context,
                    data["driver_name"] ?: "",
                    data["phone"] ?: "",
                    data["vehicle_number"] ?: "",
                    data["driver_id"] ?: ""
                )
            }
            "document_uploaded" -> {
                showDocumentUploadedNotification(
                    context,
                    data["driver_name"] ?: "",
                    data["document_type"] ?: "",
                    data["driver_id"] ?: ""
                )
            }
            "sos_alert" -> {
                showSOSNotification(
                    context,
                    data["customer_name"] ?: "",
                    data["driver_name"],
                    data["location"] ?: "",
                    data["booking_id"] ?: ""
                )
            }
        }
    }
}
