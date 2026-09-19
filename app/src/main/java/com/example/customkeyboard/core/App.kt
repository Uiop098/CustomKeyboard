package com.example.customkeyboard.core

import android.app.Application
import com.example.customkeyboard.util.CrashLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLogger.register(this)
    }
}