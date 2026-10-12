package com.aevum.app.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.HourlySteps
import com.aevum.app.ui.theme.BlueDeep
import com.aevum.app.ui.theme.BlueSky
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun DaySkylineHistogram(
    hourlyData: List<HourlySteps>,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = CardBackgroundGlass,
        borderColor = CardBorderSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: "Day skyline", "By metrics"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day skyline",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "By metrics",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Vertical Histogram Bars Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val maxSteps = 2400f
                    val totalBars = 24
                    val barSpacing = 4.dp.toPx()
                    val barWidth = ((w - (barSpacing * (totalBars - 1))) / totalBars).coerceAtLeast(3.dp.toPx())

                    for (hour in 0 until totalBars) {
                        val data = hourlyData.firstOrNull { it.hour == hour }
                        val steps = data?.steps ?: 0
                        val usual = data?.usualSteps ?: 0
                        val x = hour * (barWidth + barSpacing)

                        // Draw subtle grey "usual" ghost bar in background
                        if (usual > 0) {
                            val usualHeight = ((usual / maxSteps) * h).coerceIn(4f, h)
                            drawRoundRect(
                                color = Color(0x288E8EA0),
                                topLeft = Offset(x, h - usualHeight),
                                size = Size(barWidth, usualHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }

                        // Draw active blue step bar
                        if (steps > 0) {
                            val barHeight = ((steps / maxSteps) * h).coerceIn(6f, h)
                            val isPeak = data?.isPeak == true

                            val barBrush = if (isPeak) {
                                Brush.verticalGradient(
                                    colors = listOf(CyanGlow, BlueSky),
                                    startY = h - barHeight,
                                    endY = h
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(BlueSky, BlueDeep),
                                    startY = h - barHeight,
                                    endY = h
                                )
                            }

                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(x, h - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )

                            // Peak halo glow on top of bar
                            if (isPeak) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(CyanGlow.copy(alpha = 0.5f), Color.Transparent),
                                        center = Offset(x + (barWidth / 2f), h - barHeight),
                                        radius = 12.dp.toPx()
                                    ),
                                    radius = 12.dp.toPx(),
                                    center = Offset(x + (barWidth / 2f), h - barHeight)
                                )
                            }
                        } else {
                            // Subdued flat floor tick
                            drawRoundRect(
                                color = Color(0xFF1E222D),
                                topLeft = Offset(x, h - 3.dp.toPx()),
                                size = Size(barWidth, 3.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                            )
                        }
                    }
                }
            }

            // Footnote: "peak 7–8 AM • grey = usual"
            Text(
                text = "peak 7–8 AM • grey = usual",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
