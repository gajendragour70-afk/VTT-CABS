package com.example.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class VttFirebaseMessagingService : FirebaseMessagingService() {

    @Deprecated("Deprecated in Java")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        // Token can be sent to backend or stored locally
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Title: ${it.title}, Body: ${it.body}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message Payload Data: ${remoteMessage.data}")
        }
    }

    companion object {
        private const val TAG = "VttFCMService"
    }
}
