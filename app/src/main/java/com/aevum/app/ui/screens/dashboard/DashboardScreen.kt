package com.aevum.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aevum.app.navigation.Screen
import com.aevum.app.ui.components.DayTimelineBar
import com.aevum.app.ui.components.GlassmorphicCard
import com.aevum.app.ui.components.GlowBackground
import com.aevum.app.ui.components.SparklineCard
import com.aevum.app.ui.components.TripleRingGauge
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.EmeraldGlow
import com.aevum.app.ui.theme.MidnightDark
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite
import com.aevum.app.viewmodels.BiometricViewModel

@Composable
fun DashboardScreen(
    onNavigateToScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BiometricViewModel = viewModel()
) {
    val recoveryOverview by viewModel.recoveryOverview.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
    ) {
        // Ambient Emerald & Cyan Glow Backdrop
        GlowBackground(
            primaryGlowColor = EmeraldGlow,
            secondaryGlowColor = CyanGlow
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            // Top Header: Date Banner & Streak Flame Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FRIDAY, 9 OCTOBER",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Good morning",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Flame streak badge (12)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackgroundGlass)
                        .clickable { onNavigateToScreen(Screen.Streaks) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AmberGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "12",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Triple Ring Recovery Overview
            TripleRingGauge(
                recoveryScore = recoveryOverview.recoveryScore,
                sleepScore = recoveryOverview.sleepScore,
                strainScore = recoveryOverview.strainScore,
                onSleepClick = { onNavigateToScreen(Screen.Sleep) },
                onStrainClick = { onNavigateToScreen(Screen.Steps) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Coaching Status Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                onClick = { onNavigateToScreen(Screen.Steps) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EmeraldGlow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = "Coaching",
                                tint = EmeraldGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = recoveryOverview.coachingMessage,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Bar: "Your Day"
            DayTimelineBar(
                currentTimeFormatted = "10:06",
                segments = recoveryOverview.timelineSegments
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Vitals Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vitals",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All 8",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vitals Grid: HRV and Resting HR cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SparklineCard(
                    metric = recoveryOverview.hrvMetric,
                    lineColor = EmeraldGlow,
                    modifier = Modifier.weight(1f)
                )

                SparklineCard(
                    metric = recoveryOverview.restingHrMetric,
                    lineColor = CyanGlow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
