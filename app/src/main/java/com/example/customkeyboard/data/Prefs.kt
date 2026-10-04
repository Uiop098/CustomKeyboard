package com.example.customkeyboard.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    // Sound & Audio
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var selectedSoundType: String
        get() = prefs.getString("selected_sound_type", "CLICK_MECHANICAL") ?: "CLICK_MECHANICAL"
        set(value) = prefs.edit().putString("selected_sound_type", value).apply()

    var soundVolume: Int
        get() = prefs.getInt("sound_volume", 85)
        set(value) = prefs.edit().putInt("sound_volume", value.coerceIn(0, 100)).apply()

    var customSoundResourceId: Int
        get() = prefs.getInt("custom_sound_resource", 0)
        set(value) = prefs.edit().putInt("custom_sound_resource", value).apply()

    var customSoundUri: String
        get() = prefs.getString("custom_sound_uri", "") ?: ""
        set(value) = prefs.edit().putString("custom_sound_uri", value).apply()

    // Haptics & Vibration
    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    var vibrationDurationMs: Long
        get() = prefs.getLong("vibration_duration", 25L)
        set(value) = prefs.edit().putLong("vibration_duration", value.coerceIn(5L, 100L)).apply()

    // Theme & Appearance
    var currentThemeId: String
        get() = prefs.getString("current_theme_id", "MATERIAL_YOU") ?: "MATERIAL_YOU"
        set(value) = prefs.edit().putString("current_theme_id", value).apply()

    var isGlassModeEnabled: Boolean
        get() = prefs.getBoolean("glass_mode_enabled", false)
        set(value) = prefs.edit().putBoolean("glass_mode_enabled", value).apply()

    var customBgImageUri: String
        get() = prefs.getString("custom_bg_image_uri", "") ?: ""
        set(value) = prefs.edit().putString("custom_bg_image_uri", value).apply()

    var customBgColor: Int
        get() = prefs.getInt("custom_bg_color", 0xFF1E293B.toInt())
        set(value) = prefs.edit().putInt("custom_bg_color", value).apply()

    var customKeyColor: Int
        get() = prefs.getInt("custom_key_color", 0xFF334155.toInt())
        set(value) = prefs.edit().putInt("custom_key_color", value).apply()

    var customTextColor: Int
        get() = prefs.getInt("custom_text_color", 0xFFF8FAFC.toInt())
        set(value) = prefs.edit().putInt("custom_text_color", value).apply()

    var accentColor: Int
        get() = prefs.getInt("accent_color", 0xFF3B82F6.toInt())
        set(value) = prefs.edit().putInt("accent_color", value).apply()

    // Layout Dimensions & Spacing
    var keyboardHeightPercent: Int
        get() = prefs.getInt("keyboard_height_percent", 100)
        set(value) = prefs.edit().putInt("keyboard_height_percent", value.coerceIn(70, 150)).apply()

    var keySpacingDp: Int
        get() = prefs.getInt("key_spacing_dp", 3)
        set(value) = prefs.edit().putInt("key_spacing_dp", value.coerceIn(1, 10)).apply()

    var keyCornerRadiusDp: Int
        get() = prefs.getInt("key_corner_radius_dp", 6)
        set(value) = prefs.edit().putInt("key_corner_radius_dp", value.coerceIn(2, 20)).apply()

    var keyboardOpacityPercent: Int
        get() = prefs.getInt("keyboard_opacity_percent", 90)
        set(value) = prefs.edit().putInt("keyboard_opacity_percent", value.coerceIn(20, 100)).apply()

    // Keyboard Features & Rows
    var isNumberRowEnabled: Boolean
        get() = prefs.getBoolean("number_row_enabled", false)
        set(value) = prefs.edit().putBoolean("number_row_enabled", value).apply()

    var keyboardLayout: String
        get() = prefs.getString("keyboard_layout", "QWERTY") ?: "QWERTY"
        set(value) = prefs.edit().putString("keyboard_layout", value).apply()

    var isPopupPreviewEnabled: Boolean
        get() = prefs.getBoolean("popup_preview_enabled", false)
        set(value) = prefs.edit().putBoolean("popup_preview_enabled", value).apply()

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

    var isOneHandedModeEnabled: Boolean
        get() = prefs.getBoolean("one_handed_mode_enabled", false)
        set(value) = prefs.edit().putBoolean("one_handed_mode_enabled", value).apply()

    var oneHandedSide: String
        get() = prefs.getString("one_handed_side", "LEFT") ?: "LEFT"
        set(value) = prefs.edit().putString("one_handed_side", value).apply()

    var isSplitKeyboardEnabled: Boolean
        get() = prefs.getBoolean("split_keyboard_enabled", false)
        set(value) = prefs.edit().putBoolean("split_keyboard_enabled", value).apply()

    var currentLanguage: String
        get() = prefs.getString("current_language", "en") ?: "en"
        set(value) = prefs.edit().putString("current_language", value).apply()

    var showSuggestionBar: Boolean
        get() = prefs.getBoolean("show_suggestion_bar", true)
        set(value) = prefs.edit().putBoolean("show_suggestion_bar", value).apply()
}
