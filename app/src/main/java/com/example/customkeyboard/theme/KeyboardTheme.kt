package com.example.customkeyboard.theme

import android.graphics.Color

enum class ThemeCategory(val title: String) {
    DYNAMIC("Dynamic & Adaptive"),
    GLASS("Frosted Glass"),
    DARK("Dark & AMOLED"),
    LIGHT("Clean & Light"),
    VIBRANT("Vibrant & Neon"),
    CUSTOM("Custom Wallpaper & Colors")
}

data class KeyboardTheme(
    val id: String,
    val name: String,
    val category: ThemeCategory,
    val description: String,
    val keyboardBgColor: Int,
    val topStripBgColor: Int,
    val keyBgColor: Int,
    val keyPressedBgColor: Int,
    val keyActionBgColor: Int,
    val keyActionPressedBgColor: Int,
    val keyTextColor: Int,
    val keySecondaryTextColor: Int,
    val accentColor: Int,
    val keyBorderColor: Int = Color.TRANSPARENT,
    val keyBorderWidthDp: Float = 0f,
    val isGlass: Boolean = false,
    val isDark: Boolean = true,
    val opacity: Float = 1.0f
)
