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

val VttPrimary = Color(0xFF0066CC)
val VttPrimaryDark = Color(0xFF004999)
val VttSecondary = Color(0xFFFF9500)
val VttBackground = Color(0xFFF5F5F5)
val VttSurface = Color(0xFFFFFFFF)
val VttError = Color(0xFFD32F2F)
val VttSuccess = Color(0xFF4CAF50)

private val LightColorScheme = lightColorScheme(
    primary = VttPrimary,
    secondary = VttSecondary,
    background = VttBackground,
    surface = VttSurface,
    error = VttError
)

@Composable
fun VttAdminTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
