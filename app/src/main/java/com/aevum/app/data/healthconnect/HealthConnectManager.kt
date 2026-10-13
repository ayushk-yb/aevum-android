package com.aevum.app.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.aevum.app.data.model.DataSourceFilterMode
import com.aevum.app.data.model.DiscoveredSource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

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

        // Known Fitbit & Google Health package identifiers
        val FITBIT_PACKAGES = setOf(
            "com.fitbit.FitbitMobile",
            "com.google.android.apps.fitness",
            "com.google.android.apps.healthdata"
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

    suspend fun getGrantedPermissions(): Set<String> {
        return try {
            val client = healthConnectClient ?: return emptySet()
            client.permissionController.getGrantedPermissions()
        } catch (_: Throwable) {
            emptySet()
        }
    }

    suspend fun hasAnyPermission(): Boolean {
        return getGrantedPermissions().isNotEmpty()
    }

    suspend fun hasAllPermissions(): Boolean {
        return try {
            val granted = getGrantedPermissions()
            val target = permissions.ifEmpty { PERMISSIONS }
            granted.containsAll(target)
        } catch (_: Throwable) {
            false
        }
    }

    private fun startOfLocalDay(daysAgo: Long = 0): Instant {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).minusDays(daysAgo).atStartOfDay(zone).toInstant()
    }

    /**
     * Checks if a Record originated from Fitbit or Google Health / Fitness apps.
     */
    fun isFitbitOrGoogleRecord(record: Record): Boolean {
        val pkg = record.metadata.dataOrigin.packageName.lowercase()
        return pkg.contains("fitbit") ||
                pkg.contains("google.android.apps.fitness") ||
                pkg.contains("health") ||
                FITBIT_PACKAGES.any { pkg.equals(it, ignoreCase = true) }
    }

    /**
     * Predicate to filter records based on user's selected source filter mode.
     */
    private fun <T : Record> filterBySource(
        records: List<T>,
        filterMode: DataSourceFilterMode,
        targetPackage: String? = null
    ): List<T> {
        if (records.isEmpty()) return emptyList()

        return when (filterMode) {
            DataSourceFilterMode.ALL -> records
            DataSourceFilterMode.FITBIT_ONLY -> {
                val fitbitRecords = records.filter { isFitbitOrGoogleRecord(it) }
                // If Fitbit-tagged records exist, isolate only those!
                // If wearable hasn't synced yet or was exported via generic connector, fallback gracefully
                if (fitbitRecords.isNotEmpty()) fitbitRecords else records
            }
            DataSourceFilterMode.CUSTOM -> {
                if (targetPackage.isNullOrBlank()) records
                else {
                    val matching = records.filter { it.metadata.dataOrigin.packageName.equals(targetPackage, ignoreCase = true) }
                    if (matching.isNotEmpty()) matching else records
                }
            }
        }
    }

    /**
     * Discovers all contributing applications currently writing data into Health Connect.
     */
    suspend fun discoverContributingSources(): List<DiscoveredSource> {
        return try {
            val client = healthConnectClient ?: return emptyList()
            val startTime = startOfLocalDay(7)
            val endTime = Instant.now()

            val stepsRes = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val hrRes = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val sleepRes = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val countsByPkg = mutableMapOf<String, Int>()
            for (rec in stepsRes.records) {
                val pkg = rec.metadata.dataOrigin.packageName
                countsByPkg[pkg] = (countsByPkg[pkg] ?: 0) + 1
            }
            for (rec in hrRes.records) {
                val pkg = rec.metadata.dataOrigin.packageName
                countsByPkg[pkg] = (countsByPkg[pkg] ?: 0) + 1
            }
            for (rec in sleepRes.records) {
                val pkg = rec.metadata.dataOrigin.packageName
                countsByPkg[pkg] = (countsByPkg[pkg] ?: 0) + 1
            }

            val pm = context.packageManager
            countsByPkg.map { (pkg, count) ->
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Throwable) {
                    when {
                        pkg.contains("fitbit", ignoreCase = true) -> "Fitbit"
                        pkg.contains("fitness", ignoreCase = true) -> "Google Fit / Health"
                        pkg.contains("samsung", ignoreCase = true) -> "Samsung Health"
                        pkg.contains("health", ignoreCase = true) -> "Health App"
                        else -> pkg.substringAfterLast('.')
                    }
                }
                val isFitbit = pkg.contains("fitbit", ignoreCase = true) ||
                        pkg.contains("google.android.apps.fitness", ignoreCase = true)
                DiscoveredSource(
                    packageName = pkg,
                    appName = appLabel,
                    isFitbitOrGoogle = isFitbit,
                    recordCount = count
                )
            }.sortedWith(compareByDescending<DiscoveredSource> { it.isFitbitOrGoogle }.thenByDescending { it.recordCount })
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun readTodayStepsRecords(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): List<StepsRecord> {
        return try {
            val client = healthConnectClient ?: return emptyList()
            val startTime = startOfLocalDay(0)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            filterBySource(response.records, filterMode, targetPackage)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun readTodaySteps(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): Long {
        val records = readTodayStepsRecords(filterMode, targetPackage)
        return records.sumOf { it.count }
    }

    suspend fun readStepsForLastDays(
        days: Int = 30,
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): Map<LocalDate, Long> {
        return try {
            val client = healthConnectClient ?: return emptyMap()
            val zone = ZoneId.systemDefault()
            val startTime = startOfLocalDay(days.toLong())
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filtered = filterBySource(response.records, filterMode, targetPackage)
            val dailyMap = mutableMapOf<LocalDate, Long>()
            for (rec in filtered) {
                val date = rec.startTime.atZone(zone).toLocalDate()
                dailyMap[date] = (dailyMap[date] ?: 0L) + rec.count
            }
            dailyMap
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    suspend fun readTodayActiveCalories(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): Long {
        return try {
            val client = healthConnectClient ?: return 0
            val startTime = startOfLocalDay(0)
            val endTime = Instant.now()
            val activeResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filteredActive = filterBySource(activeResponse.records, filterMode, targetPackage)
            val activeSum = filteredActive.sumOf { it.energy.inKilocalories.toLong() }
            if (activeSum > 0) return activeSum

            // Fallback to TotalCaloriesBurnedRecord if wearable only writes total calories
            val totalResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filteredTotal = filterBySource(totalResponse.records, filterMode, targetPackage)
            val totalSum = filteredTotal.sumOf { it.energy.inKilocalories.toLong() }
            // Estimate active portion (~30% of total above basal)
            if (totalSum > 0) (totalSum * 0.30).toLong() else 0L
        } catch (_: Throwable) {
            0
        }
    }

    suspend fun readTodayFloors(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): Double {
        return try {
            val client = healthConnectClient ?: return 0.0
            val startTime = startOfLocalDay(0)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = FloorsClimbedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filtered = filterBySource(response.records, filterMode, targetPackage)
            filtered.sumOf { it.floors }
        } catch (_: Throwable) {
            0.0
        }
    }

    suspend fun readRecentHeartRateSamples(
        days: Long = 7,
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): List<Long> {
        return try {
            val client = healthConnectClient ?: return emptyList()
            val startTime = Instant.now().minus(days, ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filtered = filterBySource(response.records, filterMode, targetPackage)
            filtered.flatMap { rec -> rec.samples.map { it.beatsPerMinute } }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun readRestingHeartRateHistory(
        days: Long = 14,
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): List<Long> {
        return try {
            val client = healthConnectClient ?: return emptyList()
            val startTime = Instant.now().minus(days, ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filtered = filterBySource(response.records, filterMode, targetPackage)
            val rhrList = filtered.map { it.beatsPerMinute }
            if (rhrList.isNotEmpty()) return rhrList

            // Fallback: Estimate RHR from lowest 15th percentile of HeartRateRecord samples
            val hrSamples = readRecentHeartRateSamples(7, filterMode, targetPackage)
            if (hrSamples.isNotEmpty()) {
                val sorted = hrSamples.sorted()
                val rhrEst = sorted[(sorted.size * 0.15).toInt().coerceIn(0, sorted.lastIndex)]
                listOf(rhrEst)
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun readRestingHeartRate(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): Long? {
        return readRestingHeartRateHistory(14, filterMode, targetPackage).lastOrNull()
    }

    suspend fun readHrvHistory(
        days: Long = 14,
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): List<Double> {
        return try {
            val client = healthConnectClient ?: return emptyList()
            val startTime = Instant.now().minus(days, ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filtered = filterBySource(response.records, filterMode, targetPackage)
            val hrvList = filtered.map { it.heartRateVariabilityMillis }
            if (hrvList.isNotEmpty()) return hrvList

            // Fallback: Estimate autonomic variability from heart rate sample variance if wearable doesn't export RMSSD directly
            val hrSamples = readRecentHeartRateSamples(3, filterMode, targetPackage)
            if (hrSamples.size >= 4) {
                val rrIntervals = hrSamples.map { 60000.0 / it.toDouble().coerceAtLeast(35.0) }
                var sumDiffSq = 0.0
                for (i in 0 until rrIntervals.size - 1) {
                    val diff = rrIntervals[i + 1] - rrIntervals[i]
                    sumDiffSq += diff * diff
                }
                val rmssd = sqrt(sumDiffSq / (rrIntervals.size - 1)).coerceIn(18.0, 120.0)
                listOf(rmssd)
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun readLatestHrv(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): Double? {
        return readHrvHistory(14, filterMode, targetPackage).lastOrNull()
    }

    suspend fun readLastSleepSession(
        filterMode: DataSourceFilterMode = DataSourceFilterMode.ALL,
        targetPackage: String? = null
    ): SleepSessionRecord? {
        return try {
            val client = healthConnectClient ?: return null
            // Look back up to 7 days so the user sees their latest recorded sleep session
            val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val filtered = filterBySource(response.records, filterMode, targetPackage)
            filtered.maxByOrNull { it.endTime }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Compute cardiovascular strain score (scale 0.0 - 21.0)
     * Derived from active calories, steps, and HR zone load.
     */
    fun computeDailyStrain(activeKcal: Int, avgBpm: Int, steps: Int = 0): Float {
        val effectiveKcal = max(activeKcal, (steps * 0.04).toInt())
        if (effectiveKcal <= 0 && steps <= 0) return 0.0f
        val rawStrain = (log10(effectiveKcal.toDouble() + 10.0) * 4.2) + (max(0, avgBpm - 55) * 0.04)
        return (Math.round(rawStrain * 10.0) / 10.0).toFloat().coerceIn(0.0f, 21.0f)
    }
}
