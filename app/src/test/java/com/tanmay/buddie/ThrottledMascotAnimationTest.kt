package com.tanmay.buddie

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import com.tanmay.buddie.presentation.components.mascot.MASCOT_ANIMATION_INTERVAL_MS
import com.tanmay.buddie.presentation.components.mascot.calculateThrottledValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThrottledMascotAnimationTest {

    @Test
    fun testIntervalCorrespondsTo15To20UpdatesPerSec() {
        val updatesPerSecond = 1000.0 / MASCOT_ANIMATION_INTERVAL_MS
        assertTrue("Updates/sec should be >= 15: got $updatesPerSecond", updatesPerSecond >= 15.0)
        assertTrue("Updates/sec should be <= 20: got $updatesPerSecond", updatesPerSecond <= 20.0)
    }

    @Test
    fun testReverseAnimationStartEndApex() {
        val initial = -3.5f
        val target = 3.5f
        val duration = 1800

        // At t = 0: initial value
        val atStart = calculateThrottledValue(0L, initial, target, duration, FastOutSlowInEasing, RepeatMode.Reverse)
        assertEquals(initial, atStart, 0.001f)

        // At t = 1800 (apex): target value
        val atApex = calculateThrottledValue(1800L, initial, target, duration, FastOutSlowInEasing, RepeatMode.Reverse)
        assertEquals(target, atApex, 0.001f)

        // At t = 3600 (full cycle completed): back to initial value
        val atFullCycle = calculateThrottledValue(3600L, initial, target, duration, FastOutSlowInEasing, RepeatMode.Reverse)
        assertEquals(initial, atFullCycle, 0.001f)

        // At t = 5400 (cycle 1.5): target value again
        val atApex2 = calculateThrottledValue(5400L, initial, target, duration, FastOutSlowInEasing, RepeatMode.Reverse)
        assertEquals(target, atApex2, 0.001f)
    }

    @Test
    fun testLinearRestartAnimation() {
        val initial = 0f
        val target = 100f
        val duration = 1000

        // Start
        val start = calculateThrottledValue(0L, initial, target, duration, LinearEasing, RepeatMode.Restart)
        assertEquals(0f, start, 0.001f)

        // Halfway
        val halfway = calculateThrottledValue(500L, initial, target, duration, LinearEasing, RepeatMode.Restart)
        assertEquals(50f, halfway, 0.001f)

        // Restart at 1000
        val restart = calculateThrottledValue(1000L, initial, target, duration, LinearEasing, RepeatMode.Restart)
        assertEquals(0f, restart, 0.001f)
    }

    @Test
    fun testInvalidDurationReturnsInitial() {
        val value = calculateThrottledValue(500L, 1.0f, 2.0f, 0)
        assertEquals(1.0f, value, 0.001f)
    }
}
