package com.qmuduo.zichen.ui.date

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuduo.zichen.calendar.LunarCalendarDay
import com.qmuduo.zichen.calendar.LunarCalendarMonth
import com.qmuduo.zichen.calendar.LunarCalendarProvider
import com.qmuduo.zichen.time.TraditionalTime
import com.qmuduo.zichen.ui.home.ShichenDial
import com.qmuduo.zichen.ui.theme.HomePalette
import com.qmuduo.zichen.ui.theme.inkSurface
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
internal fun ClockCalendarScreen(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    provider: LunarCalendarProvider,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToday: () -> Unit,
    onOpenMonthCalendar: () -> Unit,
) {
    val calendar = remember(provider, month) { provider.getMonth(month) }
    val todayLunar = remember(provider, today) { provider.getDate(today) }
    var actualTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            actualTime = LocalTime.now()
            delay(1_000)
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .inkSurface()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        val landscape = maxWidth > maxHeight
        val landscapeDialSize = minOf(maxWidth * 0.42f, maxHeight - 16.dp)
            .coerceIn(112.dp, 360.dp)
        val portraitDialSize = minOf(maxWidth * 0.86f, maxHeight * 0.40f)
            .coerceAtMost(320.dp)
        val compactCellHeight = ((maxHeight - 104.dp) / 6).coerceIn(24.dp, 32.dp)
        Column(Modifier.fillMaxSize()) {
            if (!landscape) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CalendarModeSwitcher(
                        isClockCalendar = true,
                        onSelectMonth = onOpenMonthCalendar,
                        onSelectClockCalendar = {},
                    )
                    Spacer(Modifier.weight(1f))
                    ClockCalendarAction("今天", onToday)
                }
                Spacer(Modifier.height(8.dp))
            }
            if (landscape) {
                Row(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        Modifier
                            .weight(0.52f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ShichenDial(Modifier.size(landscapeDialSize), actualTime)
                    }
                    Column(
                        Modifier
                            .weight(0.48f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            ClockCalendarTimeReadout(actualTime, compact = true)
                            Spacer(Modifier.weight(1f))
                            ClockCalendarAction("今天", onToday, compact = true)
                        }
                        ClockCalendarMonthGrid(
                            calendar = calendar,
                            todayLunar = todayLunar,
                            selectedDate = selectedDate,
                            onMonthChange = onMonthChange,
                            onSelectDate = onSelectDate,
                            onOpenMonthCalendar = onOpenMonthCalendar,
                            compact = true,
                            compactCellHeight = compactCellHeight,
                        )
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        ShichenDial(Modifier.size(portraitDialSize), actualTime)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        ClockCalendarTimeReadout(actualTime)
                    }
                    Spacer(Modifier.height(12.dp))
                    ClockCalendarMonthGrid(
                        calendar = calendar,
                        todayLunar = todayLunar,
                        selectedDate = selectedDate,
                        onMonthChange = onMonthChange,
                        onSelectDate = onSelectDate,
                        onOpenMonthCalendar = onOpenMonthCalendar,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClockCalendarTimeReadout(actualTime: LocalTime, compact: Boolean = false) {
    val shichen = TraditionalTime.readAt(actualTime).shichen
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    if (compact) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClockCalendarText("${shichen.branch}时 · ${shichen.alias}", color = HomePalette.Gold, size = 12.sp)
            ClockCalendarText(actualTime.format(formatter), color = HomePalette.TextBright, size = 12.sp)
        }
        return
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ClockCalendarText("${shichen.branch}时 · ${shichen.alias}", color = HomePalette.Gold, size = 20.sp)
        ClockCalendarText(actualTime.format(formatter), color = HomePalette.TextBright, size = 18.sp)
    }
}

@Composable
private fun ClockCalendarMonthGrid(
    calendar: LunarCalendarMonth,
    todayLunar: LunarCalendarDay,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onOpenMonthCalendar: () -> Unit,
    compact: Boolean = false,
    compactCellHeight: Dp = 32.dp,
) {
    val month = calendar.month
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (compact) {
            CalendarModeSwitcher(
                isClockCalendar = true,
                onSelectMonth = onOpenMonthCalendar,
                onSelectClockCalendar = {},
                compact = true,
            )
        }
        ClockCalendarText(
            "${month.year}年 · ${month.monthValue}月",
            Modifier.weight(1f),
            HomePalette.TextBright,
            if (compact) 18.sp else 22.sp,
        )
        ClockCalendarAction(
            "‹", { onMonthChange(month.minusMonths(1)) },
            description = "上个月", enabled = month != YearMonth.of(-999999999, 1),
            compact = compact,
        )
        ClockCalendarAction(
            "›", { onMonthChange(month.plusMonths(1)) },
            description = "下个月", enabled = month != YearMonth.of(999999999, 12),
            compact = compact,
        )
    }
    val lunarCaption = todayLunar.lunarDateText
        ?.takeIf { todayLunar.hasLunarData && it.isNotBlank() }
        ?.let { "今日农历 · $it" }
        ?: "农历信息暂不可用"
    ClockCalendarText(lunarCaption, color = HomePalette.TextMuted, size = if (compact) 10.sp else 12.sp)
    Spacer(Modifier.height(if (compact) 2.dp else 12.dp))
    Row(Modifier.fillMaxWidth().padding(bottom = if (compact) 0.dp else 8.dp)) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { weekday ->
            ClockCalendarText(
                weekday, Modifier.weight(1f), HomePalette.TextMuted,
                size = if (compact) 10.sp else 12.sp, align = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(HomePalette.Rule))
    Spacer(Modifier.height(if (compact) 2.dp else 4.dp))
    calendar.cells.chunked(7).filter { week -> week.any { it != null } }.forEach { week ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(7) { weekday ->
                Box(Modifier.weight(1f)) {
                    ClockCalendarDayCell(
                        week.getOrNull(weekday), selectedDate, onSelectDate, compact, compactCellHeight,
                    )
                }
            }
        }
    }
    if (!compact) Spacer(Modifier.height(8.dp))
}

