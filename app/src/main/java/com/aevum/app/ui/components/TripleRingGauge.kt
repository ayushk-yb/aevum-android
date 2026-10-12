package com.aevum.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.EmeraldGlow
import com.aevum.app.ui.theme.LavenderSoft
import com.aevum.app.ui.theme.PurpleGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TripleRingGauge(
    recoveryScore: Int,
    sleepScore: Int,
    strainScore: Float,
    onSleepClick: () -> Unit,
    onStrainClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedRecovery = remember { Animatable(0f) }
    val animatedSleep = remember { Animatable(0f) }
    val animatedStrain = remember { Animatable(0f) }

    LaunchedEffect(recoveryScore, sleepScore, strainScore) {
        animatedRecovery.animateTo(
            targetValue = recoveryScore / 100f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        animatedSleep.animateTo(
            targetValue = sleepScore / 100f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        animatedStrain.animateTo(
            targetValue = (strainScore / 21f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Radial Background Glow for Recovery
        Canvas(
            modifier = Modifier
                .size(260.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EmeraldGlow.copy(alpha = 0.18f),
                        CyanGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Sleep Ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSleepClick() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val strokeW = 7.dp.toPx()
                        // Track
                        drawCircle(
                            color = Color(0xFF261D3A),
                            style = Stroke(width = strokeW)
                        )
                        // Progress arc
                        val sweep = animatedSleep.value * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(PurpleGlow, LavenderSoft, PurpleGlow)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$sleepScore%",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Sleep",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Center: Large Recovery Arc
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(170.dp)) {
                        val strokeW = 12.dp.toPx()
                        val padding = strokeW / 2f
                        val arcSize = Size(size.width - strokeW, size.height - strokeW)
                        val topLeft = Offset(padding, padding)
                        val startAngle = 135f
                        val maxSweep = 270f

                        // Subtle Radial Tick Marks
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val innerRadius = (size.width / 2f) - strokeW - 12.dp.toPx()
                        val outerRadius = innerRadius + 6.dp.toPx()
                        for (i in 0..24) {
                            val tickAngleDeg = startAngle + (i.toFloat() / 24f) * maxSweep
                            val tickRad = Math.toRadians(tickAngleDeg.toDouble())
                            val x1 = cx + (innerRadius * cos(tickRad)).toFloat()
                            val y1 = cy + (innerRadius * sin(tickRad)).toFloat()
                            val x2 = cx + (outerRadius * cos(tickRad)).toFloat()
                            val y2 = cy + (outerRadius * sin(tickRad)).toFloat()
                            drawLine(
                                color = Color(0x335FF6D0),
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Background track
                        drawArc(
                            color = Color(0xFF132A26),
                            startAngle = startAngle,
                            sweepAngle = maxSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // Active Glowing Arc
                        val currentSweep = animatedRecovery.value * maxSweep
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(EmeraldGlow, CyanGlow, EmeraldGlow)
                            ),
                            startAngle = startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$recoveryScore%",
                            color = TextWhite,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Recovery",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Strain Ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onStrainClick() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val strokeW = 7.dp.toPx()
                        // Track
                        drawCircle(
                            color = Color(0xFF351F1A),
                            style = Stroke(width = strokeW)
                        )
                        // Progress arc
                        val sweep = animatedStrain.value * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFFF4D26),
                                    Color(0xFFFF8A65),
                                    Color(0xFFFF4D26)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = String.format("%.1f", strainScore),
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Strain",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
