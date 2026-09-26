package com.qmuduo.zichen.date

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateInfoProvider : DateInfoProvider {
    override fun getDateInfo(instant: Instant, zone: ZoneId): DateInfo {
        val localDate = instant.atZone(zone).toLocalDate()
        val cycleIndex = Math.floorMod(localDate.toEpochDay() - GANZHI_ANCHOR_DAY, 60L).toInt()
        val dayGanzhi = "${HEAVENLY_STEMS[cycleIndex % 10]}${EARTHLY_BRANCHES[cycleIndex % 12]}"
        val solarTerm = if (instant < FIRST_TERM_INSTANT || instant > LAST_TERM_INSTANT) {
            null
        } else {
            solarTerms.lastOrNull { it.instant <= instant }
        }

        return DateInfo(localDate, dayGanzhi, solarTerm)
    }

    private companion object {
        private val SOURCE_ZONE: ZoneId = ZoneId.of("Asia/Shanghai")
        private val GANZHI_ANCHOR_DAY: Long = LocalDate.of(2000, 1, 7).toEpochDay() // 甲子日

        private const val HEAVENLY_STEMS = "甲乙丙丁戊己庚辛壬癸"
        private const val EARTHLY_BRANCHES = "子丑寅卯辰巳午未申酉戌亥"

        private val solarTerms = listOf(
            term("小寒", "2025-01-05T10:33"),
            term("大寒", "2025-01-20T04:00"),
            term("立春", "2025-02-03T22:10"),
            term("雨水", "2025-02-18T18:07"),
            term("惊蛰", "2025-03-05T16:07"),
            term("春分", "2025-03-20T17:01"),
            term("清明", "2025-04-04T20:49"),
            term("谷雨", "2025-04-20T03:56"),
            term("立夏", "2025-05-05T13:57"),
            term("小满", "2025-05-21T02:55"),
            term("芒种", "2025-06-05T17:57"),
            term("夏至", "2025-06-21T10:42"),
            term("小暑", "2025-07-07T04:05"),
            term("大暑", "2025-07-22T21:29"),
            term("立秋", "2025-08-07T13:52"),
            term("处暑", "2025-08-23T04:34"),
            term("白露", "2025-09-07T16:52"),
            term("秋分", "2025-09-23T02:19"),
            term("寒露", "2025-10-08T08:41"),
            term("霜降", "2025-10-23T11:51"),
            term("立冬", "2025-11-07T12:04"),
            term("小雪", "2025-11-22T09:36"),
            term("大雪", "2025-12-07T05:05"),
            term("冬至", "2025-12-21T23:03"),
            term("小寒", "2026-01-05T16:23"),
            term("大寒", "2026-01-20T09:45"),
            term("立春", "2026-02-04T04:02"),
            term("雨水", "2026-02-18T23:52"),
            term("惊蛰", "2026-03-05T21:59"),
            term("春分", "2026-03-20T22:46"),
            term("清明", "2026-04-05T02:40"),
            term("谷雨", "2026-04-20T09:39"),
            term("立夏", "2026-05-05T19:49"),
            term("小满", "2026-05-21T08:37"),
            term("芒种", "2026-06-05T23:48"),
            term("夏至", "2026-06-21T16:25"),
            term("小暑", "2026-07-07T09:57"),
            term("大暑", "2026-07-23T03:13"),
            term("立秋", "2026-08-07T19:43"),
            term("处暑", "2026-08-23T10:19"),
            term("白露", "2026-09-07T22:41"),
            term("秋分", "2026-09-23T08:05"),
            term("寒露", "2026-10-08T14:29"),
            term("霜降", "2026-10-23T17:38"),
            term("立冬", "2026-11-07T17:52"),
            term("小雪", "2026-11-22T15:23"),
            term("大雪", "2026-12-07T10:53"),
            term("冬至", "2026-12-22T04:50"),
        ).sortedBy(SolarTerm::instant)

        private val FIRST_TERM_INSTANT = solarTerms.first().instant
        private val LAST_TERM_INSTANT = solarTerms.last().instant

        private fun term(name: String, sourceLocalDateTime: String): SolarTerm = SolarTerm(
            name = name,
            instant = LocalDateTime.parse(sourceLocalDateTime).atZone(SOURCE_ZONE).toInstant(),
        )
    }
}
