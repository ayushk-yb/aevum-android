package com.aevum.app.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.health.connect.client.records.SleepSessionRecord
import com.aevum.app.data.healthconnect.HealthConnectManager
import com.aevum.app.data.healthconnect.MockTelemetryProvider
import com.aevum.app.data.model.BiometricBadge
import com.aevum.app.data.model.DataSourceFilterMode
import com.aevum.app.data.model.DayHeatmapCell
import com.aevum.app.data.model.DiscoveredSource
import com.aevum.app.data.model.ElevationPoint
import com.aevum.app.data.model.HourlySteps
import com.aevum.app.data.model.PulseAgeTelemetry
import com.aevum.app.data.model.RecoveryOverview
import com.aevum.app.data.model.SegmentType
import com.aevum.app.data.model.SleepHypnogramSegment
import com.aevum.app.data.model.SleepStage
import com.aevum.app.data.model.SleepTelemetry
import com.aevum.app.data.model.StepsTelemetry
import com.aevum.app.data.model.StreaksTelemetry
import com.aevum.app.data.model.TelemetryDataSource
import com.aevum.app.data.model.TimelineSegment
import com.aevum.app.data.model.VitalMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

interface TelemetryRepository {
    fun getRecoveryOverview(): Flow<RecoveryOverview>
    fun getStepsTelemetry(): Flow<StepsTelemetry>
    fun getSleepTelemetry(): Flow<SleepTelemetry>
    fun getPulseAgeTelemetry(): Flow<PulseAgeTelemetry>
    fun getStreaksTelemetry(): Flow<StreaksTelemetry>
    fun getDataSource(): Flow<TelemetryDataSource>
    fun getFilterMode(): Flow<DataSourceFilterMode>
    fun getSelectedSourcePackage(): Flow<String?>
    fun getDiscoveredSources(): Flow<List<DiscoveredSource>>
    fun setDataSource(source: TelemetryDataSource)
    fun setFilterMode(mode: DataSourceFilterMode, targetPackage: String? = null)
    fun updateCalendarAge(newAge: Int)
    fun updateBirthDate(year: Int, month: Int, dayOfMonth: Int)
    suspend fun refreshTelemetry()
}

