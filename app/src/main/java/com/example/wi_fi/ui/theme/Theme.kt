package com.example.wi_fi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview

private val DarkColorPalette = darkColors(
    primary = Color(0xFF6A1B9A), // Purple800
    secondary = Color(0xFF635D6C) // PurpleGrey800
    /* tertiary = Color(0xFFAD1457) // Pink800 */
)

private val LightColorPalette = lightColors(
    primary = Color(0xFFCE93D8), // Purple200
    secondary = Color(0xFFB39DDB) // PurpleGrey200
    /* tertiary = Color(0xFFE91E63) // Pink200 */
)

@Suppress("NOTHING_TO_INLINE")
@Composable
fun WifiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val context = LocalContext.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (context as AppCompatActivity).window
        window.statusBarColor = colors.primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewWifiTheme() {
    WifiTheme {
        // Tu contenido de vista previa aquí
    }
}
