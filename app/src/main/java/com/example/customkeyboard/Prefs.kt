package com.example.customkeyboard

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val sp: SharedPreferences = context.getSharedPreferences("custom_keyboard_prefs", Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean
        get() = sp.getBoolean("sound_enabled", true)
        set(value) = sp.edit().putBoolean("sound_enabled", value).apply()

    var isVibrationEnabled: Boolean
        get() = sp.getBoolean("vibration_enabled", true)
        set(value) = sp.edit().putBoolean("vibration_enabled", value).apply()

    var vibrationDurationMs: Long
        get() = sp.getLong("vibration_duration", 25L)
        set(value) = sp.edit().putLong("vibration_duration", value).apply()

    var isPopupPreviewEnabled: Boolean
        get() = sp.getBoolean("popup_preview_enabled", true)
        set(value) = sp.edit().putBoolean("popup_preview_enabled", value).apply()

    var currentTheme: String
        get() = sp.getString("selected_theme", "material_dark") ?: "material_dark"
        set(value) = sp.edit().putString("selected_theme", value).apply()
}
