package com.example.customkeyboard.UI

/** Owns the visual layer: themes, key styling, and layout surfaces. */
interface ThemeManager {

    /** Applies [theme] to the active keyboard surface. */
    fun applyTheme(theme: Theme)

    /** Returns the list of available themes. */
    fun availableThemes(): List<Theme>

    /** Applies color customizations to the current theme. */
    fun applyColors(accent: Int, background: Int, text: Int)

    /** Applies the built-in dark mode toggle. */
    fun setDarkMode(enabled: Boolean)

    /** Returns the current theme. */
    fun currentTheme(): Theme

    data class Theme(val id: String, val name: String, val isDark: Boolean)
}