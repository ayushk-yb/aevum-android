package com.aevum.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.LavenderSoft
import com.aevum.app.ui.theme.PurpleGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun NeedVsGotCard(
    sleepTelemetry: SleepTelemetry,
    modifier: Modifier = Modifier
) {
    val animGot = remember { Animatable(0f) }

    LaunchedEffect(sleepTelemetry.gotMinutes, sleepTelemetry.neededMinutes) {
        val targetFraction = (sleepTelemetry.gotMinutes.toFloat() / sleepTelemetry.neededMinutes.toFloat())
            .coerceIn(0f, 1f)
        animGot.animateTo(
            targetValue = targetFraction,
            animationSpec = tween(1100, easing = FastOutSlowInEasing)
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
                .padding(18.dp)
        ) {
            // Header Row: "Need vs got" + Deficit Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need vs got",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Pill: "38 min short" / "sleep deficit"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberGlow.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${sleepTelemetry.deficitMinutes} min deficit",
                        color = AmberGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Split Comparative Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .height(18.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val corner = CornerRadius(h / 2f, h / 2f)

                    // Target background track
                    drawRoundRect(
                        color = Color(0xFF231E33),
                        size = Size(w, h),
                        cornerRadius = corner
                    )

                    // Filled "Got" progress
                    val gotW = w * animGot.value
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PurpleGlow, LavenderSoft),
                            startX = 0f,
                            endX = gotW
                        ),
                        size = Size(gotW, h),
                        cornerRadius = corner
                    )

                    // White baseline target marker at full width
                    drawLine(
                        color = Color.White,
                        start = Offset(w - 2.dp.toPx(), 0f),
                        end = Offset(w - 2.dp.toPx(), h),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Subtext metrics comparison: "Got 7h 12m" vs "Needed 7h 50m"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Got ${sleepTelemetry.durationFormatted}",
                    color = LavenderSoft,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Needed 7h 50m",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
