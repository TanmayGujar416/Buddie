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

    /**
     * 4 distinct at-a-glance status dot colors for the mood states:
     * - CALM: Emerald Green (clear/peaceful)
     * - AWARE: Amber Yellow (upcoming soon)
     * - URGENT: Vibrant Coral Orange (due within 24h, distinct from red)
     * - OVERDUE: Deep Crimson Red (missed deadline)
     */
    fun getMoodDotColor(mood: MascotMood): androidx.compose.ui.graphics.Color = when (mood) {
        MascotMood.CALM -> androidx.compose.ui.graphics.Color(0xFF22C55E)
        MascotMood.AWARE -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
        MascotMood.URGENT -> androidx.compose.ui.graphics.Color(0xFFFF7A45)
        MascotMood.OVERDUE -> androidx.compose.ui.graphics.Color(0xFFDC2626)
    }
}
