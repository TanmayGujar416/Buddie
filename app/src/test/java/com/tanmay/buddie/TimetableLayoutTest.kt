package com.tanmay.buddie

import com.tanmay.buddie.presentation.screens.timetable.computeOverlappingLayout
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class TimetableLayoutTest {

    data class SimpleSlot(val id: String, val start: LocalTime, val end: LocalTime)

    @Test
    fun testNonOverlappingSlots() {
        val slots = listOf(
            SimpleSlot("1", LocalTime.of(9, 0), LocalTime.of(10, 0)),
            SimpleSlot("2", LocalTime.of(10, 0), LocalTime.of(11, 0)),
            SimpleSlot("3", LocalTime.of(13, 0), LocalTime.of(14, 0))
        )
        val layout = computeOverlappingLayout(slots) { it.start to it.end }

        assertEquals(3, layout.size)
        // All non-overlapping slots should have totalCols = 1 and colIndex = 0 (100% width)
        layout.forEach {
            assertEquals(1, it.totalCols)
            assertEquals(0, it.colIndex)
        }
    }

    @Test
    fun testTwoOverlappingSlots() {
        // User test case: 11:00-11:30 and 11:15-12:15
        val slots = listOf(
            SimpleSlot("A", LocalTime.of(11, 0), LocalTime.of(11, 30)),
            SimpleSlot("B", LocalTime.of(11, 15), LocalTime.of(12, 15))
        )
        val layout = computeOverlappingLayout(slots) { it.start to it.end }

        assertEquals(2, layout.size)
        val itemA = layout.find { it.item.id == "A" }!!
        val itemB = layout.find { it.item.id == "B" }!!

        assertEquals(2, itemA.totalCols)
        assertEquals(0, itemA.colIndex)

        assertEquals(2, itemB.totalCols)
        assertEquals(1, itemB.colIndex)
    }

    @Test
    fun testThreeConcurrentOverlappingSlots() {
        val slots = listOf(
            SimpleSlot("A", LocalTime.of(10, 0), LocalTime.of(11, 0)),
            SimpleSlot("B", LocalTime.of(10, 15), LocalTime.of(11, 15)),
            SimpleSlot("C", LocalTime.of(10, 30), LocalTime.of(11, 30))
        )
        val layout = computeOverlappingLayout(slots) { it.start to it.end }

        assertEquals(3, layout.size)
        layout.forEach {
            assertEquals(3, it.totalCols)
        }
        val cols = layout.map { it.colIndex }.toSet()
        assertEquals(setOf(0, 1, 2), cols)
    }

    @Test
    fun testMixedOverlappingAndNonOverlapping() {
        val slots = listOf(
            SimpleSlot("Solo1", LocalTime.of(8, 0), LocalTime.of(9, 0)),
            SimpleSlot("Overlap1", LocalTime.of(11, 0), LocalTime.of(11, 30)),
            SimpleSlot("Overlap2", LocalTime.of(11, 15), LocalTime.of(12, 15)),
            SimpleSlot("Solo2", LocalTime.of(14, 0), LocalTime.of(15, 0))
        )
        val layout = computeOverlappingLayout(slots) { it.start to it.end }

        val solo1 = layout.find { it.item.id == "Solo1" }!!
        val overlap1 = layout.find { it.item.id == "Overlap1" }!!
        val overlap2 = layout.find { it.item.id == "Overlap2" }!!
        val solo2 = layout.find { it.item.id == "Solo2" }!!

        assertEquals(1, solo1.totalCols)
        assertEquals(0, solo1.colIndex)

        assertEquals(2, overlap1.totalCols)
        assertEquals(0, overlap1.colIndex)

        assertEquals(2, overlap2.totalCols)
        assertEquals(1, overlap2.colIndex)

        assertEquals(1, solo2.totalCols)
        assertEquals(0, solo2.colIndex)
    }
}
