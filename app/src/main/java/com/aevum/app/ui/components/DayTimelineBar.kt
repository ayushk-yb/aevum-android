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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.TimelineSegment
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun DayTimelineBar(
    currentTimeFormatted: String = "10:06",
    segments: List<TimelineSegment>,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: "Your day" and "10:06"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your day",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentTimeFormatted,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Segmented Linear Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .height(14.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                    val trackHeight = size.height
                    val totalWidth = size.width

                    // Base background track (24h)
                    drawRoundRect(
                        color = Color(0xFF22222C),
                        size = Size(totalWidth, trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                    )

                    // Draw colored segment intervals
                    segments.forEach { segment ->
                        val startX = (segment.startHourFraction / 24f) * totalWidth
                        val endX = (segment.endHourFraction / 24f) * totalWidth
                        val segWidth = (endX - startX).coerceAtLeast(6f)

                        drawRoundRect(
                            color = segment.type.color,
                            topLeft = Offset(startX, 0f),
                            size = Size(segWidth, trackHeight),
                            cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                        )
                    }

                    // Draw subtle current time indicator needle
                    val currentFraction = 10.1f / 24f // ~10:06 AM
                    val currentX = currentFraction * totalWidth
                    drawCircle(
                        color = Color.White,
                        radius = trackHeight / 1.6f,
                        center = Offset(currentX, trackHeight / 2f)
                    )
                }
            }

            // Time axis labels: 12 AM, 6, NOON, 6, 12 AM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "12 AM", color = TextMuted, fontSize = 10.sp)
                Text(text = "6", color = TextMuted, fontSize = 10.sp)
                Text(text = "NOON", color = TextMuted, fontSize = 10.sp)
                Text(text = "6", color = TextMuted, fontSize = 10.sp)
                Text(text = "12 AM", color = TextMuted, fontSize = 10.sp)
            }

            // Duration Legend Pills: Sleep 7:12, Walk 40 min, Stress 35 min
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                segments.forEach { segment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(segment.type.color)
                        )
                        Text(
                            text = segment.durationFormatted,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
