package com.example.mybloom.config

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.core.content.edit

object ThemeManager {
    private const val PREFS_NAME = "app_preferences"
    private const val THEME_KEY = "theme_mode"

    // Theme modes
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setTheme(context: Context, themeMode: String) {
        getPreferences(context).edit { putString(THEME_KEY, themeMode) }
    }

    fun getTheme(context: Context): String {
        return getPreferences(context).getString(THEME_KEY, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun getAvailableThemes(): List<ThemeOption> {
        return listOf(
            ThemeOption(THEME_LIGHT, "Light", Icons.Default.LightMode),
            ThemeOption(THEME_DARK, "Dark", Icons.Default.DarkMode),
            ThemeOption(THEME_SYSTEM, "System", Icons.Default.SettingsBrightness)
        )
    }

    data class ThemeOption(val code: String, val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
}
