package com.example.customkeyboard.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
    
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()
    
    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()
    
    var isPopupPreviewEnabled: Boolean
        get() = prefs.getBoolean("popup_preview_enabled", true)
        set(value) = prefs.edit().putBoolean("popup_preview_enabled", value).apply()
    
    var vibrationDurationMs: Long
        get() = prefs.getLong("vibration_duration", 30L)
        set(value) = prefs.edit().putLong("vibration_duration", value).apply()
}