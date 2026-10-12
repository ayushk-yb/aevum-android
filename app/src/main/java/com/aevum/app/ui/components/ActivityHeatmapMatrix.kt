package com.aevum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.DayHeatmapCell
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun ActivityHeatmapMatrix(
    cells: List<DayHeatmapCell>,
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
            // Header Row: "4 months" and "check in by day"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4 months",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "check in by day",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Month Labels: Jul, Aug, Sep, Oct
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Jul", color = TextMuted, fontSize = 11.sp)
                Text(text = "Aug", color = TextMuted, fontSize = 11.sp)
                Text(text = "Sep", color = TextMuted, fontSize = 11.sp)
                Text(text = "Oct", color = TextMuted, fontSize = 11.sp)
            }

            // Heatmap Grid: 7 rows (days of week) with weekday markers (M, W, F, S)
            val weekdayMarkers = mapOf(1 to "M", 3 to "W", 5 to "F", 6 to "S")
            val totalWeeks = 16 // 4 months * 4 weeks

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (dayOfWeek in 0..6) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Day letter marker
                        Box(
                            modifier = Modifier.size(width = 18.dp, height = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = weekdayMarkers[dayOfWeek] ?: "",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Week cells
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.weight(1f)
                        ) {
                            for (week in 0 until totalWeeks) {
                                val monthIdx = (week / 4).coerceIn(0, 3)
                                val cell = cells.firstOrNull { it.monthIndex == monthIdx && it.dayOfWeek == dayOfWeek }
                                val intensity = cell?.intensity ?: 0f

                                val cellColor = when {
                                    intensity <= 0.05f -> Color(0xFF22222E)
                                    intensity < 0.40f -> Color(0xFF6B4500)
                                    intensity < 0.75f -> Color(0xFFC77700)
                                    else -> AmberGlow
                                }

                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(cellColor)
                                        .border(0.5.dp, Color(0x15FFFFFF), RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Legend Row: "Less" to "More logged that day"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Less", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(end = 6.dp))

                val sampleIntensities = listOf(Color(0xFF22222E), Color(0xFF6B4500), Color(0xFFC77700), AmberGlow)
                sampleIntensities.forEach { col ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(col)
                    )
                }

                Text(
                    text = "More logged that day",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
