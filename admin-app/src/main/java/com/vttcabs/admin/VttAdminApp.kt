package com.vttcabs.admin

import android.app.Application
import com.vttcabs.common.SupabaseService

class VttAdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseService.initialize(this)
    }
}
