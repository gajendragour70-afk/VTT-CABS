package com.vttcabs.admin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VttAdminApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important alerts and notifications"
            }

            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_ALERTS = "alerts_channel"
    }
}
