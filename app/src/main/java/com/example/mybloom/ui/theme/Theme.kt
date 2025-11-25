package com.example.mybloom.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Color Scheme avec vos couleurs
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,              // Vert principal (boutons, accent)
    onPrimary = SurfaceWhite,            // Texte sur le vert principal
    primaryContainer = PrimaryGreenLight, // Backgrounds clairs
    onPrimaryContainer = PrimaryGreenDark, // Texte sur background clair

    secondary = AccentGreen,             // Accent lumineux (loader, FAB)
    onSecondary = SurfaceWhite,          // Texte sur accent
    secondaryContainer = PrimaryGreenLight,
    onSecondaryContainer = PrimaryGreen,

    tertiary = PrimaryGreenDark,         // Vert foncé pour variation
    onTertiary = SurfaceWhite,

    error = ErrorRed,                    // Rouge pour erreurs/delete
    onError = SurfaceWhite,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed,

    background = BackgroundLight,        // Fond général
    onBackground = TextPrimary,          // Texte sur fond

    surface = SurfaceWhite,              // Cards, AppBar
    onSurface = TextPrimary,             // Texte sur surface
    surfaceVariant = OutlineLight,       // Variante pour séparateurs
    onSurfaceVariant = TextSecondary,    // Texte secondaire

    outline = NeutralGray,               // Bordures d'inputs
    outlineVariant = OutlineLight,       // Séparateurs légers

    scrim = TextPrimary.copy(alpha = 0.3f) // Overlay pour dialogs
)

// Dark Color Scheme avec vos couleurs adaptées
private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,               // Plus lumineux en dark mode
    onPrimary = TextPrimary,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = PrimaryGreenLight,

    secondary = PrimaryGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = PrimaryGreenDark,
    onSecondaryContainer = AccentGreen,

    tertiary = PrimaryGreenLight,
    onTertiary = TextPrimary,

    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed,

    background = TextPrimary,            // Fond sombre
    onBackground = SurfaceWhite,

    surface = TextPrimary.copy(alpha = 0.95f),
    onSurface = SurfaceWhite,
    surfaceVariant = TextSecondary,
    onSurfaceVariant = TextHint,

    outline = NeutralGray,
    outlineVariant = TextSecondary,

    scrim = TextPrimary.copy(alpha = 0.5f)
)

@Composable
fun PlantdiscoveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disponible sur Android 12+
    dynamicColor: Boolean = false,  // ✅ Désactivé pour utiliser vos couleurs custom
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
