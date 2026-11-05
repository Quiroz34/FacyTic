package com.tiketefactura.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD8EFEB),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF073F3C),
    secondary = Secondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = Success,
    background = Background,
    surface = Surface,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE0EFEC),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF164A46),
    surfaceContainerLowest = androidx.compose.ui.graphics.Color.White,
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFFFBFCFA),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFFF0F4F2),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFFE9EFEC),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    outline = androidx.compose.ui.graphics.Color(0xFF74817E),
    outlineVariant = Border,
    error = Error
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun TiketeFacturaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION") // Se conserva para Android anteriores a la política edge-to-edge obligatoria.
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
