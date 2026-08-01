package com.vttcabs.admin

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class VttAdminApp : Application() {
    
    lateinit var firebaseAuth: FirebaseAuth
        private set
    
    lateinit var firestore: FirebaseFirestore
        private set
    
    lateinit var firebaseMessaging: FirebaseMessaging
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseMessaging = FirebaseMessaging.getInstance()
    }
    
    companion object {
        lateinit var instance: VttAdminApp
            private set
    }
}
