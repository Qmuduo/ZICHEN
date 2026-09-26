package com.qmuduo.zichen.time

import java.time.LocalTime

data class Shichen(
    val index: Int,
    val branch: String,
    val alias: String,
    val startHour: Int,
    val poem: String,
) {
    val interval: String
        get() = "${formatHour(startHour)}:00 — ${formatHour((startHour + 2) % 24)}:00"
}

data class TimeReading(
    val shichen: Shichen,
    val localTime: LocalTime,
    val phase: String,
    val quarterNumber: Int,
) {
    val branch: String get() = shichen.branch
    val alias: String get() = shichen.alias
    val interval: String get() = shichen.interval
    val poem: String get() = shichen.poem
    val clockText: String get() = "${formatHour(localTime.hour)}:${formatHour(localTime.minute)}"
    val quarterLabel: String get() = "$phase${CHINESE_NUMBERS[quarterNumber - 1]}刻"
}

data class WatchReading(
    val gengNumber: Int,
    val pointNumber: Int,
) {
    val label: String
        get() = "${CHINESE_NUMBERS[gengNumber - 1]}更${CHINESE_NUMBERS[pointNumber - 1]}点"
}

object TraditionalTime {
    val shichen: List<Shichen> = listOf(
        Shichen(0, "子", "夜半", 23, "归来仿佛三更"),
        Shichen(1, "丑", "鸡鸣", 1, "虫声新透绿窗纱"),
        Shichen(2, "寅", "平旦", 3, "处处闻啼鸟"),
        Shichen(3, "卯", "日出", 5, "晨光出照屋梁明"),
        Shichen(4, "辰", "食时", 7, "春风花草香"),
        Shichen(5, "巳", "隅中", 9, "松间沙路净无泥"),
        Shichen(6, "午", "日中", 11, "闲看儿童捉柳花"),
        Shichen(7, "未", "日昳", 13, "浮萍一道开"),
        Shichen(8, "申", "晡时", 15, "返景入深林"),
        Shichen(9, "酉", "日入", 17, "夕阳无限好"),
        Shichen(10, "戌", "黄昏", 19, "明月松间照"),
        Shichen(11, "亥", "人定", 21, "巴山夜雨涨秋池"),
    )

    fun readAt(time: LocalTime): TimeReading {
        val index = ((time.hour + 1) % 24) / 2
        val current = shichen[index]
        val elapsedMinutes = ((time.hour - current.startHour + 24) % 24) * 60 + time.minute
        val phase = if (elapsedMinutes < 60) "初" else "正"
        val quarterNumber = (elapsedMinutes % 60) / 15 + 1
        return TimeReading(current, time, phase, quarterNumber)
    }

    fun previewAt(shichenIndex: Int, currentMinute: Int): TimeReading {
        require(shichenIndex in shichen.indices) { "shichenIndex must be in 0..11" }
        require(currentMinute in 0..59) { "currentMinute must be in 0..59" }
        return readAt(LocalTime.of(shichen[shichenIndex].startHour, currentMinute))
    }

    fun watchAt(time: LocalTime): WatchReading? {
        if (time.hour !in 0..4 && time.hour !in 19..23) return null

        val minutesSinceFirstWatch = if (time.hour >= 19) {
            (time.hour - 19) * 60 + time.minute
        } else {
            (time.hour + 5) * 60 + time.minute
        }
        if (minutesSinceFirstWatch >= 10 * 60) return null

        val gengNumber = minutesSinceFirstWatch / 120 + 1
        val pointNumber = (minutesSinceFirstWatch % 120) / 24 + 1
        return WatchReading(gengNumber, pointNumber)
    }
}

private val CHINESE_NUMBERS = "一二三四五六七八"

private fun formatHour(value: Int): String = value.toString().padStart(2, '0')
