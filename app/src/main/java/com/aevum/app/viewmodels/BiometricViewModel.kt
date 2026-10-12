package com.aevum.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.model.PulseAgeTelemetry
import com.aevum.app.data.model.RecoveryOverview
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.data.model.StepsTelemetry
import com.aevum.app.data.model.StreaksTelemetry
import com.aevum.app.data.repository.DefaultTelemetryRepository
import com.aevum.app.data.repository.TelemetryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BiometricViewModel(
    application: Application,
    repository: TelemetryRepository? = null
) : AndroidViewModel(application) {

    val healthConnectManager = HealthConnectManager(application)
    private val telemetryRepository: TelemetryRepository = repository ?: DefaultTelemetryRepository(healthConnectManager)

    val recoveryOverview: StateFlow<RecoveryOverview> = telemetryRepository.getRecoveryOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.aevum.app.data.healthconnect.MockTelemetryProvider.getRecoveryOverview()
        )

    val stepsTelemetry: StateFlow<StepsTelemetry> = telemetryRepository.getStepsTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.aevum.app.data.healthconnect.MockTelemetryProvider.getStepsTelemetry()
        )

    val sleepTelemetry: StateFlow<SleepTelemetry> = telemetryRepository.getSleepTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.aevum.app.data.healthconnect.MockTelemetryProvider.getSleepTelemetry()
        )

    val pulseAgeTelemetry: StateFlow<PulseAgeTelemetry> = telemetryRepository.getPulseAgeTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.aevum.app.data.healthconnect.MockTelemetryProvider.getPulseAgeTelemetry()
        )

    val streaksTelemetry: StateFlow<StreaksTelemetry> = telemetryRepository.getStreaksTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.aevum.app.data.healthconnect.MockTelemetryProvider.getStreaksTelemetry()
        )

    fun refresh() {
        viewModelScope.launch {
            telemetryRepository.refreshTelemetry()
        }
    }
}
