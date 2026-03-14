package com.fanyiadrien.ictu_ex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary            = Purple40,
    onPrimary          = NeutralWhite,
    primaryContainer   = Purple90,
    onPrimaryContainer = Purple10,

    secondary              = PurpleGrey40,
    onSecondary            = NeutralWhite,
    secondaryContainer     = PurpleGrey90,
    onSecondaryContainer   = PurpleGrey30,

    tertiary    = Teal40,
    onTertiary  = NeutralWhite,

    background   = NeutralLight,
    onBackground = NeutralDark,
    surface      = NeutralWhite,
    onSurface    = NeutralDark,

    error   = ErrorRed,
    onError = NeutralWhite,
)

private val DarkColors = darkColorScheme(
    primary            = Purple80,
    onPrimary          = Purple20,
    primaryContainer   = Purple30,
    onPrimaryContainer = Purple90,

    secondary              = PurpleGrey80,
    onSecondary            = PurpleGrey30,
    secondaryContainer     = PurpleGrey40,
    onSecondaryContainer   = PurpleGrey90,

    tertiary    = Teal80,
    onTertiary  = NeutralDark,

    background   = NeutralDark,
    onBackground = NeutralLight,
    surface      = Color(0xFF2B2930),
    onSurface    = NeutralLight,

    error   = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

@Composable
fun IctuExTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // ✅ Modern way — no deprecated statusBarColor
            // Makes the status bar fully transparent, letting the app
            // background show through. Icons adjust to light/dark automatically.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                // Light mode → dark icons on status bar (readable on light bg)
                // Dark mode  → light icons on status bar (readable on dark bg)
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}