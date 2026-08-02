package com.vttcabs.admin.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Primary = Color(0xFFDC2626)
val PrimaryDark = Color(0xFFB91C1C)
val PrimaryLight = Color(0xFFFEE2E2)
val Secondary = Color(0xFF16A34A)
val Background = Color(0xFFF9FAFB)
val Surface = Color(0xFFFFFFFF)
val Error = Color(0xFFDC2626)
val OnPrimary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF111827)
val OnSurface = Color(0xFF111827)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    secondary = Secondary,
    background = Color(0xFF111827),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF9FAFB),
    error = Error,
    onError = Color.White,
)

@Composable
fun VttAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
