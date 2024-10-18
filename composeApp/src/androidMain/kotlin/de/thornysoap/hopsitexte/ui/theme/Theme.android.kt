package de.thornysoap.hopsitexte.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    content: @Composable() () -> Unit,
) {
    val activity = LocalContext.current as? ComponentActivity

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DefaultDarkScheme
        else -> DefaultLightScheme
    }

    LaunchedEffect(darkTheme) {
        activity?.run {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.Transparent.toArgb(),
                    darkScrim = Color.Transparent.toArgb(),
                    detectDarkMode = { darkTheme },
                ),
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.Transparent.toArgb(),
                    darkScrim = Color.Transparent.toArgb(),
                    detectDarkMode = { darkTheme },
                ),
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                window.isNavigationBarContrastEnforced = false
            window.navigationBarColor = colorScheme.background.copy(alpha = 0.3f).toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
