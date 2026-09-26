package com.qmuduo.zichen.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class DialGeometryTest {
    private val centerX = 196.5f
    private val centerY = 306.72f

    @Test
    fun topRightAndBottomPointsMapClockwiseFromZi() {
        assertEquals(0, DialGeometry.sectorAt(centerX, centerY - 120f, centerX, centerY, 170f))
        assertEquals(1, DialGeometry.sectorAt(centerX + 60f, centerY - 103.92f, centerX, centerY, 170f))
        assertEquals(6, DialGeometry.sectorAt(centerX, centerY + 120f, centerX, centerY, 170f))
    }

    @Test
    fun shichenLabelCentersSelectTheirOwnSectors() {
        assertEquals(5, DialGeometry.sectorAt(centerX + 60f, centerY + 103.92f, centerX, centerY, 170f))
    }

    @Test
    fun gnomonAngleAdvancesContinuouslyThroughTheTwentyFourHourDial() {
        assertEquals(-15f, DialGeometry.pointerRotationDegrees(LocalTime.of(23, 0)), 0.001f)
        assertEquals(0f, DialGeometry.pointerRotationDegrees(LocalTime.MIDNIGHT), 0.001f)
        assertEquals(4.5f, DialGeometry.pointerRotationDegrees(LocalTime.of(0, 18)), 0.001f)
        assertEquals(4.50417f, DialGeometry.pointerRotationDegrees(LocalTime.of(0, 18, 1)), 0.001f)
        assertEquals(15f, DialGeometry.pointerRotationDegrees(LocalTime.of(1, 0)), 0.001f)
        assertEquals(255f, DialGeometry.pointerRotationDegrees(LocalTime.of(17, 0)), 0.001f)
    }

    @Test
    fun tapsOutsideTheDialOrInsideTheCenterDoNotSelectASector() {
        assertNull(DialGeometry.sectorAt(centerX + 180f, centerY, centerX, centerY, 170f))
        assertNull(DialGeometry.sectorAt(centerX + 100f, centerY, centerX, centerY, 170f))
        assertNull(DialGeometry.sectorAt(centerX, centerY, centerX, centerY, 170f))
    }
}
