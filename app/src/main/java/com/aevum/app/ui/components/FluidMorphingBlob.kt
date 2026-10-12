package com.aevum.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.GoldGlow
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FluidMorphingBlob(
    pulseAge: Float = 31.6f,
    deltaYears: Float = -2.4f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob_animation")

    // Continuous smooth phase oscillation for organic morphing
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.width * 0.38f) * breathingScale

            // Draw floating particulate speckles around the blob
            val speckleRandom = Random(777)
            for (i in 0..28) {
                val orbitRadius = baseRadius * (1.1f + speckleRandom.nextFloat() * 0.45f)
                val angle = (speckleRandom.nextFloat() * 2 * PI.toFloat()) + (phase * 0.4f * (if (i % 2 == 0) 1f else -1f))
                val sx = center.x + orbitRadius * cos(angle)
                val sy = center.y + orbitRadius * sin(angle)
                val sAlpha = (0.25f + 0.5f * sin(phase + i.toFloat()).coerceAtLeast(0f))
                drawCircle(
                    color = GoldGlow.copy(alpha = sAlpha),
                    radius = (1.2f + speckleRandom.nextFloat() * 2.0f).dp.toPx(),
                    center = Offset(sx, sy)
                )
            }

            // Function to generate smooth organic blob path
            fun createBlobPath(r: Float, waveAmp: Float, waveFreq: Int, offsetPhase: Float): Path {
                val path = Path()
                val numPoints = 64
                val angleStep = (2 * PI / numPoints).toFloat()

                var firstPoint = Offset.Zero
                for (i in 0 until numPoints) {
                    val angle = i * angleStep
                    // Organic harmonic perturbation
                    val distortion = sin(angle * waveFreq + offsetPhase) * waveAmp +
                            cos(angle * 2f - offsetPhase * 0.8f) * (waveAmp * 0.5f)
                    val currentR = r + distortion
                    val px = center.x + currentR * cos(angle)
                    val py = center.y + currentR * sin(angle)

                    if (i == 0) {
                        firstPoint = Offset(px, py)
                        path.moveTo(px, py)
                    } else {
                        path.lineTo(px, py)
                    }
                }
                path.lineTo(firstPoint.x, firstPoint.y)
                path.close()
                return path
            }

            // Layer 1: Outermost soft translucent ambient glow
            val outerPath = createBlobPath(baseRadius * 1.08f, 18.dp.toPx(), 4, phase)
            drawPath(
                path = outerPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE85D04).copy(alpha = 0.35f),
                        Color(0xFFFF7B00).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                )
            )

            // Layer 2: Middle warm glowing amber layer
            val midPath = createBlobPath(baseRadius * 0.95f, 14.dp.toPx(), 5, phase + 1.2f)
            drawPath(
                path = midPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF9E00).copy(alpha = 0.65f),
                        Color(0xFFE85D04).copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.15f
                )
            )

            // Layer 3: Inner luminous fluid golden core
            val innerPath = createBlobPath(baseRadius * 0.82f, 10.dp.toPx(), 6, phase + 2.5f)
            drawPath(
                path = innerPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD166).copy(alpha = 0.92f),
                        Color(0xFFFFAA00).copy(alpha = 0.75f),
                        Color(0xFFE85D04).copy(alpha = 0.45f)
                    ),
                    center = center,
                    radius = baseRadius * 0.95f
                )
            )
        }

        // Center Hero Overlay Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", pulseAge),
                color = TextWhite,
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2.0).sp,
                lineHeight = 56.sp
            )
            Text(
                text = "${kotlin.math.abs(deltaYears)} years younger",
                color = TextWhite.copy(alpha = 0.92f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
