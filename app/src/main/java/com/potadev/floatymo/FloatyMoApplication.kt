package com.potadev.floatymo

import android.app.Application

class FloatyMoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
