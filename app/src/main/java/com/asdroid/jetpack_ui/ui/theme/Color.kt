package com.asdroid.jetpack_ui.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand palette — the single source of truth for the whole app.
val ElectricBlue = Color(0xFF080FC3)
val ExtraGreen = Color(0xFF0A6B2F)
val DeepBlack = Color(0xFF000309)

/** Accent used for headings and highlights on the dark gradient. */
val SignalLime = Color(0xFFCDE018)

/** Card surfaces that sit on top of the gradient. */
val InkSurface = Color(0xFF0B1220)
val InkSurfaceVariant = Color(0xFF16213A)

val TextPrimary = Color(0xFFF2F4F8)
val TextSecondary = Color(0xFFB9C1D1)
val TextMuted = Color(0xFF8C93A3)

val WarningAmber = Color(0xFFFFC46B)
val SuccessGreen = Color(0xFF7BD89B)

/** The signature background. Built once and reused by every screen. */
val BrandGradient: Brush = Brush.linearGradient(
    colors = listOf(ElectricBlue, ExtraGreen, DeepBlack),
)
