package com.aevum.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.PulseAgeTelemetry
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun ComparativeAgeGauge(
    pulseAgeTelemetry: PulseAgeTelemetry,
    modifier: Modifier = Modifier
) {
    val animPin = remember { Animatable(20f) }

    LaunchedEffect(pulseAgeTelemetry.pulseAge) {
        animPin.animateTo(
            targetValue = pulseAgeTelemetry.pulseAge,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = CardBackgroundGlass,
        borderColor = CardBorderSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Dual Stat Row: "Calendar age 34" vs "Pulse age 31.6"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar age
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Calendar age",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "${pulseAgeTelemetry.calendarAge}",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Pulse age
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pulse age",
                        color = AmberGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = String.format("%.1f", pulseAgeTelemetry.pulseAge),
                        color = AmberGlow,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Calibrated Axis Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(top = 16.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val minAge = 20f
                    val maxAge = 40f
                    val ageRange = maxAge - minAge

                    val trackY = h * 0.40f
                    val paddingX = 16.dp.toPx()
                    val usableW = w - (paddingX * 2)

                    fun ageToX(age: Float): Float {
                        return paddingX + ((age - minAge) / ageRange) * usableW
                    }

                    // Background track line
                    drawLine(
                        color = Color(0xFF282836),
                        start = Offset(paddingX, trackY),
                        end = Offset(w - paddingX, trackY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Calibrated ticks & numbers (20, 25, 30, 35, 40)
                    val ticks = listOf(20, 25, 30, 35, 40)
                    ticks.forEach { tickAge ->
                        val tx = ageToX(tickAge.toFloat())
                        // Tick mark
                        drawLine(
                            color = Color(0xFF424256),
                            start = Offset(tx, trackY - 4.dp.toPx()),
                            end = Offset(tx, trackY + 4.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Tick label text
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(140, 142, 142, 160)
                                textSize = 10.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText("$tickAge", tx, trackY + 22.dp.toPx(), paint)
                        }
                    }

                    // Calendar Age Bracket / Node at 34
                    val calX = ageToX(pulseAgeTelemetry.calendarAge.toFloat())
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(calX, trackY)
                    )

                    // Pulse Age Indicator Pin at 31.6
                    val currentPulseAge = animPin.value
                    val pulseX = ageToX(currentPulseAge)

                    // Connecting link between Calendar Age (34) and Pulse Age (31.6)
                    drawLine(
                        color = AmberGlow.copy(alpha = 0.5f),
                        start = Offset(pulseX, trackY),
                        end = Offset(calX, trackY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Pulse Age indicator badge pin below track
                    val pinY = trackY + 2.dp.toPx()
                    // Glowing outer ring
                    drawCircle(
                        color = AmberGlow.copy(alpha = 0.3f),
                        radius = 16.dp.toPx(),
                        center = Offset(pulseX, pinY)
                    )
                    // White core
                    drawCircle(
                        color = Color.White,
                        radius = 11.dp.toPx(),
                        center = Offset(pulseX, pinY)
                    )
                    drawCircle(
                        color = AmberGlow,
                        radius = 9.dp.toPx(),
                        center = Offset(pulseX, pinY)
                    )

                    // Pin text "31.6" below pin
                    drawContext.canvas.nativeCanvas.apply {
                        val pinTextPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 11.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            String.format("%.1f", currentPulseAge),
                            pulseX,
                            pinY + 26.dp.toPx(),
                            pinTextPaint
                        )
                    }
                }
            }
        }
    }
}
