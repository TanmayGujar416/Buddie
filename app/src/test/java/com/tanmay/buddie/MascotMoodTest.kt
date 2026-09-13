package com.tanmay.buddie

import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.MascotMoodHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class MascotMoodTest {

    private val baseNow = LocalDateTime.of(2026, 9, 13, 12, 0)

    @Test
    fun testEmptyDeadlinesReturnsCalm() {
        val mood = MascotMoodHelper.calculateMood(emptyList(), now = baseNow)
        assertEquals(MascotMood.CALM, mood)
    }

    @Test
    fun testDeadlinesFarAwayReturnsCalm() {
        val deadlines = listOf(
            baseNow.plusDays(4),
            baseNow.plusDays(7)
        )
        val mood = MascotMoodHelper.calculateMood(deadlines, now = baseNow)
        assertEquals(MascotMood.CALM, mood)
    }

    @Test
    fun testDeadlinesInTwoToThreeDaysReturnsAware() {
        val deadlines = listOf(
            baseNow.plusDays(2), // 48 hours
            baseNow.plusDays(5)
        )
        val mood = MascotMoodHelper.calculateMood(deadlines, now = baseNow)
        assertEquals(MascotMood.AWARE, mood)
    }

    @Test
    fun testDeadlineWithin24HoursReturnsUrgent() {
        val deadlines = listOf(
            baseNow.plusHours(6),
            baseNow.plusDays(3)
        )
        val mood = MascotMoodHelper.calculateMood(deadlines, now = baseNow)
        assertEquals(MascotMood.URGENT, mood)
    }

    @Test
    fun testOverdueDeadlineReturnsOverdue() {
        val deadlines = listOf(
            baseNow.minusHours(2), // Missed deadline
            baseNow.plusDays(3)
        )
        val mood = MascotMoodHelper.calculateMood(deadlines, now = baseNow)
        assertEquals(MascotMood.OVERDUE, mood)
    }

    @Test
    fun testOverdueTakesPrecedenceOverUrgent() {
        val deadlines = listOf(
            baseNow.minusMinutes(10), // Overdue
            baseNow.plusHours(2)     // Also has urgent upcoming task
        )
        val mood = MascotMoodHelper.calculateMood(deadlines, now = baseNow)
        assertEquals(MascotMood.OVERDUE, mood)
    }
}
