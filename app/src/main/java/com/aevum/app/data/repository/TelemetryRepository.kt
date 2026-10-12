package com.aevum.app.data.repository

import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.healthconnect.MockTelemetryProvider
import com.aevum.app.data.model.PulseAgeTelemetry
import com.aevum.app.data.model.RecoveryOverview
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.data.model.StepsTelemetry
import com.aevum.app.data.model.StreaksTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TelemetryRepository {
    fun getRecoveryOverview(): Flow<RecoveryOverview>
    fun getStepsTelemetry(): Flow<StepsTelemetry>
    fun getSleepTelemetry(): Flow<SleepTelemetry>
    fun getPulseAgeTelemetry(): Flow<PulseAgeTelemetry>
    fun getStreaksTelemetry(): Flow<StreaksTelemetry>
    suspend fun refreshTelemetry()
}

class DefaultTelemetryRepository(
    private val healthConnectManager: HealthConnectManager? = null
) : TelemetryRepository {

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

    override suspend fun refreshTelemetry() {
        if (healthConnectManager != null && healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()) {
            val liveSteps = healthConnectManager.readTodaySteps()
            val liveRhr = healthConnectManager.readRestingHeartRate()
            val liveHrv = healthConnectManager.readLatestHrv()

            if (liveSteps > 0) {
                stepsState.value = stepsState.value.copy(
                    currentSteps = liveSteps.toInt(),
                    progressPercent = ((liveSteps.toFloat() / stepsState.value.targetSteps) * 100).toInt()
                )
            }

            if (liveRhr != null) {
                recoveryState.value = recoveryState.value.copy(
                    restingHrMetric = recoveryState.value.restingHrMetric.copy(
                        value = liveRhr.toString()
                    )
                )
            }

            if (liveHrv != null) {
                recoveryState.value = recoveryState.value.copy(
                    hrvMetric = recoveryState.value.hrvMetric.copy(
                        value = liveHrv.toInt().toString()
                    )
                )
            }
        }
    }
}
