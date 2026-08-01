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

// VTT Admin Colors
val VTTBluePrimary = Color(0xFF1976D2)
val VTTBlueDark = Color(0xFF0D47A1)
val VTTBlueLight = Color(0xFF42A5F5)
val VTTBlueContainer = Color(0xFFE3F2FD)
val VTTSuccess = Color(0xFF4CAF50)
val VTTWarning = Color(0xFFFF9800)
val VTTDanger = Color(0xFFF44336)
val VTTInfo = Color(0xFF2196F3)

private val LightColorScheme = lightColorScheme(
    primary = VTTBluePrimary,
    onPrimary = Color.White,
    primaryContainer = VTTBlueContainer,
    onPrimaryContainer = VTTBlueDark,
    secondary = VTTBlueLight,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    error = VTTDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = VTTBlueLight,
    onPrimary = VTTBlueDark,
    primaryContainer = VTTBlueDark,
    onPrimaryContainer = VTTBlueContainer,
    secondary = VTTBluePrimary,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    error = VTTDanger,
    onError = Color.White
)

@Composable
fun VTTAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VTTBlueDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
