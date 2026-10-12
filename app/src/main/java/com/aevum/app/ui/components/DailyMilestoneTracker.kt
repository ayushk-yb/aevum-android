package com.aevum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.GoldGlow

@Composable
fun DailyMilestoneTracker(
    milestones: List<Boolean>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalDays = 10
        for (i in 0 until totalDays) {
            val isCompleted = i < milestones.size && milestones[i]

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isCompleted) {
                            Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(GoldGlow, AmberGlow)
                                    )
                                )
                                .border(1.dp, Color(0x66FFD166), RoundedCornerShape(8.dp))
                        } else {
                            Modifier
                                .background(Color(0xFF22222E))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Day ${i + 1}",
                        tint = Color(0xFF16161C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
