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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.aevum.app.ui.components.ComparativeAgeGauge
import com.aevum.app.ui.components.FluidMorphingBlob
import com.aevum.app.ui.components.GlowBackground
import com.aevum.app.ui.components.PaceOfAgingCard
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.GoldGlow
import com.aevum.app.ui.theme.MidnightDark
import com.aevum.app.ui.theme.TextMuted
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
                    onClick = { /* Pulse Age algorithm details */ },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardBackgroundGlass)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Information",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Badge: "Based on your last 30 days"
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

            // Organic Fluid Morphing Blob Hero
            FluidMorphingBlob(
                pulseAge = pulseAgeTelemetry.pulseAge,
                deltaYears = pulseAgeTelemetry.deltaYears
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Comparative Gauge Card: Calendar age 34 vs Pulse age 31.6
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
    }
}
