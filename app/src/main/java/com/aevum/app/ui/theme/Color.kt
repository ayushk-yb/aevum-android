package com.aevum.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Midnight Dark Base
val MidnightDark = Color(0xFF0A0A0C)
val SurfaceDark = Color(0xFF131317)
val CardBackground = Color(0xFF16161C)
val CardBackgroundGlass = Color(0xCC16161C)
val CardBorder = Color(0x28FFFFFF)
val CardBorderSubtle = Color(0x14FFFFFF)

// Accent Glows & Biometric States
val CyanGlow = Color(0xFF12E6C8)
val EmeraldGlow = Color(0xFF00E676)
val MintLight = Color(0xFF5FF6D0)
val PurpleGlow = Color(0xFF9D4EDD)
val VioletDeep = Color(0xFF7B2CBF)
val LavenderSoft = Color(0xFFC77DFF)
val CoralGlow = Color(0xFFFF6B4A)
val CoralDeep = Color(0xFFFF4D26)
val AmberGlow = Color(0xFFFFAA00)
val GoldGlow = Color(0xFFFFB703)
val BlueSky = Color(0xFF388AF6)
val BlueDeep = Color(0xFF1E3A8A)

// Text & Neutral Tones
val TextWhite = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8E8EA0)
val TextMuted = Color(0xFF5A5A6E)
val TextDim = Color(0xFF3A3A4A)

// Delta Indicators
val DeltaPositive = Color(0xFF00E676)
val DeltaNegative = Color(0xFFFF5252)

// Gradients
val RecoveryGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFF00E676),
        Color(0xFF12E6C8),
        Color(0xFF00E676)
    )
)

val SleepGradient = Brush.linearGradient(
    colors = listOf(PurpleGlow, LavenderSoft)
)

val StrainGradient = Brush.linearGradient(
    colors = listOf(CoralDeep, CoralGlow)
)

val PulseAgeBlobGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFFFFB703).copy(alpha = 0.85f),
        Color(0xFFFF7B00).copy(alpha = 0.55f),
        Color(0xFFE85D04).copy(alpha = 0.25f),
        Color.Transparent
    )
)
