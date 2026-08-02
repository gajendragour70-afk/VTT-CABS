package com.vttcabs.customer

import android.app.Application
import com.vttcabs.common.SupabaseService

class VttCustomerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseService.initialize(this)
    }
}
