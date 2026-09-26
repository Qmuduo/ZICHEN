package com.qmuduo.zichen.ui.home

import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.hypot
import java.time.LocalTime

object DialGeometry {
    private const val SECTOR_DEGREES = 30.0
    private const val MIN_TOUCH_RADIUS = 20.0
    private const val INNER_DIAL_RADIUS_RATIO = 108.0 / 170.0

    /** Returns 子 at the top and advances clockwise; center and outside taps are ignored. */
    fun sectorAt(
        x: Float,
        y: Float,
        centerX: Float,
        centerY: Float,
        radius: Float,
    ): Int? {
        if (radius <= MIN_TOUCH_RADIUS) return null

        val dx = (x - centerX).toDouble()
        val dy = (y - centerY).toDouble()
        val distance = hypot(dx, dy)
        val innerRadius = radius * INNER_DIAL_RADIUS_RATIO
        if (distance < maxOf(MIN_TOUCH_RADIUS, innerRadius) || distance > radius.toDouble()) return null

        val angleFromRight = Math.toDegrees(atan2(dy, dx))
        val clockwiseFromTop = ((angleFromRight + 90.0) % 360.0 + 360.0) % 360.0
        val centeredSector = floor((clockwiseFromTop + SECTOR_DEGREES / 2.0) / SECTOR_DEGREES).toInt()
        return centeredSector % 12
    }

    /** Rotation clockwise from the top: the dial advances 30° per two-hour shichen. */
    fun pointerRotationDegrees(time: LocalTime): Float {
        val hoursFromZiStart = (time.hour - 23 + 24) % 24
        val elapsedMinutes = hoursFromZiStart * 60.0 + time.minute + time.second / 60.0 + time.nano / 60_000_000_000.0
        return (-15.0 + elapsedMinutes / 4.0).toFloat()
    }
}
