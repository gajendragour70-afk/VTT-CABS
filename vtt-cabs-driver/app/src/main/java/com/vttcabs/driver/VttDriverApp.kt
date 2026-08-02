package com.vttcabs.driver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VttDriverApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Booking Channel
            val bookingChannel = NotificationChannel(
                CHANNEL_BOOKING,
                "Booking Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new booking requests"
                enableVibration(true)
            }

            // Location Channel
            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background location tracking"
            }

            // SOS Channel
            val sosChannel = NotificationChannel(
                CHANNEL_SOS,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency SOS alerts"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(bookingChannel, locationChannel, sosChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_BOOKING = "booking_channel"
        const val CHANNEL_LOCATION = "location_channel"
        const val CHANNEL_SOS = "sos_channel"
    }
}
