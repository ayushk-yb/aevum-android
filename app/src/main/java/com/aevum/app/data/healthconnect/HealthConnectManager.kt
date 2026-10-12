package com.aevum.app.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.log10
import kotlin.math.max

class HealthConnectManager(private val context: Context) {

    companion object {
        val PERMISSIONS = setOf(
            "android.permission.health.READ_STEPS",
            "android.permission.health.READ_HEART_RATE",
            "android.permission.health.READ_RESTING_HEART_RATE",
            "android.permission.health.READ_HEART_RATE_VARIABILITY",
            "android.permission.health.READ_SLEEP",
            "android.permission.health.READ_TOTAL_CALORIES_BURNED",
            "android.permission.health.READ_ACTIVE_CALORIES_BURNED",
            "android.permission.health.READ_FLOORS_CLIMBED"
        )
    }

    val healthConnectClient by lazy {
        try {
            if (isAvailable()) HealthConnectClient.getOrCreate(context) else null
        } catch (_: Throwable) {
            null
        }
    }

    val permissions: Set<String> by lazy {
        try {
            val resolved = setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class),
                HealthPermission.getReadPermission(RestingHeartRateRecord::class),
                HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
                HealthPermission.getReadPermission(SleepSessionRecord::class),
                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(FloorsClimbedRecord::class)
            )
            if (resolved.isNotEmpty()) resolved else PERMISSIONS
        } catch (_: Throwable) {
            PERMISSIONS
        }
    }

    fun isAvailable(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            status == HealthConnectClient.SDK_AVAILABLE
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        return try {
            val client = healthConnectClient ?: return false
            val granted = client.permissionController.getGrantedPermissions()
            val target = permissions.ifEmpty { PERMISSIONS }
            granted.containsAll(target)
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun readTodaySteps(): Long {
        return try {
            val client = healthConnectClient ?: return 0
            val startTime = Instant.now().truncatedTo(ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.count }
        } catch (_: Throwable) {
            0
        }
    }

    suspend fun readTodayActiveCalories(): Long {
        return try {
            val client = healthConnectClient ?: return 0
            val startTime = Instant.now().truncatedTo(ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.energy.inKilocalories.toLong() }
        } catch (_: Throwable) {
            0
        }
    }

    suspend fun readTodayFloors(): Double {
        return try {
            val client = healthConnectClient ?: return 0.0
            val startTime = Instant.now().truncatedTo(ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = FloorsClimbedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.floors }
        } catch (_: Throwable) {
            0.0
        }
    }

    suspend fun readRestingHeartRate(): Long? {
        return try {
            val client = healthConnectClient ?: return null
            val startTime = Instant.now().minus(24, ChronoUnit.HOURS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.lastOrNull()?.beatsPerMinute
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun readLatestHrv(): Double? {
        return try {
            val client = healthConnectClient ?: return null
            val startTime = Instant.now().minus(24, ChronoUnit.HOURS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.lastOrNull()?.heartRateVariabilityMillis
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun readLastSleepSession(): SleepSessionRecord? {
        return try {
            val client = healthConnectClient ?: return null
            val startTime = Instant.now().minus(36, ChronoUnit.HOURS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.lastOrNull()
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Compute cardiovascular strain score (scale 0.0 - 21.0)
     * Derived from active calories, cardiovascular exertion, and HR zone load.
     */
    fun computeDailyStrain(activeKcal: Int, avgBpm: Int): Float {
        if (activeKcal <= 0) return 0.0f
        val rawStrain = (log10(activeKcal.toDouble() + 10.0) * 4.8) + (max(0, avgBpm - 50) * 0.04)
        return (rawStrain.toFloat()).coerceIn(0.0f, 21.0f)
    }
}
