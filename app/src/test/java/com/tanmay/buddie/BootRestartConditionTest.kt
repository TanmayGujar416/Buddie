package com.tanmay.buddie

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootRestartConditionTest {

    private fun shouldRestartBubbleServiceOnBoot(
        hasOverlayPermission: Boolean,
        isBubbleEnabled: Boolean
    ): Boolean {
        return hasOverlayPermission && isBubbleEnabled
    }

    @Test
    fun shouldRestart_whenPermissionGrantedAndBubbleEnabled() {
        assertTrue(shouldRestartBubbleServiceOnBoot(hasOverlayPermission = true, isBubbleEnabled = true))
    }

    @Test
    fun shouldNotRestart_whenPermissionMissingEvenIfBubbleEnabled() {
        assertFalse(shouldRestartBubbleServiceOnBoot(hasOverlayPermission = false, isBubbleEnabled = true))
    }

    @Test
    fun shouldNotRestart_whenBubbleDisabledEvenIfPermissionGranted() {
        assertFalse(shouldRestartBubbleServiceOnBoot(hasOverlayPermission = true, isBubbleEnabled = false))
    }

    @Test
    fun shouldNotRestart_whenBothPermissionMissingAndBubbleDisabled() {
        assertFalse(shouldRestartBubbleServiceOnBoot(hasOverlayPermission = false, isBubbleEnabled = false))
    }
}