@Composable
private fun ClockCalendarDayCell(
    day: LunarCalendarDay?,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    compact: Boolean,
    compactCellHeight: Dp,
) {
    if (day == null) {
        Spacer(Modifier.fillMaxWidth().height(if (compact) compactCellHeight else 48.dp))
        return
    }
    val isSelected = day.date == selectedDate
    val shape = RoundedCornerShape(12.dp)
    val selectedStyle = if (isSelected) {
        Modifier
            .background(HomePalette.Gold.copy(alpha = 0.10f), shape)
            .border(0.5.dp, HomePalette.Gold.copy(alpha = 0.40f), shape)
    } else {
        Modifier
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) compactCellHeight else 48.dp)
            .padding(vertical = if (compact) 1.dp else 2.dp)
            .then(selectedStyle)
            .clip(shape)
            .selectable(isSelected, role = Role.Button, onClick = { onSelectDate(day.date) })
            .semantics(mergeDescendants = true) {
                contentDescription = "${day.date.year}年${day.date.monthValue}月${day.date.dayOfMonth}日"
            }
            .padding(vertical = if (compact) 2.dp else 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        ClockCalendarText(
            day.date.dayOfMonth.toString(),
            color = if (isSelected) HomePalette.GoldLight else HomePalette.TextBright,
            size = if (compact) 15.sp else 18.sp,
        )
    }
}

@Composable
private fun ClockCalendarAction(
    text: String,
    onClick: () -> Unit,
    description: String = text,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    Box(
        Modifier
            .sizeIn(
                minWidth = if (compact) 34.dp else 48.dp,
                minHeight = if (compact) 34.dp else 48.dp,
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(if (compact) 4.dp else 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        ClockCalendarText(text, color = if (enabled) HomePalette.TextMuted else HomePalette.TextLow)
    }
}

@Composable
private fun ClockCalendarText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HomePalette.Text,
    size: TextUnit = 14.sp,
    align: TextAlign = TextAlign.Start,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontFamily = if (text.all { it.isDigit() || it == ':' }) FontFamily.Monospace else FontFamily.Serif,
            fontSize = size,
            lineHeight = size * 1.45f,
            textAlign = align,
        ),
    )
}
