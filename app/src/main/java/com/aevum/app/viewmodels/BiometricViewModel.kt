package com.aevum.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.healthconnect.MockTelemetryProvider
import com.aevum.app.data.model.DataSourceFilterMode
import com.aevum.app.data.model.DiscoveredSource
import com.aevum.app.data.model.PulseAgeTelemetry
import com.aevum.app.data.model.RecoveryOverview
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.data.model.StepsTelemetry
import com.aevum.app.data.model.StreaksTelemetry
import com.aevum.app.data.model.TelemetryDataSource
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

    private var telemetryRepository: TelemetryRepository =
        DefaultTelemetryRepository.getInstance(healthConnectManager, application)

    // Secondary constructor for testing
    constructor(application: Application, repository: TelemetryRepository) : this(application) {
        this.telemetryRepository = repository
    }

    val dataSource: StateFlow<TelemetryDataSource> = telemetryRepository.getDataSource()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TelemetryDataSource.DEMO
        )

    val filterMode: StateFlow<DataSourceFilterMode> = telemetryRepository.getFilterMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DataSourceFilterMode.FITBIT_ONLY
        )

    val selectedSourcePackage: StateFlow<String?> = telemetryRepository.getSelectedSourcePackage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val discoveredSources: StateFlow<List<DiscoveredSource>> = telemetryRepository.getDiscoveredSources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val recoveryOverview: StateFlow<RecoveryOverview> = telemetryRepository.getRecoveryOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MockTelemetryProvider.getRecoveryOverview()
        )

    val stepsTelemetry: StateFlow<StepsTelemetry> = telemetryRepository.getStepsTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MockTelemetryProvider.getStepsTelemetry()
        )

    val sleepTelemetry: StateFlow<SleepTelemetry> = telemetryRepository.getSleepTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MockTelemetryProvider.getSleepTelemetry()
        )

    val pulseAgeTelemetry: StateFlow<PulseAgeTelemetry> = telemetryRepository.getPulseAgeTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MockTelemetryProvider.getPulseAgeTelemetry()
        )

    val streaksTelemetry: StateFlow<StreaksTelemetry> = telemetryRepository.getStreaksTelemetry()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MockTelemetryProvider.getStreaksTelemetry()
        )

    val isHealthConnectAvailable: Boolean
        get() = healthConnectManager?.isAvailable() == true

    val permissions: Set<String>
        get() = healthConnectManager?.permissions ?: HealthConnectManager.PERMISSIONS

    init {
        checkPermissionsAndSync()
    }

    fun checkPermissionsAndSync() {
        viewModelScope.launch {
            try {
                if (healthConnectManager?.hasAnyPermission() == true) {
                    telemetryRepository.setDataSource(TelemetryDataSource.LIVE)
                    telemetryRepository.refreshTelemetry()
                } else if (dataSource.value == TelemetryDataSource.LIVE) {
                    telemetryRepository.refreshTelemetry()
                }
            } catch (_: Throwable) {
            }
        }
    }

    fun setDataSource(source: TelemetryDataSource) {
        telemetryRepository.setDataSource(source)
        if (source == TelemetryDataSource.LIVE) {
            refresh()
        }
    }

    fun setFilterMode(mode: DataSourceFilterMode, targetPackage: String? = null) {
        telemetryRepository.setFilterMode(mode, targetPackage)
        if (dataSource.value == TelemetryDataSource.LIVE) {
            refresh()
        }
    }

    fun toggleDataSource() {
        val next = if (dataSource.value == TelemetryDataSource.DEMO) TelemetryDataSource.LIVE else TelemetryDataSource.DEMO
        setDataSource(next)
    }

    fun updateCalendarAge(newAge: Int) {
        telemetryRepository.updateCalendarAge(newAge)
        if (dataSource.value == TelemetryDataSource.LIVE) {
            refresh()
        }
    }

    fun updateBirthDate(year: Int, month: Int, dayOfMonth: Int) {
        telemetryRepository.updateBirthDate(year, month, dayOfMonth)
        if (dataSource.value == TelemetryDataSource.LIVE) {
            refresh()
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        if (granted.isNotEmpty()) {
            setDataSource(TelemetryDataSource.LIVE)
        } else {
            checkPermissionsAndSync()
        }
    }

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
