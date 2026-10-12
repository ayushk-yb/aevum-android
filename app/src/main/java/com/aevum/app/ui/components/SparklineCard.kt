package com.aevum.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.VitalMetric
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.DeltaPositive
import com.aevum.app.ui.theme.EmeraldGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun SparklineCard(
    metric: VitalMetric,
    lineColor: Color = EmeraldGlow,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = CardBackgroundGlass,
        borderColor = CardBorderSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Label
            Text(
                text = metric.title,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            // Value + Unit + Delta Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = metric.value,
                        color = TextWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = metric.unit,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                // Delta pill (e.g. +11 or -3)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeltaPositive.copy(alpha = 0.14f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val isUp = metric.delta.startsWith("+")
                        Icon(
                            imageVector = if (isUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = DeltaPositive,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = metric.delta.replace("+", "").replace("-", ""),
                            color = DeltaPositive,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Neon sparkline waveform
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    if (metric.waveformPoints.isEmpty()) return@Canvas

                    val points = metric.waveformPoints
                    val min = points.minOrNull() ?: 0f
                    val max = points.maxOrNull() ?: 1f
                    val range = if (max - min == 0f) 1f else (max - min)

                    val width = size.width
                    val height = size.height
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { index, value ->
                        val normY = 1f - ((value - min) / range)
                        // keep slight padding on top and bottom
                        val y = (normY * (height * 0.75f)) + (height * 0.12f)
                        val x = index * stepX

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevVal = points[index - 1]
                            val prevNormY = 1f - ((prevVal - min) / range)
                            val prevY = (prevNormY * (height * 0.75f)) + (height * 0.12f)

                            val midX = (prevX + x) / 2f
                            path.cubicTo(midX, prevY, midX, y, x, y)
                            fillPath.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Gradient fill under waveform
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Stroke line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
