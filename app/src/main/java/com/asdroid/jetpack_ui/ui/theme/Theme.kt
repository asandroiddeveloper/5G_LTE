package com.asdroid.jetpack_ui.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * One brand scheme for the whole app.
 *
 * Deliberately not using dynamic colour: the app is a dark, branded gradient UI,
 * and a wallpaper-derived palette was fighting the hardcoded brand colours that
 * every screen used to paint inline.
 */
private val BrandColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = InkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = ExtraGreen,
    onSecondary = Color.White,
    tertiary = SignalLime,
    onTertiary = DeepBlack,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = InkSurface,
    onSurface = TextPrimary,
    surfaceVariant = InkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    error = WarningAmber,
    onError = DeepBlack,
)

@Composable
fun JetPackUITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrandColorScheme,
        typography = AppTypography,
        content = content,
    )
}
