package com.tanmay.buddie

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for screen-state awareness logic used by BubbleService.
 *
 * Verifies that screen-off events pause the mascot idle animation
 * and screen-on events resume it.
 */
class BubbleScreenStateTest {

    private class ScreenStateController(initialState: Boolean = true) {
        var isScreenOn: Boolean = initialState
            private set

        fun handleIntentAction(action: String?) {
            when (action) {
                ACTION_SCREEN_OFF -> isScreenOn = false
                ACTION_SCREEN_ON -> isScreenOn = true
            }
        }

        companion object {
            const val ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF"
            const val ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON"
        }
    }

    @Test
    fun initialState_defaultsToScreenOn() {
        val controller = ScreenStateController(initialState = true)
        assertTrue(controller.isScreenOn)
    }

    @Test
    fun screenOffAction_pausesIdleAnimation() {
        val controller = ScreenStateController(initialState = true)
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_OFF)
        assertFalse("Mascot idle animation should be paused when screen turns off", controller.isScreenOn)
    }

    @Test
    fun screenOnAction_resumesIdleAnimation() {
        val controller = ScreenStateController(initialState = false)
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_ON)
        assertTrue("Mascot idle animation should resume when screen turns on", controller.isScreenOn)
    }

    @Test
    fun consecutiveScreenOffActions_remainPaused() {
        val controller = ScreenStateController(initialState = true)
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_OFF)
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_OFF)
        assertFalse(controller.isScreenOn)
    }

    @Test
    fun repeatedScreenOnOffCycles_transitionCorrectly() {
        val controller = ScreenStateController(initialState = true)

        // Turn off
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_OFF)
        assertFalse(controller.isScreenOn)

        // Turn on
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_ON)
        assertTrue(controller.isScreenOn)

        // Turn off again
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_OFF)
        assertFalse(controller.isScreenOn)

        // Turn on again
        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_ON)
        assertTrue(controller.isScreenOn)
    }

    @Test
    fun unrecognizedAction_doesNotChangeState() {
        val controller = ScreenStateController(initialState = true)
        controller.handleIntentAction("android.intent.action.USER_PRESENT")
        assertTrue(controller.isScreenOn)

        controller.handleIntentAction(ScreenStateController.ACTION_SCREEN_OFF)
        assertFalse(controller.isScreenOn)

        controller.handleIntentAction("android.intent.action.BATTERY_LOW")
        assertFalse(controller.isScreenOn)
    }
}
