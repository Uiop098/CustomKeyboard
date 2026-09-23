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
    
    var soundVolume: Int
        get() = prefs.getInt("sound_volume", 80)
        set(value) = prefs.edit().putInt("sound_volume", value.coerceIn(0, 100)).apply()
    
    var customSoundResourceId: Int
        get() = prefs.getInt("custom_sound_resource", 0)
        set(value) = prefs.edit().putInt("custom_sound_resource", value).apply()
    
    var selectedSoundType: String
        get() = prefs.getString("selected_sound_type", "CLICK_MECHANICAL") ?: "CLICK_MECHANICAL"
        set(value) = prefs.edit().putString("selected_sound_type", value).apply()
}