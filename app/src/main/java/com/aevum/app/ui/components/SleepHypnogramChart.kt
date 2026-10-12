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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.SleepHypnogramSegment
import com.aevum.app.data.model.SleepStage
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun SleepHypnogramChart(
    sleepTelemetry: SleepTelemetry,
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
            // Header Row: "Your night", "11:32 PM"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your night",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = sleepTelemetry.bedTime,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 4-Tier Swimlane Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 4 horizontal row levels:
                    // 0: Awake (top, y = 15% of h)
                    // 1: REM (y = 40% of h)
                    // 2: Light (y = 65% of h)
                    // 3: Deep (bottom, y = 90% of h)
                    val rowHeights = listOf(
                        h * 0.12f, // Awake
                        h * 0.38f, // REM
                        h * 0.65f, // Light
                        h * 0.90f  // Deep
                    )

                    // Draw subtle horizontal grid guidelines
                    rowHeights.forEach { y ->
                        drawLine(
                            color = Color(0x18FFFFFF),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw hypnogram stepped segments and vertical links
                    val stages = sleepTelemetry.stages
                    var prevCenter: Offset? = null

                    stages.forEach { seg ->
                        val startX = seg.startFraction * w
                        val segW = (seg.durationFraction * w).coerceAtLeast(6.dp.toPx())
                        val rowIdx = when (seg.stage) {
                            SleepStage.AWAKE -> 0
                            SleepStage.REM -> 1
                            SleepStage.LIGHT -> 2
                            SleepStage.DEEP -> 3
                        }
                        val y = rowHeights[rowIdx]
                        val capsuleH = 8.dp.toPx()

                        // Vertical connector line from previous segment
                        prevCenter?.let { prev ->
                            drawLine(
                                color = Color(0x55A0AEC0),
                                start = Offset(startX, prev.y),
                                end = Offset(startX, y),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Capsule bar for this stage
                        drawRoundRect(
                            color = seg.stage.color,
                            topLeft = Offset(startX, y - (capsuleH / 2f)),
                            size = Size(segW, capsuleH),
                            cornerRadius = CornerRadius(capsuleH / 2f, capsuleH / 2f)
                        )

                        // If awake, add small glow point
                        if (seg.stage == SleepStage.AWAKE) {
                            drawCircle(
                                color = Color(0xFFFFAA00),
                                radius = 3.dp.toPx(),
                                center = Offset(startX + (segW / 2f), y)
                            )
                        }

                        prevCenter = Offset(startX + segW, y)
                    }
                }
            }

            // Time axis ticks: 11:32 PM, 1:34 AM, 6:44 AM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = sleepTelemetry.bedTime, color = TextMuted, fontSize = 10.sp)
                Text(text = "1:34 AM", color = TextMuted, fontSize = 10.sp)
                Text(text = sleepTelemetry.wakeTime, color = TextMuted, fontSize = 10.sp)
            }

            // Duration Legend Breakdown: Awake, REM, Light, Deep
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Awake
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SleepStage.AWAKE.color))
                    Text(text = "Awake ${sleepTelemetry.awakeDurationFormatted}", color = TextSecondary, fontSize = 10.sp)
                }

                // REM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SleepStage.REM.color))
                    Text(text = "REM ${sleepTelemetry.remDurationFormatted}", color = TextSecondary, fontSize = 10.sp)
                }

                // Light
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SleepStage.LIGHT.color))
                    Text(text = "Light ${sleepTelemetry.lightDurationFormatted}", color = TextSecondary, fontSize = 10.sp)
                }

                // Deep
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SleepStage.DEEP.color))
                    Text(text = "Deep ${sleepTelemetry.deepDurationFormatted}", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}
