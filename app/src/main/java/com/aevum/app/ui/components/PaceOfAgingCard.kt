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
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CoralGlow
import com.aevum.app.ui.theme.EmeraldGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun PaceOfAgingCard(
    paceOfAging: Float = 0.88f,
    paceLabel: String = "0.88 yrs / yr (Optimal)",
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
            // Title & Value Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pace of aging",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = paceLabel,
                    color = AmberGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Gradient scale track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .height(14.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Gradient bar: Slow (Emerald) -> Optimal (Amber) -> High (Coral)
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(EmeraldGlow, AmberGlow, CoralGlow)
                        ),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(h / 2f, h / 2f)
                    )

                    // Pin marker at paceOfAging (normalized between 0.6 and 1.4)
                    val normFraction = ((paceOfAging - 0.6f) / (1.4f - 0.6f)).coerceIn(0.05f, 0.95f)
                    val markerX = normFraction * w

                    drawCircle(
                        color = Color.White,
                        radius = h * 0.7f,
                        center = Offset(markerX, h / 2f)
                    )
                    drawCircle(
                        color = AmberGlow,
                        radius = h * 0.45f,
                        center = Offset(markerX, h / 2f)
                    )
                }
            }

            // Scale legend labels: Slow, Optimal, High
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Slow", color = TextMuted, fontSize = 11.sp)
                Text(text = "Optimal", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = "High", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
