package com.aevum.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.ElevationPoint
import com.aevum.app.ui.theme.BlueDeep
import com.aevum.app.ui.theme.BlueSky
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SummitElevationChart(
    progressPercent: Int = 52,
    elevationPoints: List<ElevationPoint>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(progressPercent) {
        animProgress.animateTo(
            targetValue = progressPercent / 100f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = CardBackgroundGlass,
        borderColor = CardBorderSubtle
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                val w = size.width
                val h = size.height

                // 1. Starlight Sky Background
                val starRandom = Random(1337)
                for (i in 0..45) {
                    val starX = starRandom.nextFloat() * w
                    val starY = starRandom.nextFloat() * (h * 0.65f)
                    val starAlpha = (starRandom.nextFloat() * 0.7f) + 0.2f
                    val starRadius = (starRandom.nextFloat() * 1.5f).dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        radius = starRadius,
                        center = Offset(starX, starY)
                    )
                }

                // Mountain Curve Path points
                // Sloping up from bottom left (15% height from bottom) to top right summit (15% from top)
                val startX = w * 0.05f
                val startY = h * 0.85f
                val apexX = w * 0.88f
                val apexY = h * 0.22f

                // Mountain Ridge Base Silhouette Fill
                val mountainFill = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, startY)
                    // First lower hill
                    cubicTo(w * 0.25f, h * 0.80f, w * 0.35f, h * 0.60f, w * 0.52f, h * 0.48f)
                    // Ridge to apex
                    cubicTo(w * 0.68f, h * 0.38f, w * 0.78f, h * 0.28f, apexX, apexY)
                    // Down to right edge
                    lineTo(w, h * 0.50f)
                    lineTo(w, h)
                    close()
                }

                drawPath(
                    path = mountainFill,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF182236),
                            Color(0xFF10131A)
                        ),
                        startY = apexY,
                        endY = h
                    )
                )

                // The Climbing Track Line
                val fullPath = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(w * 0.25f, h * 0.80f, w * 0.35f, h * 0.60f, w * 0.52f, h * 0.48f)
                    cubicTo(w * 0.68f, h * 0.38f, w * 0.78f, h * 0.28f, apexX, apexY)
                }

                // 2. Dashed Trajectory for "usual by now" and future climb to summit
                drawPath(
                    path = fullPath,
                    color = Color(0x55388AF6),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                        cap = StrokeCap.Round
                    )
                )

                // 3. Current active climb path (traversed so far)
                val currentProgress = animProgress.value
                val nowX = startX + (w * 0.52f - startX) * (currentProgress / 0.52f).coerceIn(0f, 1f)
                val nowY = startY + (h * 0.48f - startY) * (currentProgress / 0.52f).coerceIn(0f, 1f)

                val activeClimbPath = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        startX + (w * 0.25f - startX) * (currentProgress / 0.52f).coerceIn(0f, 1f),
                        startY + (h * 0.80f - startY) * (currentProgress / 0.52f).coerceIn(0f, 1f),
                        startX + (w * 0.35f - startX) * (currentProgress / 0.52f).coerceIn(0f, 1f),
                        startY + (h * 0.60f - startY) * (currentProgress / 0.52f).coerceIn(0f, 1f),
                        nowX,
                        nowY
                    )
                }

                // Neon active glow line
                drawPath(
                    path = activeClimbPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(BlueSky, CyanGlow),
                        startX = startX,
                        endX = nowX
                    ),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Sparkle particles along active climb
                val particleRandom = Random(999)
                for (p in 0..18) {
                    val frac = particleRandom.nextFloat() * (currentProgress / 0.52f).coerceIn(0f, 1f)
                    val px = startX + (nowX - startX) * frac + (particleRandom.nextFloat() * 20f - 10f)
                    val py = startY + (nowY - startY) * frac + (particleRandom.nextFloat() * 16f - 8f)
                    drawCircle(
                        color = CyanGlow.copy(alpha = 0.75f),
                        radius = 1.6.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // 4. Glowing "Now" Node Circle
                // Outer glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(nowX, nowY),
                        radius = 18.dp.toPx()
                    ),
                    radius = 18.dp.toPx(),
                    center = Offset(nowX, nowY)
                )
                // Solid node core
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(nowX, nowY)
                )
                drawCircle(
                    color = CyanGlow,
                    radius = 7.dp.toPx(),
                    center = Offset(nowX, nowY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Summit Apex Flag ("10,000")
                // Pole
                drawLine(
                    color = BlueSky,
                    start = Offset(apexX, apexY),
                    end = Offset(apexX, apexY - 26.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Small flag pill background
                val flagW = 54.dp.toPx()
                val flagH = 20.dp.toPx()
                val flagLeft = apexX
                val flagTop = apexY - 26.dp.toPx()

                drawRoundRect(
                    color = Color(0xFF254B8C),
                    topLeft = Offset(flagLeft, flagTop),
                    size = Size(flagW, flagH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Apex flag text drawn with native Canvas
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 10.sp.toPx()
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("10,000", flagLeft + (flagW / 2f), flagTop + (flagH * 0.72f), paint)
                }

                // "usual by now" caption below trajectory
                drawContext.canvas.nativeCanvas.apply {
                    val usualPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(170, 142, 142, 160)
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                    drawText("usual by now", w * 0.58f, h * 0.62f, usualPaint)
                }
            }
        }
    }
}
