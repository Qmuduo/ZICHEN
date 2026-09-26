package com.qmuduo.zichen.ui.date

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuduo.zichen.date.LocalDateInfoProvider
import com.qmuduo.zichen.ui.theme.HomePalette
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay

@Composable
fun DateDetailScreen(onBack: () -> Unit) {
    var now by remember { mutableStateOf(Instant.now()) }
    var zone by remember { mutableStateOf(ZoneId.systemDefault()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Instant.now()
            zone = ZoneId.systemDefault()
            delay(30_000L)
        }
    }

    val info = remember(now, zone) { LocalDateInfoProvider().getDateInfo(now, zone) }
    val weekday = listOf("一", "二", "三", "四", "五", "六", "日")[info.localDate.dayOfWeek.value - 1]
    val solarTerm = info.currentSolarTerm

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomePalette.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "‹  返回",
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                style = TextStyle(color = HomePalette.TextMuted, fontSize = 14.sp, fontFamily = FontFamily.Serif),
            )
            Spacer(Modifier.weight(1f))
            BasicText(
                text = "日期",
                style = TextStyle(
                    color = HomePalette.TextMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.padding(horizontal = 27.dp))
        }

        Spacer(Modifier.height(40.dp))
        BasicText(
            text = "${info.localDate.year}年  ${info.localDate.monthValue}月${info.localDate.dayOfMonth}日  星期$weekday",
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 13.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(Modifier.height(32.dp))
        BasicText(
            text = "${info.dayGanzhi}日",
            style = TextStyle(
                color = HomePalette.Gold,
                fontFamily = FontFamily.Serif,
                fontSize = 46.sp,
                lineHeight = 58.sp,
                fontWeight = FontWeight.Light,
            ),
        )
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = "今日干支",
            style = TextStyle(color = HomePalette.TextBright, fontSize = 17.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(Modifier.height(9.dp))
        BasicText(
            text = "按设备所在时区的公历日期推算，本地 00:00 换日。",
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 12.sp, lineHeight = 21.sp, fontFamily = FontFamily.Serif),
        )

        Spacer(Modifier.height(40.dp))
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(HomePalette.TextLow.copy(alpha = 0.52f)))
        Spacer(Modifier.height(27.dp))
        BasicText(
            text = "此时节气",
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 12.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(Modifier.height(12.dp))
        BasicText(
            text = solarTerm?.name ?: "资料暂不可用",
            style = TextStyle(
                color = if (solarTerm == null) HomePalette.TextMuted else HomePalette.GoldLight,
                fontSize = 25.sp,
                lineHeight = 34.sp,
                fontFamily = FontFamily.Serif,
            ),
        )
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = if (solarTerm == null) {
                "内置节气时刻表暂未覆盖这一天；今日干支仍可正常查看。"
            } else {
                val localTime = solarTerm.instant.atZone(zone)
                "交节于 ${localTime.monthValue}月${localTime.dayOfMonth}日 ${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}（当地时间）"
            },
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 12.sp, lineHeight = 21.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(Modifier.height(32.dp))
        BasicText(
            text = "节气采用离线历表，时刻按原始资料时区转换后读取。",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                color = HomePalette.TextLow,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
            ),
        )
        Spacer(Modifier.height(32.dp))
    }
}
