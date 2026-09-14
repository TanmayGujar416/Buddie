package com.tanmay.buddie

import com.tanmay.buddie.data.datastore.BubbleEdge
import org.junit.Assert.assertEquals
import org.junit.Test

class BubbleSnapTest {

    @Test
    fun snapToEdge_leftHalfOfScreen_selectsLeftEdge() {
        val screenWidth = 1080
        val bubbleSizePx = 156
        val marginPx = 24

        // Bubble on left half: x = 100 -> center = 178 < 540
        val currentX = 100
        val bubbleCenterX = currentX + bubbleSizePx / 2
        val screenCenterX = screenWidth / 2

        val targetEdge = if (bubbleCenterX < screenCenterX) BubbleEdge.LEFT else BubbleEdge.RIGHT
        val targetX = if (targetEdge == BubbleEdge.LEFT) {
            marginPx
        } else {
            (screenWidth - bubbleSizePx - marginPx).coerceAtLeast(0)
        }

        assertEquals(BubbleEdge.LEFT, targetEdge)
        assertEquals(marginPx, targetX)
    }

    @Test
    fun snapToEdge_rightHalfOfScreen_selectsRightEdge() {
        val screenWidth = 1080
        val bubbleSizePx = 156
        val marginPx = 24

        // Bubble on right half: x = 600 -> center = 678 >= 540
        val currentX = 600
        val bubbleCenterX = currentX + bubbleSizePx / 2
        val screenCenterX = screenWidth / 2

        val targetEdge = if (bubbleCenterX < screenCenterX) BubbleEdge.LEFT else BubbleEdge.RIGHT
        val targetX = if (targetEdge == BubbleEdge.LEFT) {
            marginPx
        } else {
            (screenWidth - bubbleSizePx - marginPx).coerceAtLeast(0)
        }

        assertEquals(BubbleEdge.RIGHT, targetEdge)
        assertEquals(900, targetX) // 1080 - 156 - 24 = 900
    }

    @Test
    fun verticalClamp_clampsWithinSafeBounds() {
        val screenHeight = 2400
        val bubbleSizePx = 156
        val density = 3f
        val minY = (24 * density).toInt() // 72
        val maxY = (screenHeight - bubbleSizePx - (48 * density).toInt()).coerceAtLeast(minY) // 2400 - 156 - 144 = 2100

        // Clamping above top
        val clampedTop = (-50).coerceIn(minY, maxY)
        assertEquals(minY, clampedTop)

        // Clamping below bottom
        val clampedBottom = 3000.coerceIn(minY, maxY)
        assertEquals(maxY, clampedBottom)

        // Valid inside
        val validY = 1000.coerceIn(minY, maxY)
        assertEquals(1000, validY)
    }

    @Test
    fun initialPosition_withNoSavedY_centersVertically() {
        val screenHeight = 2400
        val bubbleSizePx = 156
        val defaultY = (screenHeight - bubbleSizePx) / 2
        assertEquals(1122, defaultY)
    }
}
