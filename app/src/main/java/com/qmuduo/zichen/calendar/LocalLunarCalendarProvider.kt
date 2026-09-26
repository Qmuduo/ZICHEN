package com.qmuduo.zichen.calendar

import com.nlf.calendar.Solar
import java.time.LocalDate
import java.time.YearMonth

class LocalLunarCalendarProvider : LunarCalendarProvider {
    override fun getMonth(month: YearMonth): LunarCalendarMonth {
        val offset = month.atDay(1).dayOfWeek.value - 1
        return LunarCalendarMonth(
            month = month,
            cells = List(42) { index ->
                val day = index - offset + 1
                if (day in 1..month.lengthOfMonth()) getDate(month.atDay(day)) else null
            },
        )
    }

    override fun getDate(date: LocalDate): LunarCalendarDay {
        val gregorian = LunarCalendarDay(
            date = date,
            weekday = "星期" + "一二三四五六日"[date.dayOfWeek.value - 1],
        )
        // LocalDate is proleptic Gregorian; the engine uses Julian dates before this
        // cutover. Do not silently show a lunar conversion for a different date.
        // Bound astronomical calculations to positive four-digit calendar years.
        if (date < GREGORIAN_START || date.year > 9999) return gregorian

        return try {
            val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
            val lunar = solar.lunar
            val fullAlmanac = date.year in 2025..2028
            gregorian.copy(
                lunarYear = lunar.year,
                lunarYearName = text { lunar.yearInGanZhi },
                zodiac = text { lunar.yearShengXiao },
                lunarMonthName = text { lunar.monthInChinese }?.let { "${it}月" },
                lunarDayName = text { lunar.dayInChinese },
                yearGanzhi = text { lunar.yearInGanZhiByLiChun },
                monthGanzhi = text { lunar.monthInGanZhi },
                dayGanzhi = text { lunar.dayInGanZhi },
                solarTerm = text { lunar.jieQi },
                festivals = (labels { lunar.festivals } + labels { solar.festivals } +
                    labels { lunar.otherFestivals } + labels { solar.otherFestivals }).distinct(),
                yi = if (fullAlmanac) labels { lunar.dayYi } else emptyList(),
                ji = if (fullAlmanac) labels { lunar.dayJi } else emptyList(),
                nayin = if (fullAlmanac) text { lunar.dayNaYin } else null,
                clash = if (fullAlmanac) text { lunar.dayChongDesc }?.let { "冲$it" } else null,
                shaDirection = if (fullAlmanac) text { lunar.daySha } else null,
                pengzuGan = if (fullAlmanac) text { lunar.pengZuGan } else null,
                pengzuZhi = if (fullAlmanac) text { lunar.pengZuZhi } else null,
                xiDirection = if (fullAlmanac) text { lunar.dayPositionXiDesc } else null,
                fuDirection = if (fullAlmanac) text { lunar.getDayPositionFuDesc(2) } else null,
                caiDirection = if (fullAlmanac) text { lunar.dayPositionCaiDesc } else null,
                auspiciousDeities = if (fullAlmanac) labels { lunar.dayJiShen } else emptyList(),
                inauspiciousDeities = if (fullAlmanac) labels { lunar.dayXiongSha } else emptyList(),
                hasFullAlmanac = fullAlmanac,
            )
        } catch (_: RuntimeException) {
            gregorian
        }
    }

    // A missing optional field must not discard the remaining calendar data.
    private fun text(read: () -> String?): String? = try {
        read()?.trim()?.takeIf { it.isNotEmpty() }
    } catch (_: RuntimeException) {
        null
    }

    private fun labels(read: () -> List<String>?): List<String> = try {
        read().orEmpty().map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    } catch (_: RuntimeException) {
        emptyList()
    }

    private companion object {
        val GREGORIAN_START: LocalDate = LocalDate.of(1582, 10, 15)
    }
}
