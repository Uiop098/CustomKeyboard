package com.example.customkeyboard

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLogger.register(this)
    }
}