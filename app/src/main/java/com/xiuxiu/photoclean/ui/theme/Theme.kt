package com.xiuxiu.photoclean.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BabyBlue,
    onPrimary = CardSurfaceWhite,
    primaryContainer = BabyBlueLight,
    onPrimaryContainer = TextPrimary,
    
    secondary = PastelPink,
    onSecondary = CardSurfaceWhite,
    secondaryContainer = PastelPinkLight,
    onSecondaryContainer = TextPrimary,
    
    background = BackgroundCream,
    onBackground = TextPrimary,
    surface = CardSurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary
)

@Composable
fun XiuXiuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundCream.toArgb()
            window.navigationBarColor = BackgroundCream.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
