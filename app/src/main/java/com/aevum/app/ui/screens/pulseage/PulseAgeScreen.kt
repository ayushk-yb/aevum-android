package com.aevum.app.ui.screens.pulseage

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aevum.app.ui.components.BiometricInfoDialog
import com.aevum.app.ui.components.ComparativeAgeGauge
import com.aevum.app.ui.components.ConfigureAgeDialog
import com.aevum.app.ui.components.FluidMorphingBlob
import com.aevum.app.ui.components.GlassmorphicCard
import com.aevum.app.ui.components.GlowBackground
import com.aevum.app.ui.components.InfoSection
import com.aevum.app.ui.components.PaceOfAgingCard
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.GoldGlow
import com.aevum.app.ui.theme.MidnightDark
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite
import com.aevum.app.viewmodels.BiometricViewModel

@Composable
fun PulseAgeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BiometricViewModel = viewModel()
) {
    val pulseAgeTelemetry by viewModel.pulseAgeTelemetry.collectAsState()
    val scrollState = rememberScrollState()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showConfigureAgeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
    ) {
        // Ambient Amber & Golden Glow
        GlowBackground(
            primaryGlowColor = AmberGlow,
            secondaryGlowColor = GoldGlow
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            // Header: Back button, Title & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CardBackgroundGlass)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Pulse Age",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Updated weekly",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardBackgroundGlass)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Longevity Methodology Info",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Badge & Edit Age Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackgroundGlass)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Based on your last 30 days",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Interactive Configure Age Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberGlow.copy(alpha = 0.15f))
                        .clickable { showConfigureAgeDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Age",
                            tint = AmberGlow,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Age ${pulseAgeTelemetry.calendarAge}",
                            color = AmberGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Organic Fluid Morphing Blob Hero
            FluidMorphingBlob(
                pulseAge = pulseAgeTelemetry.pulseAge,
                deltaYears = pulseAgeTelemetry.deltaYears
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Comparative Gauge Card: Calendar age vs Pulse age
            ComparativeAgeGauge(
                pulseAgeTelemetry = pulseAgeTelemetry
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Pace of Aging Gradient Track Card
            PaceOfAgingCard(
                paceOfAging = pulseAgeTelemetry.paceOfAging,
                paceLabel = pulseAgeTelemetry.paceLabel
            )
        }

        // Configure Calendar Age Dialog
        if (showConfigureAgeDialog) {
            ConfigureAgeDialog(
                initialAge = pulseAgeTelemetry.calendarAge,
                onSaveAge = { newAge ->
                    viewModel.updateCalendarAge(newAge)
                },
                onDismiss = { showConfigureAgeDialog = false }
            )
        }

        // (i) Info Dialog for Pulse Age
        if (showInfoDialog) {
            BiometricInfoDialog(
                title = "Biological Longevity",
                subtitle = "Pulse Age Methodology",
                accentColor = AmberGlow,
                sections = listOf(
                    InfoSection(
                        "Biological Pulse Age (31.6)",
                        "Calculated from longitudinal cardiovascular metrics: resting heart rate slope, nocturnal HRV RMSSD recovery, and cardiovascular strain efficiency. A lower pulse age indicates superior cellular and arterial health relative to chronological peers."
                    ),
                    InfoSection(
                        "Calendar Age (${pulseAgeTelemetry.calendarAge})",
                        "Your chronological age. Tap 'Configure Calendar Age' below to customize your actual age and recalculate your biological delta in real-time."
                    ),
                    InfoSection(
                        "Pace of Aging (0.88 yrs / yr)",
                        "A longitudinal biological speed index. A score below 1.0 indicates your body is aging at a decelerated pace, typical of individuals with high physical conditioning and low systemic inflammation."
                    )
                ),
                actionButtonText = "Configure Calendar Age",
                onActionClick = { showConfigureAgeDialog = true },
                onDismiss = { showInfoDialog = false }
            )
        }
    }
}
