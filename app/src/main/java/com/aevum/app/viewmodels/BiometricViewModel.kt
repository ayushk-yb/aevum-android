package com.aevum.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.healthconnect.MockTelemetryProvider
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
    application: Application
) : AndroidViewModel(application) {

    val healthConnectManager: HealthConnectManager? = try {
        HealthConnectManager(application)
    } catch (_: Throwable) {
        null
    }

    private var telemetryRepository: TelemetryRepository = DefaultTelemetryRepository(healthConnectManager)

    // Secondary constructor for testing
    constructor(application: Application, repository: TelemetryRepository) : this(application) {
        this.telemetryRepository = repository
    }

    val recoveryOverview: StateFlow<RecoveryOverview> = telemetryRepository.getRecoveryOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockTelemetryProvider.getRecoveryOverview()
        )

    val stepsTelemetry: StateFlow<StepsTelemetry> = telemetryRepository.getStepsTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockTelemetryProvider.getStepsTelemetry()
        )

    val sleepTelemetry: StateFlow<SleepTelemetry> = telemetryRepository.getSleepTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockTelemetryProvider.getSleepTelemetry()
        )

    val pulseAgeTelemetry: StateFlow<PulseAgeTelemetry> = telemetryRepository.getPulseAgeTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockTelemetryProvider.getPulseAgeTelemetry()
        )

    val streaksTelemetry: StateFlow<StreaksTelemetry> = telemetryRepository.getStreaksTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockTelemetryProvider.getStreaksTelemetry()
        )

    fun refresh() {
        viewModelScope.launch {
            try {
                telemetryRepository.refreshTelemetry()
            } catch (_: Throwable) {
                // Ignore any telemetry refresh errors
            }
        }
    }
}
