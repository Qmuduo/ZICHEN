package com.qmuduo.zichen.ui.date

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuduo.zichen.calendar.LunarCalendarDay
import com.qmuduo.zichen.calendar.LunarCalendarProvider
import com.qmuduo.zichen.ui.theme.HomePalette
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun LunarCalendarScreen(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    provider: LunarCalendarProvider,
    onBack: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToday: () -> Unit,
    onOpenClockCalendar: () -> Unit,
    onOpenDetails: (LocalDate) -> Unit,
) {
    val calendar = remember(provider, month) { provider.getMonth(month) }
    val selectedDay = remember(provider, selectedDate, calendar) {
        calendar.cells.firstOrNull { it?.date == selectedDate } ?: provider.getDate(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomePalette.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CalendarAction("‹ 返回", onBack, Modifier.weight(1f))
            CalendarModeSwitcher(
                isClockCalendar = false,
                onSelectMonth = {},
                onSelectClockCalendar = onOpenClockCalendar,
            )
            CalendarAction("今天", onToday, Modifier.weight(1f), alignment = Alignment.CenterEnd)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CalendarText(
                "${month.year}年 · ${month.monthValue}月",
                Modifier.weight(1f),
                HomePalette.Gold,
                24.sp,
            )
            CalendarAction(
                "‹", { onMonthChange(month.minusMonths(1)) },
                description = "上个月", enabled = month != YearMonth.of(-999999999, 1),
            )
            CalendarAction(
                "›", { onMonthChange(month.plusMonths(1)) },
                description = "下个月", enabled = month != YearMonth.of(999999999, 12),
            )
        }
        CalendarText("公历月历 · 点选日期查看农历", color = HomePalette.TextMuted, size = 12.sp)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { weekday ->
                CalendarText(
                    weekday, Modifier.weight(1f), HomePalette.TextMuted,
                    size = 12.sp, align = TextAlign.Center,
                )
            }
        }
        CalendarRule()
        Spacer(Modifier.height(6.dp))
        // Six fixed weeks keep the summary in the same place when switching months.
        repeat(6) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(7) { weekday ->
                    val day = calendar.cells.getOrNull(week * 7 + weekday)
                    CalendarCell(day, selectedDate, today, onSelectDate, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        CalendarRule()
        SelectedDaySummary(selectedDay, onOpenDetails)
        CalendarAction("回到今天", onToday, Modifier.fillMaxWidth(), color = HomePalette.Gold)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CalendarCell(
    day: LunarCalendarDay?,
    selectedDate: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (day == null) {
        Spacer(modifier.heightIn(min = 70.dp))
        return
    }
    val isSelected = day.date == selectedDate
    val isToday = day.date == today
    val shape = RoundedCornerShape(50)
    val outline = if (isSelected) Modifier.border(1.dp, HomePalette.Gold, shape) else Modifier
    val description = listOfNotNull(
        "${day.date.year}年${day.date.monthValue}月${day.date.dayOfMonth}日",
        day.weekday, day.lunarDateText,
        day.eventLabels.takeIf { it.isNotEmpty() }?.joinToString("，"),
        if (isToday) "今天" else null,
    ).joinToString("，")
    Column(
        modifier = modifier
            .heightIn(min = 70.dp)
            .padding(vertical = 2.dp)
            .then(outline)
            .clip(shape)
            .selectable(isSelected, role = Role.Button, onClick = { onSelectDate(day.date) })
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 1.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CalendarText(
                day.date.dayOfMonth.toString(),
                color = if (isSelected) HomePalette.GoldLight else HomePalette.TextBright,
                size = 18.sp,
                maxLines = 1,
            )
            if (isToday) CalendarText("今", color = HomePalette.Gold, size = 9.sp, maxLines = 1)
        }
        CalendarText(day.lunarDayName ?: "—", color = HomePalette.TextMuted, size = 11.sp, maxLines = 1)
        CalendarText(
            day.cellLabel.orEmpty(), Modifier.fillMaxWidth(), HomePalette.Gold,
            size = 10.sp, align = TextAlign.Center, maxLines = 1,
        )
    }
}

@Composable
private fun SelectedDaySummary(day: LunarCalendarDay, onOpenDetails: (LocalDate) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CalendarText(day.lunarDateText ?: "农历资料暂不可用", color = HomePalette.TextBright, size = 22.sp)
                day.lunarYearCaption?.let { CalendarText(it, color = HomePalette.TextMuted, size = 12.sp) }
            }
            CalendarAction("详情 ›", { onOpenDetails(day.date) }, color = HomePalette.Gold)
        }
        CalendarText(
            "${day.date.year}年${day.date.monthValue}月${day.date.dayOfMonth}日 · ${day.weekday}",
            color = HomePalette.TextMuted, size = 12.sp,
        )
        val pillars = listOfNotNull(
            day.yearGanzhi?.let { "${it}年" },
            day.monthGanzhi?.let { "${it}月" },
            day.dayGanzhi?.let { "${it}日" },
        )
        if (pillars.isNotEmpty()) CalendarText(pillars.joinToString("　"), size = 13.sp)
        if (day.eventLabels.isNotEmpty()) {
            CalendarText(day.eventLabels.joinToString(" · "), color = HomePalette.Gold, size = 12.sp)
        }
        if (day.hasFullAlmanac) {
            AlmanacSummaryRow("宜", day.yi, HomePalette.Preview)
            AlmanacSummaryRow("忌", day.ji, Color(0xFFC96F67))
            CalendarText("传统民俗资料，仅供参考", color = HomePalette.TextMuted, size = 11.sp)
        } else {
            CalendarText(
                "该年份未包含完整民俗资料\n完整离线黄历资料覆盖 2025-01-01 至 2028-12-31。",
                color = HomePalette.TextMuted, size = 12.sp,
            )
        }
        Spacer(Modifier.height(5.dp))
    }
}

@Composable
private fun AlmanacSummaryRow(label: String, entries: List<String>, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.background(color.copy(alpha = 0.16f), CircleShape).padding(horizontal = 7.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            CalendarText(label, color = color, size = 13.sp)
        }
        CalendarText(
            entries.takeIf { it.isNotEmpty() }?.joinToString("　") ?: "暂无资料",
            Modifier.weight(1f).padding(top = 3.dp), size = 13.sp,
        )
    }
}

@Composable
private fun CalendarAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    color: Color = HomePalette.TextMuted,
    description: String = text,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = alignment,
    ) {
        CalendarText(text, color = if (enabled) color else HomePalette.TextLow, size = 14.sp)
    }
}

@Composable
private fun CalendarRule() {
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(HomePalette.TextLow.copy(alpha = 0.52f)))
}

@Composable
private fun CalendarText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HomePalette.Text,
    size: TextUnit = 14.sp,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = color, fontFamily = FontFamily.Serif, fontSize = size,
            lineHeight = size * 1.45f, textAlign = align,
        ),
    )
}
