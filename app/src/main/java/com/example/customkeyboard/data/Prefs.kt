package com.example.customkeyboard.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

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

    var customSoundUri: String
        get() = prefs.getString("custom_sound_uri", "") ?: ""
        set(value) = prefs.edit().putString("custom_sound_uri", value).apply()

    var selectedSoundType: String
        get() = prefs.getString("selected_sound_type", "CLICK_MECHANICAL") ?: "CLICK_MECHANICAL"
        set(value) = prefs.edit().putString("selected_sound_type", value).apply()

    var isGestureTypingEnabled: Boolean
        get() = prefs.getBoolean("gesture_typing_enabled", true)
        set(value) = prefs.edit().putBoolean("gesture_typing_enabled", value).apply()

    var isGestureDeleteEnabled: Boolean
        get() = prefs.getBoolean("gesture_delete_enabled", true)
        set(value) = prefs.edit().putBoolean("gesture_delete_enabled", value).apply()

    var isGestureCursorEnabled: Boolean
        get() = prefs.getBoolean("gesture_cursor_enabled", true)
        set(value) = prefs.edit().putBoolean("gesture_cursor_enabled", value).apply()

    var isGestureCapitalizeEnabled: Boolean
        get() = prefs.getBoolean("gesture_capitalize_enabled", true)
        set(value) = prefs.edit().putBoolean("gesture_capitalize_enabled", value).apply()

    var isLongPressSymbolsEnabled: Boolean
        get() = prefs.getBoolean("long_press_symbols_enabled", true)
        set(value) = prefs.edit().putBoolean("long_press_symbols_enabled", value).apply()

    var longPressDelayMs: Long
        get() = prefs.getLong("long_press_delay_ms", 400L)
        set(value) = prefs.edit().putLong("long_press_delay_ms", value).apply()

    var keyboardLayout: String
        get() = prefs.getString("keyboard_layout", "QWERTY") ?: "QWERTY"
        set(value) = prefs.edit().putString("keyboard_layout", value).apply()

    var isOneHandedModeEnabled: Boolean
        get() = prefs.getBoolean("one_handed_mode_enabled", false)
        set(value) = prefs.edit().putBoolean("one_handed_mode_enabled", value).apply()

    var oneHandedSide: String
        get() = prefs.getString("one_handed_side", "LEFT") ?: "LEFT"
        set(value) = prefs.edit().putString("one_handed_side", value).apply()

    var isSplitKeyboardEnabled: Boolean
        get() = prefs.getBoolean("split_keyboard_enabled", false)
        set(value) = prefs.edit().putBoolean("split_keyboard_enabled", value).apply()

    var keyboardHeightPercent: Int
        get() = prefs.getInt("keyboard_height_percent", 100)
        set(value) = prefs.edit().putInt("keyboard_height_percent", value.coerceIn(70, 150)).apply()

    var currentThemeId: String
        get() = prefs.getString("current_theme_id", "MATERIAL_YOU") ?: "MATERIAL_YOU"
        set(value) = prefs.edit().putString("current_theme_id", value).apply()

    var currentLanguage: String
        get() = prefs.getString("current_language", "en") ?: "en"
        set(value) = prefs.edit().putString("current_language", value).apply()

    var showSuggestionBar: Boolean
        get() = prefs.getBoolean("show_suggestion_bar", true)
        set(value) = prefs.edit().putBoolean("show_suggestion_bar", value).apply()
}
