package com.qmuduo.zichen.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDialStateTest {
    @Test
    fun previewChangesDisplayedShichenWithoutMovingTheLiveMarker() {
        val frame = HomeDialState().select(5).renderFor(currentIndex = 0)

        assertEquals(0, frame.actualIndex)
        assertEquals(5, frame.displayIndex)
        assertTrue(frame.isPreviewing)
    }

    @Test
    fun returningToCurrentRestoresTheLiveReading() {
        val frame = HomeDialState().select(5).returnToCurrent().renderFor(currentIndex = 0)

        assertEquals(0, frame.actualIndex)
        assertEquals(0, frame.displayIndex)
        assertFalse(frame.isPreviewing)
    }
}
