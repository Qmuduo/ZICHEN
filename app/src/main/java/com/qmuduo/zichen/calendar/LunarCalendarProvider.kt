package com.qmuduo.zichen.calendar

import java.time.LocalDate
import java.time.YearMonth

interface LunarCalendarProvider {
    fun getMonth(month: YearMonth): LunarCalendarMonth
    fun getDate(date: LocalDate): LunarCalendarDay
}

/** Monday-first, six-week grid. Null cells are padding, not adjacent-month dates. */
data class LunarCalendarMonth(
    val month: YearMonth,
    val cells: List<LunarCalendarDay?>,
)

/** App-owned values only; no third-party calendar objects escape the provider. */
data class LunarCalendarDay(
    val date: LocalDate,
    val weekday: String,
    val lunarYear: Int? = null,
    val lunarYearName: String? = null,
    val zodiac: String? = null,
    val lunarMonthName: String? = null,
    val lunarDayName: String? = null,
    val yearGanzhi: String? = null,
    val monthGanzhi: String? = null,
    val dayGanzhi: String? = null,
    val solarTerm: String? = null,
    val festivals: List<String> = emptyList(),
    val primaryCellFestival: String? = null,
    val yi: List<String> = emptyList(),
    val ji: List<String> = emptyList(),
    val nayin: String? = null,
    val clash: String? = null,
    val shaDirection: String? = null,
    val pengzuGan: String? = null,
    val pengzuZhi: String? = null,
    val xiDirection: String? = null,
    val fuDirection: String? = null,
    val caiDirection: String? = null,
    val auspiciousDeities: List<String> = emptyList(),
    val inauspiciousDeities: List<String> = emptyList(),
    val hasFullAlmanac: Boolean = false,
) {
    val hasLunarData: Boolean get() = lunarYear != null
    val lunarYearCaption: String?
        get() = lunarYearName?.let { year ->
            val zodiacCaption = zodiac?.let { " · ${it}年" }.orEmpty()
            "${year}年$zodiacCaption"
        }
    val lunarDateText: String?
        get() = lunarMonthName?.let { month -> month + lunarDayName.orEmpty() }
    val eventLabels: List<String>
        get() = (festivals + listOfNotNull(solarTerm)).distinct()
    val cellLabel: String? get() = primaryCellFestival ?: solarTerm
    val sourceDescription: String
        get() = "历法及民俗资料：6tail lunar-java 1.7.7；农历年与生肖以正月初一换年，年柱以立春当天换年，月柱以节交接当天换月，日柱以本地日期零点换日；福神采用流派二。"
}