class DefaultTelemetryRepository(
    private val healthConnectManager: HealthConnectManager? = null,
    context: Context? = null
) : TelemetryRepository {

    companion object {
        @Volatile
        private var instance: DefaultTelemetryRepository? = null

        fun getInstance(
            healthConnectManager: HealthConnectManager?,
            context: Context?
        ): DefaultTelemetryRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultTelemetryRepository(healthConnectManager, context?.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val sharedPrefs = context?.getSharedPreferences("aevum_settings", Context.MODE_PRIVATE)

    private val savedSource = sharedPrefs?.getString("data_source", null)
    private val initialSource = if (savedSource == "LIVE") TelemetryDataSource.LIVE else TelemetryDataSource.DEMO

    private val savedFilterMode = sharedPrefs?.getString("filter_mode", DataSourceFilterMode.FITBIT_ONLY.name)
        ?.let { try { DataSourceFilterMode.valueOf(it) } catch (_: Exception) { DataSourceFilterMode.FITBIT_ONLY } }
        ?: DataSourceFilterMode.FITBIT_ONLY

    private val savedTargetPackage = sharedPrefs?.getString("target_package", null)

    private val dataSourceState = MutableStateFlow(initialSource)
    private val filterModeState = MutableStateFlow(savedFilterMode)
    private val selectedSourcePackageState = MutableStateFlow<String?>(savedTargetPackage)
    private val discoveredSourcesState = MutableStateFlow<List<DiscoveredSource>>(emptyList())

    private val savedBirthDateIso = sharedPrefs?.getString("birth_date", null)

    private fun computeCurrentAge(): Int {
        val iso = sharedPrefs?.getString("birth_date", null)
        if (!iso.isNullOrBlank()) {
            try {
                val bDate = LocalDate.parse(iso)
                val now = LocalDate.now(ZoneId.systemDefault())
                val years = java.time.Period.between(bDate, now).years
                if (years in 1..120) return years
            } catch (_: Exception) {}
        }
        return sharedPrefs?.getInt("calendar_age", 34) ?: 34
    }

    private val savedAge = computeCurrentAge()

    private val recoveryState = MutableStateFlow(MockTelemetryProvider.getRecoveryOverview())
    private val stepsState = MutableStateFlow(MockTelemetryProvider.getStepsTelemetry())
    private val sleepState = MutableStateFlow(MockTelemetryProvider.getSleepTelemetry())
    private val pulseAgeState = MutableStateFlow(
        MockTelemetryProvider.getPulseAgeTelemetry().let {
            val delta = Math.round((it.pulseAge - savedAge) * 10f) / 10f
            it.copy(
                calendarAge = savedAge,
                birthDateIso = savedBirthDateIso,
                deltaYears = delta
            )
        }
    )
    private val streaksState = MutableStateFlow(MockTelemetryProvider.getStreaksTelemetry())

    override fun getRecoveryOverview(): Flow<RecoveryOverview> = recoveryState.asStateFlow()
    override fun getStepsTelemetry(): Flow<StepsTelemetry> = stepsState.asStateFlow()
    override fun getSleepTelemetry(): Flow<SleepTelemetry> = sleepState.asStateFlow()
    override fun getPulseAgeTelemetry(): Flow<PulseAgeTelemetry> = pulseAgeState.asStateFlow()
    override fun getStreaksTelemetry(): Flow<StreaksTelemetry> = streaksState.asStateFlow()
    override fun getDataSource(): Flow<TelemetryDataSource> = dataSourceState.asStateFlow()
    override fun getFilterMode(): Flow<DataSourceFilterMode> = filterModeState.asStateFlow()
    override fun getSelectedSourcePackage(): Flow<String?> = selectedSourcePackageState.asStateFlow()
    override fun getDiscoveredSources(): Flow<List<DiscoveredSource>> = discoveredSourcesState.asStateFlow()

    override fun setFilterMode(mode: DataSourceFilterMode, targetPackage: String?) {
        filterModeState.value = mode
        selectedSourcePackageState.value = targetPackage
        sharedPrefs?.edit()
            ?.putString("filter_mode", mode.name)
            ?.putString("target_package", targetPackage)
            ?.apply()
    }

    override fun updateCalendarAge(newAge: Int) {
        sharedPrefs?.edit()
            ?.putInt("calendar_age", newAge)
            ?.remove("birth_date")
            ?.apply()
        val current = pulseAgeState.value
        val delta = Math.round((current.pulseAge - newAge) * 10f) / 10f
        pulseAgeState.value = current.copy(
            calendarAge = newAge,
            birthDateIso = null,
            deltaYears = delta,
            minScaleAge = (newAge - 15).coerceAtLeast(15).toFloat(),
            maxScaleAge = (newAge + 15).toFloat()
        )
    }

    override fun updateBirthDate(year: Int, month: Int, dayOfMonth: Int) {
        val bDate = LocalDate.of(year, month, dayOfMonth)
        val iso = bDate.toString()
        val now = LocalDate.now(ZoneId.systemDefault())
        val calculatedAge = java.time.Period.between(bDate, now).years.coerceIn(10, 115)

        sharedPrefs?.edit()
            ?.putString("birth_date", iso)
            ?.putInt("calendar_age", calculatedAge)
            ?.apply()

        val current = pulseAgeState.value
        val delta = Math.round((current.pulseAge - calculatedAge) * 10f) / 10f
        pulseAgeState.value = current.copy(
            calendarAge = calculatedAge,
            birthDateIso = iso,
            deltaYears = delta,
            minScaleAge = (calculatedAge - 15).coerceAtLeast(15).toFloat(),
            maxScaleAge = (calculatedAge + 15).toFloat()
        )
    }

    override fun setDataSource(source: TelemetryDataSource) {
        dataSourceState.value = source
        sharedPrefs?.edit()?.putString("data_source", source.name)?.apply()
        if (source == TelemetryDataSource.DEMO) {
            recoveryState.value = MockTelemetryProvider.getRecoveryOverview()
            stepsState.value = MockTelemetryProvider.getStepsTelemetry()
            sleepState.value = MockTelemetryProvider.getSleepTelemetry()
            pulseAgeState.value = MockTelemetryProvider.getPulseAgeTelemetry().let {
                val age = pulseAgeState.value.calendarAge
                val delta = Math.round((it.pulseAge - age) * 10f) / 10f
                it.copy(calendarAge = age, deltaYears = delta)
            }
            streaksState.value = MockTelemetryProvider.getStreaksTelemetry()
        }
    }

    override suspend fun refreshTelemetry() {
        if (dataSourceState.value == TelemetryDataSource.DEMO) return
        val manager = healthConnectManager ?: return

        val zone = ZoneId.systemDefault()
        val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

        val currentFilter = filterModeState.value
        val currentTargetPkg = selectedSourcePackageState.value

        // Discover contributing sources
        val sources = manager.discoverContributingSources()
        discoveredSourcesState.value = sources

        // 1. Fetch Live Records from Health Connect with Source Filtering
        val todayStepRecords = manager.readTodayStepsRecords(currentFilter, currentTargetPkg)
        val liveSteps = todayStepRecords.sumOf { it.count }.toInt()
        val last30DaysSteps = manager.readStepsForLastDays(30, currentFilter, currentTargetPkg)
        val rhrHistory = manager.readRestingHeartRateHistory(14, currentFilter, currentTargetPkg)
        val liveRhr = rhrHistory.lastOrNull()
        val hrvHistory = manager.readHrvHistory(14, currentFilter, currentTargetPkg)
        val liveHrv = hrvHistory.lastOrNull()
        val liveActiveKcal = manager.readTodayActiveCalories(currentFilter, currentTargetPkg).toInt()
        val liveFloors = manager.readTodayFloors(currentFilter, currentTargetPkg).toInt()
        val liveSleep = manager.readLastSleepSession(currentFilter, currentTargetPkg)

        // 2. Build Live Hourly Steps Histogram & Elevation
        val hourlyBuckets = IntArray(24)
        for (rec in todayStepRecords) {
            val hour = rec.startTime.atZone(zone).hour.coerceIn(0, 23)
            hourlyBuckets[hour] += rec.count.toInt()
        }
        val maxHourSteps = hourlyBuckets.maxOrNull() ?: 0
        val peakHour = if (maxHourSteps > 0) hourlyBuckets.indexOfFirst { it == maxHourSteps } else -1

        // Historical daily average steps (usual)
        val historicalAvgDaily = if (last30DaysSteps.size > 1) {
            last30DaysSteps.values.average().toInt().coerceAtLeast(1000)
        } else {
            6000
        }
        val usualPerActiveHour = (historicalAvgDaily / 14).coerceAtLeast(100)

        val currentHour = java.time.LocalTime.now(zone).hour
        val liveHistogram = (0..max(currentHour, 12)).map { hr ->
            HourlySteps(
                hour = hr,
                steps = hourlyBuckets[hr],
                usualSteps = if (hr in 7..20) usualPerActiveHour else 0,
                isPeak = (hr == peakHour && maxHourSteps > 0)
            )
        }

        val targetSteps = 10000
        val km = (liveSteps * 0.00075f)
        val progressPercent = ((liveSteps.toFloat() / targetSteps) * 100).toInt().coerceIn(0, 100)
        val deviationVsUsual = liveSteps - historicalAvgDaily
        val toSummit = (targetSteps - liveSteps).coerceAtLeast(0)
        val effectiveKcal = if (liveActiveKcal > 0) liveActiveKcal else (liveSteps * 0.04f).roundToInt()

        val normProgress = (liveSteps.toFloat() / targetSteps).coerceIn(0.02f, 0.98f)
        stepsState.value = StepsTelemetry(
            currentSteps = liveSteps,
            targetSteps = targetSteps,
            distanceKm = (Math.round(km * 10.0f) / 10.0f),
            progressPercent = progressPercent,
            deviationVsUsual = deviationVsUsual,
            toSummitSteps = toSummit,
            floorsClimbed = liveFloors,
            activeKcal = effectiveKcal,
            summitElevationPath = listOf(
                ElevationPoint(0.00f, 0.05f),
                ElevationPoint((normProgress * 0.5f).coerceAtLeast(0.01f), (normProgress * 0.5f).coerceAtLeast(0.05f)),
                ElevationPoint(normProgress, normProgress.coerceIn(0.08f, 0.95f), "now"),
                ElevationPoint(1.00f, 0.98f, "10,000")
            ),
            hourlyHistogram = liveHistogram
        )

        // 3. Build Live Sleep Telemetry
        val targetMinutes = 470 // 7h 50m biological need
        val updatedSleep: SleepTelemetry = if (liveSleep != null) {
            val duration = Duration.between(liveSleep.startTime, liveSleep.endTime)
            val totalMinutes = duration.toMinutes().toInt().coerceAtLeast(1)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val bedTimeStr = liveSleep.startTime.atZone(zone).format(timeFmt)
            val wakeTimeStr = liveSleep.endTime.atZone(zone).format(timeFmt)

            var deepMins = 0
            var lightMins = 0
            var remMins = 0
            var awakeMins = 0
            val hypnogramSegments = mutableListOf<SleepHypnogramSegment>()

            if (liveSleep.stages.isNotEmpty()) {
                val sessionStartMs = liveSleep.startTime.toEpochMilli()
                val sessionTotalMs = (liveSleep.endTime.toEpochMilli() - sessionStartMs).coerceAtLeast(1L).toFloat()

                for (stageRec in liveSleep.stages) {
                    val stageMins = Duration.between(stageRec.startTime, stageRec.endTime).toMinutes().toInt().coerceAtLeast(1)
                    val mappedStage = when (stageRec.stage) {
                        SleepSessionRecord.STAGE_TYPE_DEEP -> {
                            deepMins += stageMins
                            SleepStage.DEEP
                        }
                        SleepSessionRecord.STAGE_TYPE_REM -> {
                            remMins += stageMins
                            SleepStage.REM
                        }
                        SleepSessionRecord.STAGE_TYPE_AWAKE,
                        SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED,
                        SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> {
                            awakeMins += stageMins
                            SleepStage.AWAKE
                        }
                        else -> {
                            lightMins += stageMins
                            SleepStage.LIGHT
                        }
                    }
                    val startFrac = ((stageRec.startTime.toEpochMilli() - sessionStartMs) / sessionTotalMs).coerceIn(0f, 1f)
                    val endFrac = ((stageRec.endTime.toEpochMilli() - sessionStartMs) / sessionTotalMs).coerceIn(startFrac, 1f)
                    hypnogramSegments.add(
                        SleepHypnogramSegment(
                            stage = mappedStage,
                            startFraction = startFrac,
                            durationFraction = (endFrac - startFrac).coerceAtLeast(0.02f)
                        )
                    )
                }
            } else {
                // Estimate stage distribution from total sleep duration when wearable only writes session start/end
                deepMins = (totalMinutes * 0.20f).roundToInt()
                remMins = (totalMinutes * 0.22f).roundToInt()
                awakeMins = (totalMinutes * 0.06f).roundToInt()
                lightMins = (totalMinutes - deepMins - remMins - awakeMins).coerceAtLeast(0)
                hypnogramSegments.addAll(
                    listOf(
                        SleepHypnogramSegment(SleepStage.LIGHT, 0.00f, 0.15f),
                        SleepHypnogramSegment(SleepStage.DEEP, 0.15f, 0.20f),
                        SleepHypnogramSegment(SleepStage.LIGHT, 0.35f, 0.25f),
                        SleepHypnogramSegment(SleepStage.REM, 0.60f, 0.22f),
                        SleepHypnogramSegment(SleepStage.LIGHT, 0.82f, 0.12f),
                        SleepHypnogramSegment(SleepStage.AWAKE, 0.94f, 0.06f)
                    )
                )
            }

            fun fmtHrsMins(m: Int): String = if (m >= 60) "${m / 60}:${String.format(Locale.US, "%02d", m % 60)}" else "${m}m"
            val effPct = ((totalMinutes.toFloat() / targetMinutes) * 100).roundToInt().coerceIn(10, 100)

            SleepTelemetry(
                durationFormatted = "${hours}h ${minutes}m",
                efficiencyPercent = effPct,
                bedTime = bedTimeStr,
                wakeTime = wakeTimeStr,
                deepDurationFormatted = fmtHrsMins(deepMins),
                lightDurationFormatted = fmtHrsMins(lightMins),
                remDurationFormatted = fmtHrsMins(remMins),
                awakeDurationFormatted = "${awakeMins}m",
                gotMinutes = totalMinutes,
                neededMinutes = targetMinutes,
                deficitMinutes = (targetMinutes - totalMinutes).coerceAtLeast(0),
                stages = hypnogramSegments
            )
        } else {
            SleepTelemetry(
                durationFormatted = "0h 0m",
                efficiencyPercent = 0,
                bedTime = "--:--",
                wakeTime = "--:--",
                deepDurationFormatted = "0m",
                lightDurationFormatted = "0m",
                remDurationFormatted = "0m",
                awakeDurationFormatted = "0m",
                gotMinutes = 0,
                neededMinutes = targetMinutes,
                deficitMinutes = targetMinutes,
                stages = emptyList()
            )
        }
        sleepState.value = updatedSleep

        // 4. Build Live Recovery Overview & Vitals
        val computedStrain = manager.computeDailyStrain(
            activeKcal = effectiveKcal,
            avgBpm = (liveRhr ?: 60L).toInt(),
            steps = liveSteps
        )

        val hrvValStr = liveHrv?.roundToInt()?.toString() ?: "--"
        val hrvAvg = if (hrvHistory.size > 1) hrvHistory.average() else (liveHrv ?: 50.0)
        val hrvDeltaInt = if (liveHrv != null) (liveHrv - hrvAvg).roundToInt() else 0
        val hrvDeltaStr = if (liveHrv != null) (if (hrvDeltaInt >= 0) "+$hrvDeltaInt" else "$hrvDeltaInt") else "Live"
        val hrvWave = if (hrvHistory.size >= 2) hrvHistory.map { it.toFloat() } else listOfNotNull(liveHrv?.toFloat(), liveHrv?.toFloat()).ifEmpty { listOf(40f, 40f) }

        val rhrValStr = liveRhr?.toString() ?: "--"
        val rhrAvg = if (rhrHistory.size > 1) rhrHistory.average() else (liveRhr?.toDouble() ?: 60.0)
        val rhrDeltaInt = if (liveRhr != null) (liveRhr - rhrAvg.roundToInt()).toInt() else 0
        val rhrDeltaStr = if (liveRhr != null) (if (rhrDeltaInt >= 0) "+$rhrDeltaInt" else "$rhrDeltaInt") else "Live"
        val rhrWave = if (rhrHistory.size >= 2) rhrHistory.map { it.toFloat() } else listOfNotNull(liveRhr?.toFloat(), liveRhr?.toFloat()).ifEmpty { listOf(60f, 60f) }

        // Dynamic Recovery Score based on sleep score, HRV, and RHR
        val sleepComponent = if (updatedSleep.efficiencyPercent > 0) updatedSleep.efficiencyPercent else 65
        val hrvComponent = if (liveHrv != null) ((liveHrv / 70.0) * 100.0).roundToInt().coerceIn(30, 100) else 70
        val rhrComponent = if (liveRhr != null) (100 - (liveRhr.toInt() - 48).coerceAtLeast(0)).coerceIn(30, 100) else 70
        val liveRecoveryScore = if (liveSleep == null && liveHrv == null && liveRhr == null && liveSteps == 0) {
            0
        } else {
            ((sleepComponent * 0.45f) + (hrvComponent * 0.30f) + (rhrComponent * 0.25f)).roundToInt().coerceIn(1, 100)
        }

        val liveTimeline = mutableListOf<TimelineSegment>()
        if (liveSleep != null) {
            val endHr = liveSleep.endTime.atZone(zone).hour + (liveSleep.endTime.atZone(zone).minute / 60f)
            liveTimeline.add(
                TimelineSegment(
                    title = "Sleep",
                    type = SegmentType.SLEEP,
                    startHourFraction = 0.0f,
                    endHourFraction = endHr.coerceIn(1f, 12f),
                    durationFormatted = "Sleep ${updatedSleep.durationFormatted}"
                )
            )
        }
        if (liveSteps > 0) {
            val walkMins = (liveSteps / 100).coerceAtLeast(1)
            val pkHr = if (peakHour >= 0) peakHour.toFloat() else 9.0f
            liveTimeline.add(
                TimelineSegment(
                    title = "Walk",
                    type = SegmentType.WALK,
                    startHourFraction = pkHr.coerceIn(0f, 22f),
                    endHourFraction = (pkHr + (walkMins / 60f).coerceAtLeast(0.4f)).coerceIn(0.5f, 23.8f),
                    durationFormatted = "Active $walkMins min ($liveSteps steps)"
                )
            )
        }

        val sourceFilterLabel = when (currentFilter) {
            DataSourceFilterMode.FITBIT_ONLY -> "Fitbit Wearable"
            DataSourceFilterMode.CUSTOM -> "Custom Source"
            DataSourceFilterMode.ALL -> "Merged Sources"
        }

        val coachingText = when {
            liveSteps == 0 && liveSleep == null && liveRhr == null ->
                "Connected to Health Connect ($sourceFilterLabel). Waiting for sync..."
            liveRecoveryScore >= 67 ->
                "Well recovered ($liveRecoveryScore%). Optimal window to push strain to 11–14."
            liveRecoveryScore >= 34 ->
                "Moderate recovery ($liveRecoveryScore%). Maintain balanced strain (8–11)."
            else ->
                "Restorative day ($liveRecoveryScore%). Prioritize sleep & active recovery."
        }

        recoveryState.value = RecoveryOverview(
            recoveryScore = liveRecoveryScore,
            sleepScore = updatedSleep.efficiencyPercent,
            strainScore = computedStrain,
            coachingMessage = coachingText,
            timelineSegments = liveTimeline,
            hrvMetric = VitalMetric(
                title = "HRV",
                value = hrvValStr,
                unit = "ms",
                delta = hrvDeltaStr,
                isPositive = hrvDeltaInt >= 0,
                waveformPoints = hrvWave
            ),
            restingHrMetric = VitalMetric(
                title = "Resting HR",
                value = rhrValStr,
                unit = "bpm",
                delta = rhrDeltaStr,
                isPositive = rhrDeltaInt <= 0,
                waveformPoints = rhrWave
            )
        )

        // 5. Build Live Pulse Age & Longevity
        val calAge = pulseAgeState.value.calendarAge
        val rhrAgeOffset = if (liveRhr != null) ((liveRhr - 62) * 0.25f) else 0f
        val hrvAgeOffset = if (liveHrv != null) ((52.0 - liveHrv) * 0.08).toFloat() else 0f
        val stepAgeOffset = when {
            liveSteps >= 8000 || historicalAvgDaily >= 8000 -> -1.4f
            liveSteps >= 5000 || historicalAvgDaily >= 5000 -> -0.6f
            else -> 0.2f
        }
        val totalAgeDelta = (rhrAgeOffset + hrvAgeOffset + stepAgeOffset).coerceIn(-6.5f, 6.5f)
        val roundedDelta = Math.round(totalAgeDelta * 10f) / 10f
        val computedPulseAge = Math.round((calAge + roundedDelta) * 10f) / 10f
        val computedPace = Math.round((1.0f + (roundedDelta / 25.0f)).coerceIn(0.65f, 1.35f) * 100f) / 100f
        val paceStatus = if (computedPace <= 1.0f) "$computedPace yrs / yr (Optimal)" else "$computedPace yrs / yr (Elevated)"

        pulseAgeState.value = PulseAgeTelemetry(
            calendarAge = calAge,
            pulseAge = computedPulseAge,
            deltaYears = roundedDelta,
            paceOfAging = computedPace,
            paceLabel = paceStatus,
            minScaleAge = (calAge - 15).coerceAtLeast(15).toFloat(),
            maxScaleAge = (calAge + 15).toFloat()
        )

        // 6. Build Live Streaks & Heatmap from Last 30 Days Steps
        val today = LocalDate.now(zone)
        var currentStreak = 0
        var d = today
        // Count consecutive active days (today or starting yesterday if today just started)
        if ((last30DaysSteps[today] ?: 0L) > 0L) {
            while ((last30DaysSteps[d] ?: 0L) > 0L) {
                currentStreak++
                d = d.minusDays(1)
            }
        } else {
            d = today.minusDays(1)
            while ((last30DaysSteps[d] ?: 0L) > 0L) {
                currentStreak++
                d = d.minusDays(1)
            }
        }

        val totalLogged = last30DaysSteps.count { it.value > 0L }
        var bestStreak = currentStreak
        var tempStreak = 0
        for (i in 0..30) {
            val day = today.minusDays(i.toLong())
            if ((last30DaysSteps[day] ?: 0L) > 0L) {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else {
                tempStreak = 0
            }
        }

        val recent10Days = (9 downTo 0).map { offset ->
            (last30DaysSteps[today.minusDays(offset.toLong())] ?: 0L) > 0L
        }
        val compliance = ((totalLogged / 30.0f) * 100).roundToInt().coerceIn(0, 100)

        val liveHeatmap = mutableListOf<DayHeatmapCell>()
        for (month in 0..3) {
            for (day in 0..6) {
                for (week in 0..3) {
                    val daysBack = ((3 - month) * 28) + ((3 - week) * 7) + (6 - day)
                    val targetDate = today.minusDays(daysBack.toLong())
                    val stepsOnDay = last30DaysSteps[targetDate] ?: 0L
                    val intensity = when {
                        stepsOnDay >= 8000 -> 1.0f
                        stepsOnDay >= 5000 -> 0.7f
                        stepsOnDay >= 1500 -> 0.4f
                        stepsOnDay > 0 -> 0.25f
                        else -> 0.0f
                    }
                    liveHeatmap.add(DayHeatmapCell(month, day, intensity))
                }
            }
        }

        val liveBadges = listOf(
            BiometricBadge("recovery_master", "Recovery Master", "Recovery", liveRecoveryScore >= 70, Color(0xFF00E676)),
            BiometricBadge("summit_10k", "Summit 10K", "Steps", liveSteps >= 10000 || last30DaysSteps.values.any { it >= 10000 }, Color(0xFF388AF6)),
            BiometricBadge("sleep_architect", "Sleep Architect", "Sleep", updatedSleep.gotMinutes >= 420, Color(0xFF9D4EDD)),
            BiometricBadge("hrv_flow", "HRV Flow", "Heart", (liveHrv ?: 0.0) >= 45.0, Color(0xFF12E6C8)),
            BiometricBadge("longevity_prime", "Longevity Prime", "Longevity", roundedDelta < 0f, Color(0xFFFFB703)),
            BiometricBadge("streak_titan", "Streak Titan", "Consistency", bestStreak >= 7, Color(0xFF6B7280))
        )

        streaksState.value = StreaksTelemetry(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalDaysLogged = totalLogged,
            complianceRatePercent = compliance,
            recentDayMilestones = recent10Days,
            heatmapCells = liveHeatmap,
            badges = liveBadges
        )
    }
}
