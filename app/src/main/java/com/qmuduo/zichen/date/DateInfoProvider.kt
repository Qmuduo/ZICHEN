package com.qmuduo.zichen.date

import java.time.Instant
import java.time.ZoneId

fun interface DateInfoProvider {
    fun getDateInfo(instant: Instant, zone: ZoneId): DateInfo
}
