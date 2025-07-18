package com.jlss.smartDairy.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
private val LightColorScheme = lightColorScheme(
    primary = AppGreen,
    onPrimary = AppWhite,
    background = AppWhite,
    onBackground = AppBlack,
    surface = AppWhite,         // Usually white for TextField background
    onSurface = AppBlack,       // ✅ Black so text is visible inside fields
    secondary = AppGreen,
    onSecondary = AppWhite,
    tertiary = AppGreen,
    onTertiary = AppWhite,

    error = Color(0xFFB00020),
    onError = Color.White,
    outline = Color.Gray,
    inverseOnSurface = AppWhite,
    inverseSurface = AppBlack,
    surfaceVariant = Color(0xFFE0E0E0)
)

@Composable

fun MukeshDairyTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Force your custom light color scheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true // Force black icons on white
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
