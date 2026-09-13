package com.tanmay.buddie.presentation.components.mascot

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object MascotMoodHelper {
    /**
     * Calculates the mascot mood based on the closest deadline per mascot-style-guide.md:
     * - Overdue: Any uncompleted deadline has passed
     * - Urgent: Closest deadline within ~24 hours
     * - Aware: Closest deadline in ~2-3 days (up to 72 hours)
     * - Calm: Nothing due for several days (> 72 hours or no tasks)
     */
    fun calculateMood(
        deadlines: List<LocalDateTime>,
        now: LocalDateTime = LocalDateTime.now()
    ): MascotMood {
        if (deadlines.isEmpty()) return MascotMood.CALM

        // If any deadline has passed, mascot enters OVERDUE state
        if (deadlines.any { it.isBefore(now) }) {
            return MascotMood.OVERDUE
        }

        // Among upcoming deadlines, find the nearest one
        val nearest = deadlines.minOrNull() ?: return MascotMood.CALM
        val hoursUntil = ChronoUnit.HOURS.between(now, nearest)

        return when {
            hoursUntil <= 24 -> MascotMood.URGENT
            hoursUntil <= 72 -> MascotMood.AWARE
            else -> MascotMood.CALM
        }
    }
}
