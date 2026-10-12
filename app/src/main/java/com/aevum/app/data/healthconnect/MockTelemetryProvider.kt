package com.aevum.app.data.healthconnect

import androidx.compose.ui.graphics.Color
import com.aevum.app.data.model.BiometricBadge
import com.aevum.app.data.model.DayHeatmapCell
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
import com.aevum.app.data.model.TimelineSegment
import com.aevum.app.data.model.VitalMetric
import kotlin.random.Random

object MockTelemetryProvider {

    fun getRecoveryOverview(): RecoveryOverview {
        return RecoveryOverview(
            recoveryScore = 72,
            sleepScore = 88,
            strainScore = 6.4f,
            coachingMessage = "Well recovered. Push to a strain of 11–14.",
            timelineSegments = listOf(
                TimelineSegment(
                    title = "Sleep",
                    type = SegmentType.SLEEP,
                    startHourFraction = 0.0f,
                    endHourFraction = 7.2f,
                    durationFormatted = "Sleep 7:12"
                ),
                TimelineSegment(
                    title = "Walk",
                    type = SegmentType.WALK,
                    startHourFraction = 7.5f,
                    endHourFraction = 8.16f,
                    durationFormatted = "Walk 40 min"
                ),
                TimelineSegment(
                    title = "Stress",
                    type = SegmentType.STRESS,
                    startHourFraction = 11.25f,
                    endHourFraction = 11.83f,
                    durationFormatted = "Stress 35 min"
                )
            ),
            hrvMetric = VitalMetric(
                title = "HRV",
                value = "54",
                unit = "ms",
                delta = "+11",
                isPositive = true,
                waveformPoints = listOf(
                    38f, 42f, 40f, 45f, 48f, 44f, 52f, 49f, 55f, 54f, 58f, 54f
                )
            ),
            restingHrMetric = VitalMetric(
                title = "Resting HR",
                value = "57",
                unit = "bpm",
                delta = "-3",
                isPositive = true, // Lower resting heart rate is positive in health telemetry
                waveformPoints = listOf(
                    62f, 60f, 61f, 59f, 58f, 60f, 58f, 56f, 57f, 55f, 57f
                )
            )
        )
    }

    fun getStepsTelemetry(): StepsTelemetry {
        val histogram = listOf(
            HourlySteps(0, 0, 10),
            HourlySteps(1, 0, 0),
            HourlySteps(2, 0, 0),
            HourlySteps(3, 0, 0),
            HourlySteps(4, 0, 0),
            HourlySteps(5, 40, 20),
            HourlySteps(6, 420, 310),
            HourlySteps(7, 2180, 1200, isPeak = true),
            HourlySteps(8, 920, 850),
            HourlySteps(9, 340, 520),
            HourlySteps(10, 680, 710),
            HourlySteps(11, 460, 640),
            HourlySteps(12, 200, 500)
        )

        val elevationPath = listOf(
            ElevationPoint(0.00f, 0.05f),
            ElevationPoint(0.12f, 0.12f),
            ElevationPoint(0.25f, 0.22f),
            ElevationPoint(0.38f, 0.35f),
            ElevationPoint(0.52f, 0.58f, "now"),
            ElevationPoint(0.68f, 0.74f),
            ElevationPoint(0.85f, 0.88f),
            ElevationPoint(1.00f, 0.98f, "10,000")
        )

        return StepsTelemetry(
            currentSteps = 5240,
            targetSteps = 10000,
            distanceKm = 3.9f,
            progressPercent = 52,
            deviationVsUsual = -2860,
            toSummitSteps = 4760,
            floorsClimbed = 3,
            activeKcal = 212,
            summitElevationPath = elevationPath,
            hourlyHistogram = histogram
        )
    }

    fun getSleepTelemetry(): SleepTelemetry {
        val stages = listOf(
            SleepHypnogramSegment(SleepStage.LIGHT, 0.00f, 0.08f),
            SleepHypnogramSegment(SleepStage.DEEP, 0.08f, 0.22f),
            SleepHypnogramSegment(SleepStage.LIGHT, 0.22f, 0.30f),
            SleepHypnogramSegment(SleepStage.REM, 0.30f, 0.42f),
            SleepHypnogramSegment(SleepStage.AWAKE, 0.42f, 0.45f),
            SleepHypnogramSegment(SleepStage.DEEP, 0.45f, 0.58f),
            SleepHypnogramSegment(SleepStage.LIGHT, 0.58f, 0.70f),
            SleepHypnogramSegment(SleepStage.REM, 0.70f, 0.86f),
            SleepHypnogramSegment(SleepStage.LIGHT, 0.86f, 0.96f),
            SleepHypnogramSegment(SleepStage.AWAKE, 0.96f, 1.00f)
        )

        return SleepTelemetry(
            durationFormatted = "7h 12m",
            efficiencyPercent = 88,
            bedTime = "11:32 PM",
            wakeTime = "6:44 AM",
            deepDurationFormatted = "1:36",
            lightDurationFormatted = "3:00",
            remDurationFormatted = "1:54",
            awakeDurationFormatted = "42m",
            gotMinutes = 432,
            neededMinutes = 470,
            deficitMinutes = 38,
            stages = stages
        )
    }

    fun getPulseAgeTelemetry(): PulseAgeTelemetry {
        return PulseAgeTelemetry(
            calendarAge = 34,
            pulseAge = 31.6f,
            deltaYears = -2.4f,
            paceOfAging = 0.88f,
            paceLabel = "0.88 yrs / yr (Optimal)",
            minScaleAge = 20f,
            maxScaleAge = 40f
        )
    }

    fun getStreaksTelemetry(): StreaksTelemetry {
        // Deterministic random seed for 4 months heatmap
        val random = Random(42)
        val cells = mutableListOf<DayHeatmapCell>()

        for (month in 0..3) {
            // Approx 4 weeks per month, 7 days per week
            for (day in 0..6) {
                for (week in 0..3) {
                    val intensity = when {
                        month == 3 && week == 3 && day > 4 -> 0.0f // Future days in Oct
                        else -> {
                            val r = random.nextFloat()
                            if (r < 0.15f) 0.0f
                            else if (r < 0.45f) 0.35f
                            else if (r < 0.8f) 0.7f
                            else 1.0f
                        }
                    }
                    cells.add(DayHeatmapCell(month, day, intensity))
                }
            }
        }

        val badges = listOf(
            BiometricBadge("recovery_master", "Recovery Master", "Recovery", true, Color(0xFF00E676)),
            BiometricBadge("summit_10k", "Summit 10K", "Steps", true, Color(0xFF388AF6)),
            BiometricBadge("sleep_architect", "Sleep Architect", "Sleep", true, Color(0xFF9D4EDD)),
            BiometricBadge("hrv_flow", "HRV Flow", "Heart", true, Color(0xFF12E6C8)),
            BiometricBadge("longevity_prime", "Longevity Prime", "Longevity", true, Color(0xFFFFB703)),
            BiometricBadge("streak_titan", "Streak Titan", "Consistency", false, Color(0xFF6B7280))
        )

        return StreaksTelemetry(
            currentStreakDays = 12,
            bestStreakDays = 21,
            totalDaysLogged = 94,
            complianceRatePercent = 82,
            recentDayMilestones = listOf(
                true, true, true, true, true, true, true, true, true, true
            ),
            heatmapCells = cells,
            badges = badges
        )
    }
}
