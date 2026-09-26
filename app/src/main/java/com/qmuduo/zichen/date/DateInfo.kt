package com.qmuduo.zichen.date

import java.time.Instant
import java.time.LocalDate

data class SolarTerm(
    val name: String,
    val instant: Instant,
)

data class DateInfo(
    val localDate: LocalDate,
    val dayGanzhi: String,
    val currentSolarTerm: SolarTerm?,
)
