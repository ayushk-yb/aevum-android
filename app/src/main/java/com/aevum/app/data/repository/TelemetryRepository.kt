package com.aevum.app.data.repository

import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.healthconnect.MockTelemetryProvider
import com.aevum.app.data.model.ElevationPoint
import com.aevum.app.data.model.HourlySteps
import com.aevum.app.data.model.PulseAgeTelemetry
import com.aevum.app.data.model.RecoveryOverview
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.data.model.StepsTelemetry
import com.aevum.app.data.model.StreaksTelemetry
import com.aevum.app.data.model.TelemetryDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration

interface TelemetryRepository {
    fun getRecoveryOverview(): Flow<RecoveryOverview>
    fun getStepsTelemetry(): Flow<StepsTelemetry>
    fun getSleepTelemetry(): Flow<SleepTelemetry>
    fun getPulseAgeTelemetry(): Flow<PulseAgeTelemetry>
    fun getStreaksTelemetry(): Flow<StreaksTelemetry>
    fun getDataSource(): Flow<TelemetryDataSource>
    fun setDataSource(source: TelemetryDataSource)
    suspend fun refreshTelemetry()
}

class DefaultTelemetryRepository(
    private val healthConnectManager: HealthConnectManager? = null
) : TelemetryRepository {

    private val dataSourceState = MutableStateFlow(TelemetryDataSource.DEMO)

    private val recoveryState = MutableStateFlow(MockTelemetryProvider.getRecoveryOverview())
    private val stepsState = MutableStateFlow(MockTelemetryProvider.getStepsTelemetry())
    private val sleepState = MutableStateFlow(MockTelemetryProvider.getSleepTelemetry())
    private val pulseAgeState = MutableStateFlow(MockTelemetryProvider.getPulseAgeTelemetry())
    private val streaksState = MutableStateFlow(MockTelemetryProvider.getStreaksTelemetry())

    override fun getRecoveryOverview(): Flow<RecoveryOverview> = recoveryState.asStateFlow()
    override fun getStepsTelemetry(): Flow<StepsTelemetry> = stepsState.asStateFlow()
    override fun getSleepTelemetry(): Flow<SleepTelemetry> = sleepState.asStateFlow()
    override fun getPulseAgeTelemetry(): Flow<PulseAgeTelemetry> = pulseAgeState.asStateFlow()
    override fun getStreaksTelemetry(): Flow<StreaksTelemetry> = streaksState.asStateFlow()
    override fun getDataSource(): Flow<TelemetryDataSource> = dataSourceState.asStateFlow()

    override fun setDataSource(source: TelemetryDataSource) {
        dataSourceState.value = source
        if (source == TelemetryDataSource.DEMO) {
            // Restore demo data
            recoveryState.value = MockTelemetryProvider.getRecoveryOverview()
            stepsState.value = MockTelemetryProvider.getStepsTelemetry()
            sleepState.value = MockTelemetryProvider.getSleepTelemetry()
            pulseAgeState.value = MockTelemetryProvider.getPulseAgeTelemetry()
            streaksState.value = MockTelemetryProvider.getStreaksTelemetry()
        }
    }

    override suspend fun refreshTelemetry() {
        if (dataSourceState.value == TelemetryDataSource.DEMO) return

        if (healthConnectManager != null && healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()) {
            val liveSteps = healthConnectManager.readTodaySteps()
            val liveRhr = healthConnectManager.readRestingHeartRate()
            val liveHrv = healthConnectManager.readLatestHrv()
            val liveActiveKcal = healthConnectManager.readTodayActiveCalories()
            val liveFloors = healthConnectManager.readTodayFloors()
            val liveSleep = healthConnectManager.readLastSleepSession()

            // Update Steps telemetry with live numbers
            val targetSteps = 10000
            val currentSteps = liveSteps.toInt()
            val km = (currentSteps * 0.00075f) // approx 0.75m per step
            val progressPercent = ((currentSteps.toFloat() / targetSteps) * 100).toInt().coerceIn(0, 100)
            val deviationVsUsual = currentSteps - 8100
            val toSummit = (targetSteps - currentSteps).coerceAtLeast(0)

            stepsState.value = stepsState.value.copy(
                currentSteps = currentSteps,
                distanceKm = (Math.round(km * 10.0f) / 10.0f),
                progressPercent = progressPercent,
                deviationVsUsual = deviationVsUsual,
                toSummitSteps = toSummit,
                floorsClimbed = liveFloors.toInt(),
                activeKcal = if (liveActiveKcal > 0) liveActiveKcal.toInt() else stepsState.value.activeKcal,
                summitElevationPath = listOf(
                    ElevationPoint(0.00f, 0.05f),
                    ElevationPoint(0.25f, 0.22f),
                    ElevationPoint((progressPercent / 100f).coerceIn(0.05f, 0.95f), (progressPercent / 100f).coerceIn(0.1f, 0.9f), "now"),
                    ElevationPoint(1.00f, 0.98f, "10,000")
                )
            )

            // Calculate live strain
            val computedStrain = healthConnectManager.computeDailyStrain(
                activeKcal = if (liveActiveKcal > 0) liveActiveKcal.toInt() else 212,
                avgBpm = (liveRhr ?: 60).toInt()
            )

            // Update Recovery & Vitals
            var updatedRecovery = recoveryState.value.copy(
                strainScore = computedStrain
            )

            if (liveRhr != null) {
                updatedRecovery = updatedRecovery.copy(
                    restingHrMetric = updatedRecovery.restingHrMetric.copy(
                        value = liveRhr.toString()
                    )
                )
            }

            if (liveHrv != null) {
                updatedRecovery = updatedRecovery.copy(
                    hrvMetric = updatedRecovery.hrvMetric.copy(
                        value = liveHrv.toInt().toString()
                    )
                )
            }

            // Update Sleep if available
            if (liveSleep != null) {
                val duration = Duration.between(liveSleep.startTime, liveSleep.endTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60
                val totalMinutes = duration.toMinutes().toInt()
                val targetMinutes = 470 // 7h 50m

                sleepState.value = sleepState.value.copy(
                    durationFormatted = "${hours}h ${minutes}m",
                    gotMinutes = totalMinutes,
                    neededMinutes = targetMinutes,
                    deficitMinutes = (targetMinutes - totalMinutes).coerceAtLeast(0),
                    efficiencyPercent = ((totalMinutes.toFloat() / targetMinutes) * 100).toInt().coerceIn(0, 100)
                )

                updatedRecovery = updatedRecovery.copy(
                    sleepScore = sleepState.value.efficiencyPercent
                )
            }

            recoveryState.value = updatedRecovery
        }
    }
}
