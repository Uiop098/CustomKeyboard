package com.example.customkeyboard.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Build
import com.example.customkeyboard.data.Prefs
import java.io.File
import java.io.FileOutputStream

class ThemeManager(private val context: Context) {

    private val prefs = Prefs(context)

    companion object {
        // Built-in theme presets
        val MATERIAL_YOU = KeyboardTheme(
            id = "MATERIAL_YOU",
            name = "Material You",
            category = ThemeCategory.DYNAMIC,
            description = "Modern adaptive dynamic Material palette",
            keyboardBgColor = 0xFF182232.toInt(),
            topStripBgColor = 0xFF121B28.toInt(),
            keyBgColor = 0xFF2A374A.toInt(),
            keyPressedBgColor = 0xFF3D4F68.toInt(),
            keyActionBgColor = 0xFF3B82F6.toInt(),
            keyActionPressedBgColor = 0xFF2563EB.toInt(),
            keyTextColor = 0xFFF8FAFC.toInt(),
            keySecondaryTextColor = 0xFF94A3B8.toInt(),
            accentColor = 0xFF3B82F6.toInt(),
            keyBorderColor = 0x22FFFFFF,
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val GLASS_DARK = KeyboardTheme(
            id = "GLASS_DARK",
            name = "Frosted Glass Dark",
            category = ThemeCategory.GLASS,
            description = "Translucent frosted dark glass with glowing crystal border",
            keyboardBgColor = 0xCC0F172A.toInt(),
            topStripBgColor = 0xDD0A0E1A.toInt(),
            keyBgColor = 0x2AFFFFFF.toInt(),
            keyPressedBgColor = 0x55FFFFFF.toInt(),
            keyActionBgColor = 0x448B5CF6.toInt(),
            keyActionPressedBgColor = 0x778B5CF6.toInt(),
            keyTextColor = 0xFFFFFFFF.toInt(),
            keySecondaryTextColor = 0xCCFFFFFF.toInt(),
            accentColor = 0xFF8B5CF6.toInt(),
            keyBorderColor = 0x44FFFFFF.toInt(),
            keyBorderWidthDp = 1f,
            isGlass = true,
            isDark = true,
            opacity = 0.85f
        )

        val GLASS_LIGHT = KeyboardTheme(
            id = "GLASS_LIGHT",
            name = "Frosted Glass Light",
            category = ThemeCategory.GLASS,
            description = "Translucent frosted bright glass with crisp typography",
            keyboardBgColor = 0xBBE2E8F0.toInt(),
            topStripBgColor = 0xCCCBD5E1.toInt(),
            keyBgColor = 0x66FFFFFF.toInt(),
            keyPressedBgColor = 0x99FFFFFF.toInt(),
            keyActionBgColor = 0x443B82F6.toInt(),
            keyActionPressedBgColor = 0x773B82F6.toInt(),
            keyTextColor = 0xFF0F172A.toInt(),
            keySecondaryTextColor = 0xFF475569.toInt(),
            accentColor = 0xFF3B82F6.toInt(),
            keyBorderColor = 0x55FFFFFF.toInt(),
            keyBorderWidthDp = 1f,
            isGlass = true,
            isDark = false,
            opacity = 0.85f
        )

        val AMOLED_BLACK = KeyboardTheme(
            id = "AMOLED_BLACK",
            name = "AMOLED Pitch Black",
            category = ThemeCategory.DARK,
            description = "Pure black #000000 with high-contrast sharp keys & battery saver",
            keyboardBgColor = 0xFF000000.toInt(),
            topStripBgColor = 0xFF000000.toInt(),
            keyBgColor = 0xFF121212.toInt(),
            keyPressedBgColor = 0xFF282828.toInt(),
            keyActionBgColor = 0xFF1F1F1F.toInt(),
            keyActionPressedBgColor = 0xFF333333.toInt(),
            keyTextColor = 0xFFFFFFFF.toInt(),
            keySecondaryTextColor = 0xFFA0A0A0.toInt(),
            accentColor = 0xFF00E5FF.toInt(),
            keyBorderColor = 0xFF262626.toInt(),
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val DARK_SLATE = KeyboardTheme(
            id = "DARK_SLATE",
            name = "Deep Slate",
            category = ThemeCategory.DARK,
            description = "Professional oceanic slate with sapphire highlights",
            keyboardBgColor = 0xFF0F172A.toInt(),
            topStripBgColor = 0xFF0B1120.toInt(),
            keyBgColor = 0xFF1E293B.toInt(),
            keyPressedBgColor = 0xFF334155.toInt(),
            keyActionBgColor = 0xFF2563EB.toInt(),
            keyActionPressedBgColor = 0xFF1D4ED8.toInt(),
            keyTextColor = 0xFFF1F5F9.toInt(),
            keySecondaryTextColor = 0xFF94A3B8.toInt(),
            accentColor = 0xFF38BDF8.toInt(),
            keyBorderColor = 0xFF334155.toInt(),
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val CYBERPUNK = KeyboardTheme(
            id = "CYBERPUNK",
            name = "Cyberpunk Neon",
            category = ThemeCategory.VIBRANT,
            description = "Vibrant electric neon pink and cyan on midnight violet",
            keyboardBgColor = 0xFF0A0017.toInt(),
            topStripBgColor = 0xFF05000C.toInt(),
            keyBgColor = 0xFF1A0A2E.toInt(),
            keyPressedBgColor = 0xFF2E1251.toInt(),
            keyActionBgColor = 0xFFFF007F.toInt(),
            keyActionPressedBgColor = 0xFFD6006B.toInt(),
            keyTextColor = 0xFF00F0FF.toInt(),
            keySecondaryTextColor = 0xFFFF007F.toInt(),
            accentColor = 0xFFFF007F.toInt(),
            keyBorderColor = 0x6600F0FF.toInt(),
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val SUNSET_GLOW = KeyboardTheme(
            id = "SUNSET_GLOW",
            name = "Sunset Glow",
            category = ThemeCategory.VIBRANT,
            description = "Warm radiant sunset hues with deep plum keys",
            keyboardBgColor = 0xFF1A0C1E.toInt(),
            topStripBgColor = 0xFF120815.toInt(),
            keyBgColor = 0xFF2D1533.toInt(),
            keyPressedBgColor = 0xFF46204E.toInt(),
            keyActionBgColor = 0xFFF97316.toInt(),
            keyActionPressedBgColor = 0xFFEA580C.toInt(),
            keyTextColor = 0xFFFFF1F2.toInt(),
            keySecondaryTextColor = 0xFFFDBA74.toInt(),
            accentColor = 0xFFF97316.toInt(),
            keyBorderColor = 0x44F97316.toInt(),
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val EMERALD_FOREST = KeyboardTheme(
            id = "EMERALD_FOREST",
            name = "Emerald Matrix",
            category = ThemeCategory.VIBRANT,
            description = "Lush deep forest green with vibrant emerald accents",
            keyboardBgColor = 0xFF061A14.toInt(),
            topStripBgColor = 0xFF030E0B.toInt(),
            keyBgColor = 0xFF0D2D23.toInt(),
            keyPressedBgColor = 0xFF144234.toInt(),
            keyActionBgColor = 0xFF10B981.toInt(),
            keyActionPressedBgColor = 0xFF059669.toInt(),
            keyTextColor = 0xFFECFDF5.toInt(),
            keySecondaryTextColor = 0xFF6EE7B7.toInt(),
            accentColor = 0xFF10B981.toInt(),
            keyBorderColor = 0x4410B981.toInt(),
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val ROYAL_PURPLE = KeyboardTheme(
            id = "ROYAL_PURPLE",
            name = "Royal Velvet",
            category = ThemeCategory.VIBRANT,
            description = "Luxurious royal amethyst and purple neon glow",
            keyboardBgColor = 0xFF130924.toInt(),
            topStripBgColor = 0xFF0D061A.toInt(),
            keyBgColor = 0xFF241242.toInt(),
            keyPressedBgColor = 0xFF3A1C6B.toInt(),
            keyActionBgColor = 0xFFA855F7.toInt(),
            keyActionPressedBgColor = 0xFF9333EA.toInt(),
            keyTextColor = 0xFFFAF5FF.toInt(),
            keySecondaryTextColor = 0xFFD8B4FE.toInt(),
            accentColor = 0xFFA855F7.toInt(),
            keyBorderColor = 0x44A855F7.toInt(),
            keyBorderWidthDp = 1f,
            isDark = true
        )

        val LIGHT_MINIMAL = KeyboardTheme(
            id = "LIGHT_MINIMAL",
            name = "Clean Minimal Light",
            category = ThemeCategory.LIGHT,
            description = "Crisp white minimalist aesthetic with slate keys",
            keyboardBgColor = 0xFFF1F5F9.toInt(),
            topStripBgColor = 0xFFE2E8F0.toInt(),
            keyBgColor = 0xFFFFFFFF.toInt(),
            keyPressedBgColor = 0xFFCBD5E1.toInt(),
            keyActionBgColor = 0xFF3B82F6.toInt(),
            keyActionPressedBgColor = 0xFF2563EB.toInt(),
            keyTextColor = 0xFF0F172A.toInt(),
            keySecondaryTextColor = 0xFF64748B.toInt(),
            accentColor = 0xFF3B82F6.toInt(),
            keyBorderColor = 0xFFE2E8F0.toInt(),
            keyBorderWidthDp = 1f,
            isDark = false
        )

        val ALL_THEMES = listOf(
            MATERIAL_YOU,
            GLASS_DARK,
            GLASS_LIGHT,
            AMOLED_BLACK,
            DARK_SLATE,
            CYBERPUNK,
            SUNSET_GLOW,
            EMERALD_FOREST,
            ROYAL_PURPLE,
            LIGHT_MINIMAL
        )
    }

    /**
     * Returns the active theme based on user preferences.
     */
    fun getCurrentTheme(): KeyboardTheme {
        val themeId = prefs.currentThemeId
        if (themeId == "CUSTOM") {
            return getCustomTheme()
        }
        return ALL_THEMES.firstOrNull { it.id == themeId } ?: MATERIAL_YOU
    }

    /**
     * Builds a custom theme based on user selected custom colors and wallpaper.
     */
    fun getCustomTheme(): KeyboardTheme {
        val hasCustomImage = hasCustomBackgroundImage()
        val bgCol = prefs.customBgColor
        val keyCol = prefs.customKeyColor
        val textCol = prefs.customTextColor
        val accentCol = prefs.accentColor

        return KeyboardTheme(
            id = "CUSTOM",
            name = "Custom Wallpaper / Theme",
            category = ThemeCategory.CUSTOM,
            description = if (hasCustomImage) "Custom background image with personalized colors" else "User defined custom palette",
            keyboardBgColor = if (hasCustomImage) 0xCC111827.toInt() else bgCol,
            topStripBgColor = if (hasCustomImage) 0xDD0A0F1D.toInt() else darkenColor(bgCol, 0.8f),
            keyBgColor = keyCol,
            keyPressedBgColor = lightenColor(keyCol, 0.2f),
            keyActionBgColor = accentCol,
            keyActionPressedBgColor = darkenColor(accentCol, 0.85f),
            keyTextColor = textCol,
            keySecondaryTextColor = fadeColor(textCol, 0.7f),
            accentColor = accentCol,
            keyBorderColor = 0x44FFFFFF.toInt(),
            keyBorderWidthDp = 1f,
            isGlass = hasCustomImage || prefs.isGlassModeEnabled,
            isDark = isColorDark(bgCol)
        )
    }

    /**
     * Creates a state-aware Drawable for keyboard keys with custom theme, corner radius, and borders.
     */
    fun createKeyDrawable(
        theme: KeyboardTheme,
        cornerRadiusDp: Float = prefs.keyCornerRadiusDp.toFloat(),
        isActionKey: Boolean = false
    ): StateListDrawable {
        val states = StateListDrawable()
        val density = context.resources.displayMetrics.density
        val radiusPx = cornerRadiusDp * density

        // 1. Pressed State
        val pressedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(if (isActionKey) theme.keyActionPressedBgColor else theme.keyPressedBgColor)
            if (theme.keyBorderWidthDp > 0) {
                setStroke((theme.keyBorderWidthDp * density).toInt(), theme.keyBorderColor)
            }
        }

        // 2. Normal State
        val normalDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(if (isActionKey) theme.keyActionBgColor else theme.keyBgColor)
            if (theme.keyBorderWidthDp > 0) {
                setStroke((theme.keyBorderWidthDp * density).toInt(), theme.keyBorderColor)
            }
        }

        states.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        states.addState(intArrayOf(), normalDrawable)
        return states
    }

    /**
     * Checks if a custom wallpaper background image exists.
     */
    fun hasCustomBackgroundImage(): Boolean {
        val file = File(context.filesDir, "custom_keyboard_bg.jpg")
        return file.exists() && file.length() > 0
    }

    /**
     * Loads the custom wallpaper bitmap.
     */
    fun loadCustomBackgroundBitmap(): Bitmap? {
        val file = File(context.filesDir, "custom_keyboard_bg.jpg")
        if (!file.exists() || file.length() == 0L) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a chosen background image to internal storage.
     */
    fun saveCustomBackgroundImage(uri: Uri): Boolean {
        return try {
            val file = File(context.filesDir, "custom_keyboard_bg.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) {
                prefs.customBgImageUri = uri.toString()
                prefs.currentThemeId = "CUSTOM"
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Removes custom background image.
     */
    fun removeCustomBackgroundImage() {
        val file = File(context.filesDir, "custom_keyboard_bg.jpg")
        if (file.exists()) {
            file.delete()
        }
        prefs.customBgImageUri = ""
        if (prefs.currentThemeId == "CUSTOM") {
            prefs.currentThemeId = "MATERIAL_YOU"
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    private fun lightenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) + (255 - Color.red(color)) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) + (255 - Color.green(color)) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) + (255 - Color.blue(color)) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    private fun fadeColor(color: Int, factor: Float): Int {
        val a = (Color.alpha(color) * factor).toInt().coerceIn(0, 255)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)
    }
}
