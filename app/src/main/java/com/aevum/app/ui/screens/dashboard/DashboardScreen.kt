package com.aevum.app.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.model.TelemetryDataSource
import com.aevum.app.navigation.Screen
import com.aevum.app.ui.components.BiometricInfoDialog
import com.aevum.app.ui.components.DayTimelineBar
import com.aevum.app.ui.components.GlassmorphicCard
import com.aevum.app.ui.components.GlowBackground
import com.aevum.app.ui.components.InfoSection
import com.aevum.app.ui.components.SparklineCard
import com.aevum.app.ui.components.StartupConnectionDialog
import com.aevum.app.ui.components.TripleRingGauge
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.EmeraldGlow
import com.aevum.app.ui.theme.MidnightDark
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
    val streaksTelemetry by viewModel.streaksTelemetry.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()
    val scrollState = rememberScrollState()
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    var showStartupDialog by remember { mutableStateOf(dataSource != TelemetryDataSource.LIVE) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Auto-check permissions and refresh live data whenever returning from Health Connect Settings
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionsAndSync()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun openHealthConnectSettings(ctx: Context) {
        val intents = listOf(
            Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"),
            Intent("android.health.connect.action.HEALTH_HOME_SETTINGS"),
            Intent("android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"),
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", ctx.packageName, null)
            }
        )
        for (intent in intents) {
            try {
                ctx.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permsMap ->
        val granted = permsMap.filterValues { it }.keys
        viewModel.onPermissionsResult(granted)
        if (granted.isNotEmpty()) {
            showStartupDialog = false
        }
    }

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.onPermissionsResult(grantedPermissions)
        if (grantedPermissions.isNotEmpty()) {
            showStartupDialog = false
        }
    }

    fun triggerConnect() {
        val perms = viewModel.permissions.ifEmpty { HealthConnectManager.PERMISSIONS }
        try {
            // Primary: Standard Health Connect contract
            healthConnectPermissionLauncher.launch(perms)
        } catch (_: Exception) {
            try {
                // Secondary fallback: Standard Android runtime permissions contract
                runtimePermissionLauncher.launch(perms.toTypedArray())
            } catch (_: Exception) {
                // Tertiary fallback: Directly open Health Connect settings / permissions manager
                openHealthConnectSettings(context)
            }
        }
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
            // Top Header: Date Banner, Streak Flame Badge & Info Button
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flame streak badge (live or demo)
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
                                text = "${streaksTelemetry.currentStreakDays}",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // (i) Info trigger button
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardBackgroundGlass)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Day Overview Info",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
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
                        viewModel.refresh()
                    } else {
                        triggerConnect()
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
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
                            text = if (dataSource == TelemetryDataSource.LIVE) "Health Connect Live • Tap to refresh" else "Demo Mode • Tap to connect Health Connect",
                            color = if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (dataSource == TelemetryDataSource.LIVE) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackgroundGlass)
                                    .clickable { viewModel.setDataSource(TelemetryDataSource.DEMO) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Demo",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                                text = if (dataSource == TelemetryDataSource.LIVE) "Live Sync" else "Connect",
                                color = if (dataSource == TelemetryDataSource.LIVE) EmeraldGlow else CyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

        // Startup Health Connect Connection Dialog
        if (showStartupDialog && dataSource != TelemetryDataSource.LIVE) {
            StartupConnectionDialog(
                onConnectClick = {
                    triggerConnect()
                },
                onOpenSettingsClick = {
                    openHealthConnectSettings(context)
                },
                onUseDemoDataClick = {
                    showStartupDialog = false
                }
            )
        }

        // (i) Info Dialog for Day Overview
        if (showInfoDialog) {
            BiometricInfoDialog(
                title = "Day Overview",
                subtitle = "Biometric Telemetry Engine",
                accentColor = EmeraldGlow,
                sections = listOf(
                    InfoSection(
                        "Recovery Score (72%)",
                        "A composite index (0–100%) reflecting autonomic recovery, sleep debt repayment, and resting cardiovascular strain. Green (>66%) signifies your body is primed for cardiovascular adaptation."
                    ),
                    InfoSection(
                        "Sleep Performance (88%)",
                        "Quantifies total restorative sleep against your biological sleep requirement. Combines slow-wave deep sleep and REM consistency."
                    ),
                    InfoSection(
                        "Daily Strain (6.4)",
                        "A logarithmic exertion metric (0–21) measuring total cardiovascular load across heart rate zones, active energy burned, and training intensity."
                    ),
                    InfoSection(
                        "Heart Rate Variability (HRV 54 ms)",
                        "RMSSD measurement of intervals between heartbeats. Higher HRV signals optimal parasympathetic tone and physiological resilience."
                    ),
                    InfoSection(
                        "Resting Heart Rate (57 bpm)",
                        "Basal cardiovascular efficiency recorded during sleep. Downward trends indicate strengthening aerobic conditioning."
                    )
                ),
                onDismiss = { showInfoDialog = false }
            )
        }
    }
}
