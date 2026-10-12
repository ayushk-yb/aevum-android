package com.aevum.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GlowBackground(
    primaryGlowColor: Color = Color(0xFF00E676),
    secondaryGlowColor: Color = Color(0xFF9D4EDD),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top-center ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryGlowColor.copy(alpha = 0.12f),
                    primaryGlowColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.22f),
                radius = width * 0.65f
            ),
            radius = width * 0.65f,
            center = Offset(width * 0.5f, height * 0.22f)
        )

        // Subtle secondary accent glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryGlowColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(width * 0.85f, height * 0.55f),
                radius = width * 0.5f
            ),
            radius = width * 0.5f,
            center = Offset(width * 0.85f, height * 0.55f)
        )
    }
}
