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
            floatingBubbleEnabled = true
        )
        assertTrue(state.floatingBubbleEnabled)
        assertEquals("COMPANION_HEADER", state.cardStyle)
        assertEquals(48, state.notificationHours)
    }
}
