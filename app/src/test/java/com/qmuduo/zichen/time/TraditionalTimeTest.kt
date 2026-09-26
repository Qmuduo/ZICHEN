package com.qmuduo.zichen.time

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TraditionalTimeTest {
    @Test
    fun midnightBelongsToZiAndKeepsLocalClockText() {
        val reading = TraditionalTime.readAt(LocalTime.of(0, 18))

        assertEquals("子", reading.branch)
        assertEquals("夜半", reading.alias)
        assertEquals("正二刻", reading.quarterLabel)
        assertEquals("23:00 — 01:00", reading.interval)
        assertEquals("00:18", reading.clockText)
    }

    @Test
    fun everyShichenChangesAtItsTwoHourBoundary() {
        val cases = listOf(
            LocalTime.of(23, 0) to "子",
            LocalTime.of(1, 0) to "丑",
            LocalTime.of(3, 0) to "寅",
            LocalTime.of(5, 0) to "卯",
            LocalTime.of(7, 0) to "辰",
            LocalTime.of(9, 0) to "巳",
            LocalTime.of(11, 0) to "午",
            LocalTime.of(13, 0) to "未",
            LocalTime.of(15, 0) to "申",
            LocalTime.of(17, 0) to "酉",
            LocalTime.of(19, 0) to "戌",
            LocalTime.of(21, 0) to "亥",
        )

        cases.forEach { (time, expectedBranch) ->
            assertEquals("at $time", expectedBranch, TraditionalTime.readAt(time).branch)
        }
    }

    @Test
    fun initialAndZhengEachContainFourQuarterLabels() {
        assertEquals("初四刻", TraditionalTime.readAt(LocalTime.of(23, 59)).quarterLabel)
        assertEquals("正一刻", TraditionalTime.readAt(LocalTime.MIDNIGHT).quarterLabel)
        assertEquals("正二刻", TraditionalTime.readAt(LocalTime.of(0, 15)).quarterLabel)
    }

    @Test
    fun nightWatchUsesTwentyFourMinutePointsAndEndsAtFive() {
        assertEquals("一更二点", TraditionalTime.watchAt(LocalTime.of(19, 24))?.label)
        assertEquals("三更四点", TraditionalTime.watchAt(LocalTime.of(0, 18))?.label)
        assertEquals(null, TraditionalTime.watchAt(LocalTime.of(5, 0)))
    }

    @Test
    fun previewUsesTheSelectedShichenStartAndCurrentMinute() {
        val preview = TraditionalTime.previewAt(shichenIndex = 5, currentMinute = 18)

        assertEquals("巳", preview.branch)
        assertEquals("09:18", preview.clockText)
        assertEquals("初二刻", preview.quarterLabel)
        assertEquals("09:00 — 11:00", preview.interval)
    }
}
