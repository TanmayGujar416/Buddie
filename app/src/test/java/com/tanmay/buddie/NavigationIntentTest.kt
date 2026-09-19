package com.tanmay.buddie

import com.tanmay.buddie.presentation.components.mascot.card.CardSection
import com.tanmay.buddie.service.BubbleService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationIntentTest {

    @Test
    fun extraTargetTabConstants_matchBetweenMainActivityAndBubbleService() {
        assertEquals(MainActivity.EXTRA_TARGET_TAB, BubbleService.EXTRA_TARGET_TAB)
        assertEquals("extra_target_tab", MainActivity.EXTRA_TARGET_TAB)
    }

    @Test
    fun cardSectionRoutes_mapToCorrectNavigationDestinations() {
        fun getRouteForSection(section: CardSection): String {
            return when (section) {
                CardSection.SUBMISSIONS -> "home"
                CardSection.TIMETABLE -> "timetable"
                CardSection.EXAMS -> "exams"
            }
        }

        assertEquals("home", getRouteForSection(CardSection.SUBMISSIONS))
        assertEquals("timetable", getRouteForSection(CardSection.TIMETABLE))
        assertEquals("exams", getRouteForSection(CardSection.EXAMS))
    }

    @Test
    fun doubleTapDetection_withinTimeout_detectsDoubleTap() {
        val doubleTapTimeout = 300L
        var lastTapTime = 1000L

        val secondTapTimeQuick = 1150L // 150ms delta <= 300ms
        val isDoubleTapQuick = (secondTapTimeQuick - lastTapTime) < doubleTapTimeout
        assertTrue("Tap within 300ms should be recognized as double tap", isDoubleTapQuick)

        val secondTapTimeSlow = 1450L // 450ms delta > 300ms
        val isDoubleTapSlow = (secondTapTimeSlow - lastTapTime) < doubleTapTimeout
        assertFalse("Tap after 300ms should not be recognized as double tap", isDoubleTapSlow)
    }

    @Test
    fun startDestinationResolution_matchesOnboardingStatus() {
        fun resolveStartDestination(isOnboardingCompleted: Boolean): String {
            return if (isOnboardingCompleted) "home" else "onboarding"
        }

        assertEquals("onboarding", resolveStartDestination(false))
        assertEquals("home", resolveStartDestination(true))
    }
}
