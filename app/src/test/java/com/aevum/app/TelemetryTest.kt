package com.aevum.app

import com.aevum.app.data.healthconnect.MockTelemetryProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryTest {

    @Test
    fun testRecoveryOverviewDefaults() {
        val recovery = MockTelemetryProvider.getRecoveryOverview()
        assertEquals(72, recovery.recoveryScore)
        assertEquals(88, recovery.sleepScore)
        assertEquals(6.4f, recovery.strainScore, 0.01f)
        assertEquals(3, recovery.timelineSegments.size)
        assertEquals("54", recovery.hrvMetric.value)
        assertEquals("57", recovery.restingHrMetric.value)
    }

    @Test
    fun testStepsTelemetrySummitPath() {
        val steps = MockTelemetryProvider.getStepsTelemetry()
        assertEquals(5240, steps.currentSteps)
        assertEquals(10000, steps.targetSteps)
        assertEquals(52, steps.progressPercent)
        assertTrue(steps.summitElevationPath.isNotEmpty())
        assertEquals("10,000", steps.summitElevationPath.last().label)
    }

    @Test
    fun testSleepTelemetryHypnogram() {
        val sleep = MockTelemetryProvider.getSleepTelemetry()
        assertEquals(88, sleep.efficiencyPercent)
        assertEquals("7h 12m", sleep.durationFormatted)
        assertEquals(38, sleep.deficitMinutes)
        assertTrue(sleep.stages.isNotEmpty())
    }

    @Test
    fun testPulseAgeLongevityCalculation() {
        val pulseAge = MockTelemetryProvider.getPulseAgeTelemetry()
        assertEquals(34, pulseAge.calendarAge)
        assertEquals(31.6f, pulseAge.pulseAge, 0.01f)
        assertTrue(pulseAge.deltaYears < 0)
    }

    @Test
    fun testStreaksAndBadges() {
        val streaks = MockTelemetryProvider.getStreaksTelemetry()
        assertEquals(12, streaks.currentStreakDays)
        assertEquals(21, streaks.bestStreakDays)
        assertEquals(94, streaks.totalDaysLogged)
        assertEquals(82, streaks.complianceRatePercent)
        assertEquals(10, streaks.recentDayMilestones.size)
        assertTrue(streaks.badges.isNotEmpty())
    }
}
