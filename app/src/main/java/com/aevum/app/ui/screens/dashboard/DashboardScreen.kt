package com.aevum.app.ui.screens.dashboard

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aevum.app.data.model.TelemetryDataSource
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
import com.aevum.app.ui.theme.GoldGlow
import com.aevum.app.ui.theme.MidnightDark
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite
import com.aevum.app.viewmodels.BiometricViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onNavigateToScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BiometricViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recoveryOverview by viewModel.recoveryOverview.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()
    val scrollState = rememberScrollState()

    var showNotAvailableDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
    }

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

            Spacer(modifier = Modifier.height(14.dp))

            // Health Connect Connection & Sync Status Banner
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = if (dataSource == TelemetryDataSource.LIVE) Color(0x1A00E676) else CardBackgroundGlass,
                borderColor = if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow.copy(alpha = 0.35f) else CardBorderSubtle,
                onClick = {
                    if (dataSource == TelemetryDataSource.LIVE) {
                        // Toggle back to Demo preview
                        viewModel.setDataSource(TelemetryDataSource.DEMO)
                    } else {
                        // Connect to Health Connect
                        if (viewModel.isHealthConnectAvailable) {
                            scope.launch {
                                val hasAll = viewModel.healthConnectManager?.hasAllPermissions() == true
                                if (hasAll) {
                                    viewModel.setDataSource(TelemetryDataSource.LIVE)
                                } else {
                                    permissionLauncher.launch(viewModel.permissions)
                                }
                            }
                        } else {
                            showNotAvailableDialog = true
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow else AmberGlow
                                )
                        )
                        Text(
                            text = if (dataSource == TelemetryDataSource.LIVE) "Health Connect Live Sync" else "Demo Mode • Tap to connect Health Connect",
                            color = if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow.copy(alpha = 0.2f)
                                else CyanGlow.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (dataSource == TelemetryDataSource.LIVE) "Connected" else "Connect",
                            color = if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow else CyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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

        // Dialog when Health Connect app is not installed
        if (showNotAvailableDialog) {
            AlertDialog(
                onDismissRequest = { showNotAvailableDialog = false },
                containerColor = Color(0xFF181822),
                title = {
                    Text(
                        text = "Google Health Connect",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Health Connect is not currently active on this device. Install or enable Google Health Connect to sync live continuous telemetry, or continue exploring with Aevum Demo Telemetry.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNotAvailableDialog = false
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=com.google.android.apps.healthdata")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                                )
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow)
                    ) {
                        Text("Get Health Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotAvailableDialog = false }) {
                        Text("Keep Demo Mode", color = TextSecondary)
                    }
                }
            )
        }
    }
}
