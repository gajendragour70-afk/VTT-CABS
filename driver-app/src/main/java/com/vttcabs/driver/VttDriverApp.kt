package com.vttcabs.driver

import android.app.Application
import com.vttcabs.common.SupabaseService

class VttDriverApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseService.initialize(this)
    }
}
