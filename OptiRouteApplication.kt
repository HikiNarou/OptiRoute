package com.optiroute.com

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OptiRouteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}