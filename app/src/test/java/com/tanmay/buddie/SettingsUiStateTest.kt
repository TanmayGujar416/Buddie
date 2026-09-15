package com.tanmay.buddie

import com.tanmay.buddie.presentation.screens.settings.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUiStateTest {

    @Test
    fun defaultSettingsUiState_hasFloatingBubbleDisabled() {
        val state = SettingsUiState()
        assertFalse(state.floatingBubbleEnabled)
        assertEquals("SPEECH_BUBBLE", state.cardStyle)
        assertEquals(24, state.notificationHours)
    }

    @Test
    fun settingsUiState_withFloatingBubbleEnabled() {
        val state = SettingsUiState(
            cardStyle = "COMPANION_HEADER",
            notificationHours = 48,
            floatingBubbleEnabled = true,
            batteryOptimizationPromptDismissed = false
        )
        assertTrue(state.floatingBubbleEnabled)
        assertEquals("COMPANION_HEADER", state.cardStyle)
        assertEquals(48, state.notificationHours)
        assertFalse(state.batteryOptimizationPromptDismissed)
    }

    @Test
    fun settingsUiState_withBatteryOptimizationDismissed() {
        val state = SettingsUiState(
            floatingBubbleEnabled = true,
            batteryOptimizationPromptDismissed = true
        )
        assertTrue(state.floatingBubbleEnabled)
        assertTrue(state.batteryOptimizationPromptDismissed)
    }
}
