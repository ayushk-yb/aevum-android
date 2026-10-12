package com.aevum.app.data.model

import androidx.compose.ui.graphics.Color

enum class SegmentType(val label: String, val color: Color) {
    SLEEP("Sleep", Color(0xFF9D4EDD)),
    WALK("Walk", Color(0xFFFF6B4A)),
    STRESS("Stress", Color(0xFFFFB703)),
    IDLE("Rest", Color(0xFF282836))
}

data class TimelineSegment(
    val title: String,
    val type: SegmentType,
    val startHourFraction: Float, // 0f to 24f
    val endHourFraction: Float,
    val durationFormatted: String
)

data class VitalMetric(
    val title: String,
    val value: String,
    val unit: String,
    val delta: String,
    val isPositive: Boolean,
    val waveformPoints: List<Float>
)

data class RecoveryOverview(
    val recoveryScore: Int = 72,
    val sleepScore: Int = 88,
    val strainScore: Float = 6.4f,
    val coachingMessage: String = "Well recovered. Push to a strain of 11–14.",
    val timelineSegments: List<TimelineSegment> = emptyList(),
    val hrvMetric: VitalMetric,
    val restingHrMetric: VitalMetric
)

data class HourlySteps(
    val hour: Int, // 0 to 23
    val steps: Int,
    val usualSteps: Int,
    val isPeak: Boolean = false
)

data class ElevationPoint(
    val progress: Float, // 0f to 1f
    val elevationNorm: Float, // 0f to 1f
    val label: String? = null
)

data class StepsTelemetry(
    val currentSteps: Int = 5240,
    val targetSteps: Int = 10000,
    val distanceKm: Float = 3.9f,
    val progressPercent: Int = 52,
    val deviationVsUsual: Int = -2860,
    val toSummitSteps: Int = 4760,
    val floorsClimbed: Int = 3,
    val activeKcal: Int = 212,
    val summitElevationPath: List<ElevationPoint> = emptyList(),
    val hourlyHistogram: List<HourlySteps> = emptyList()
)

enum class SleepStage(val level: Int, val label: String, val color: Color) {
    AWAKE(3, "Awake", Color(0xFFFFAA00)),
    REM(2, "REM", Color(0xFFC77DFF)),
    LIGHT(1, "Light", Color(0xFF5B67EA)),
    DEEP(0, "Deep", Color(0xFF1E2875))
}

data class SleepHypnogramSegment(
    val stage: SleepStage,
    val startFraction: Float, // 0f to 1f
    val durationFraction: Float,
    val startTimestampFormatted: String? = null
)

data class SleepTelemetry(
    val durationFormatted: String = "7h 12m",
    val efficiencyPercent: Int = 88,
    val bedTime: String = "11:32 PM",
    val wakeTime: String = "6:44 AM",
    val deepDurationFormatted: String = "1:36",
    val lightDurationFormatted: String = "3:00",
    val remDurationFormatted: String = "1:54",
    val awakeDurationFormatted: String = "42m",
    val gotMinutes: Int = 432,
    val neededMinutes: Int = 470,
    val deficitMinutes: Int = 38,
    val stages: List<SleepHypnogramSegment> = emptyList()
)

data class PulseAgeTelemetry(
    val calendarAge: Int = 34,
    val pulseAge: Float = 31.6f,
    val deltaYears: Float = -2.4f,
    val paceOfAging: Float = 0.88f, // < 1.0 is slower aging
    val paceLabel: String = "Slower Aging",
    val minScaleAge: Float = 20f,
    val maxScaleAge: Float = 40f
)

data class DayHeatmapCell(
    val monthIndex: Int, // 0 = Jul, 1 = Aug, 2 = Sep, 3 = Oct
    val dayOfWeek: Int, // 0 = Sun, 1 = Mon ... 6 = Sat
    val intensity: Float // 0f to 1f (0 = empty, 1 = max amber)
)

data class BiometricBadge(
    val id: String,
    val title: String,
    val category: String,
    val isUnlocked: Boolean,
    val iconColor: Color
)

data class StreaksTelemetry(
    val currentStreakDays: Int = 12,
    val bestStreakDays: Int = 21,
    val totalDaysLogged: Int = 94,
    val complianceRatePercent: Int = 82,
    val recentDayMilestones: List<Boolean> = emptyList(), // 10 days
    val heatmapCells: List<DayHeatmapCell> = emptyList(),
    val badges: List<BiometricBadge> = emptyList()
)
