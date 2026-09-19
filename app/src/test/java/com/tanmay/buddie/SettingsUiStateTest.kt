package com.tanmay.buddie

import com.tanmay.buddie.presentation.components.mascot.CompanionType
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
        assertEquals("COMPANION_HEADER", state.cardStyle)
        assertEquals(CompanionType.BUDDIE, state.companionType)
        assertEquals(24, state.notificationHours)
        assertFalse(state.batterySaverEnabled)
    }

    @Test
    fun settingsUiState_withFloatingBubbleEnabled() {
        val state = SettingsUiState(
            cardStyle = "COMPANION_HEADER",
            notificationHours = 48,
            floatingBubbleEnabled = true,
            batteryOptimizationPromptDismissed = false,
            batterySaverEnabled = true
        )
        assertTrue(state.floatingBubbleEnabled)
        assertEquals("COMPANION_HEADER", state.cardStyle)
        assertEquals(48, state.notificationHours)
        assertFalse(state.batteryOptimizationPromptDismissed)
        assertTrue(state.batterySaverEnabled)
    }

    @Test
    fun settingsUiState_withBatteryOptimizationDismissed() {
        val state = SettingsUiState(
            floatingBubbleEnabled = true,
            batteryOptimizationPromptDismissed = true
        )
        assertTrue(state.floatingBubbleEnabled)
        assertTrue(state.batteryOptimizationPromptDismissed)
        assertFalse(state.batterySaverEnabled)
    }
}
