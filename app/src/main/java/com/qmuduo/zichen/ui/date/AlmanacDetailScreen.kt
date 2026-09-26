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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuduo.zichen.calendar.LunarCalendarDay
import com.qmuduo.zichen.ui.theme.HomePalette

@Composable
fun AlmanacDetailScreen(day: LunarCalendarDay, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(HomePalette.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Button, onClick = onBack)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                AlmanacText("‹ 返回月历", size = 14.sp)
            }
            AlmanacText(
                "日期详情", Modifier.weight(1f), HomePalette.TextBright,
                size = 16.sp, align = TextAlign.End,
            )
        }
        Spacer(Modifier.height(16.dp))
        AlmanacText(
            "${day.date.year}年${day.date.monthValue}月${day.date.dayOfMonth}日 · ${day.weekday}",
            size = 13.sp,
        )
        day.lunarYearCaption?.let {
            AlmanacText(it, Modifier.padding(top = 6.dp), HomePalette.Gold, 14.sp)
        }
        if (day.eventLabels.isNotEmpty()) {
            AlmanacText(
                day.eventLabels.joinToString(" · "), Modifier.padding(top = 6.dp),
                HomePalette.Gold, 12.sp,
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .border(1.dp, HomePalette.TextLow.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AlmanacDateRail(day, Modifier.weight(0.28f))
            Column(Modifier.weight(0.72f)) {
                if (day.hasFullAlmanac) {
                    AlmanacField("五行", day.nayin)
                    AlmanacField(
                        "冲煞",
                        listOfNotNull(day.clash, day.shaDirection?.let { "煞$it" })
                            .joinToString("　"),
                    )
                    AlmanacField("彭祖", listOfNotNull(day.pengzuGan, day.pengzuZhi).joinToString("\n"))
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf("喜神" to day.xiDirection, "福神" to day.fuDirection, "财神" to day.caiDirection)
                            .forEach { (label, direction) ->
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    AlmanacText(label, color = HomePalette.TextBright, align = TextAlign.Center)
                                    AlmanacText(
                                        direction ?: "暂无资料", Modifier.padding(top = 6.dp),
                                        size = 12.sp, align = TextAlign.Center,
                                    )
                                }
                            }
                    }
                    AlmanacRule()
                    AlmanacField("宜", day.yi.joinToString("　"), HomePalette.Preview)
                    AlmanacField("忌", day.ji.joinToString("　"), AlmanacMutedRed)
                    AlmanacField("吉神", day.auspiciousDeities.joinToString("　"), HomePalette.Gold)
                    AlmanacField("凶神", day.inauspiciousDeities.joinToString("　"), AlmanacMutedRed)
                } else {
                    AlmanacText(
                        "该年份未包含完整民俗资料",
                        Modifier.padding(vertical = 12.dp), HomePalette.TextBright, 15.sp,
                    )
                    AlmanacRule()
                    AlmanacText(
                        "完整黄历资料覆盖 2025—2028 年。",
                        Modifier.padding(top = 12.dp), size = 13.sp,
                    )
                    if (!day.hasLunarData) {
                        AlmanacText("该日期暂无可靠农历换算资料。", Modifier.padding(top = 12.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        AlmanacText("传统民俗资料，仅供参考", color = HomePalette.TextMuted, size = 12.sp)
        AlmanacText(
            day.sourceDescription, Modifier.padding(top = 8.dp, bottom = 28.dp),
            HomePalette.TextMuted, 11.sp,
        )
    }
}

@Composable
private fun AlmanacDateRail(day: LunarCalendarDay, modifier: Modifier = Modifier) {
    val lunarDate = day.lunarDateText?.let { "农历$it" } ?: "公历"
    val pillars = listOfNotNull(
        day.yearGanzhi?.let { "${it}年" },
        day.monthGanzhi?.let { "${it}月" },
        day.dayGanzhi?.let { "${it}日" },
    )
    Row(
        modifier
            .drawBehind {
                drawLine(
                    HomePalette.TextLow.copy(alpha = 0.5f),
                    Offset(size.width, 0f), Offset(size.width, size.height), 1.dp.toPx(),
                )
            }
            .padding(top = 16.dp, end = 10.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AlmanacText(
            lunarDate.toList().joinToString("\n"),
            Modifier.weight(0.6f).clearAndSetSemantics { contentDescription = lunarDate },
            HomePalette.TextBright, 23.sp, TextAlign.Center,
        )
        if (pillars.isNotEmpty()) {
            AlmanacText(
                pillars.joinToString("\n\n") { it.toList().joinToString("\n") },
                Modifier.weight(0.4f).clearAndSetSemantics {
                    contentDescription = "干支：${pillars.joinToString("，")}"
                },
                HomePalette.TextMuted, 12.sp, TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AlmanacField(label: String, value: String?, labelColor: Color = HomePalette.TextBright) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AlmanacText(label, Modifier.weight(0.27f), labelColor)
        AlmanacText(value?.takeIf { it.isNotBlank() } ?: "暂无资料", Modifier.weight(0.73f))
    }
    AlmanacRule()
}

@Composable
private fun AlmanacRule() {
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(HomePalette.TextLow.copy(alpha = 0.35f)))
}

@Composable
private fun AlmanacText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HomePalette.Text,
    size: TextUnit = 13.sp,
    align: TextAlign = TextAlign.Start,
) {
    BasicText(
        text,
        modifier,
        style = TextStyle(
            color = color,
            fontFamily = FontFamily.Serif,
            fontSize = size,
            lineHeight = size * 1.65f,
            textAlign = align,
        ),
    )
}

private val AlmanacMutedRed = Color(0xFFC96F67)
