package com.qmuduo.zichen.ui.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qmuduo.zichen.timer.TimerPreset
import com.qmuduo.zichen.timer.TimerState
import com.qmuduo.zichen.timer.TimerStatus
import com.qmuduo.zichen.ui.theme.HomePalette
import java.util.Locale

@Composable
fun TimerScreen(
    state: TimerState,
    nowEpochMillis: Long,
    exactAlarmAllowed: Boolean,
    notificationsAllowed: Boolean,
    onStart: (TimerPreset) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onSelectPreset: (TimerPreset) -> Unit,
    onToggleSound: () -> Unit,
    onRequestReminderPermissions: () -> Unit,
    onBack: () -> Unit,
) {
    var showReminderExplanation by rememberSaveable { mutableStateOf(false) }
    val remaining = state.remainingAt(nowEpochMillis)
    val remainingRatio = remember(state, remaining) {
        remaining.toFloat() / state.preset.durationMillis.toFloat()
    }.coerceIn(0f, 1f)
    val statusText = when (state.status) {
        TimerStatus.READY -> "静候开始"
        TimerStatus.RUNNING -> "计时中"
        TimerStatus.PAUSED -> "暂歇"
        TimerStatus.FINISHED -> "计时已毕"
    }
    val mainAction = when (state.status) {
        TimerStatus.READY -> "开始"
        TimerStatus.RUNNING -> "暂停"
        TimerStatus.PAUSED -> "继续"
        TimerStatus.FINISHED -> "再计一次"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomePalette.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                text = "仪式计时",
                style = TextStyle(color = HomePalette.TextMuted, fontSize = 13.sp, letterSpacing = 4.sp, fontFamily = FontFamily.Serif),
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.padding(horizontal = 27.dp))
        }

        Spacer(Modifier.height(18.dp))
        BasicText(
            text = statusText,
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 13.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(Modifier.height(15.dp))
        Sandglass(remainingRatio = remainingRatio, isRunning = state.status == TimerStatus.RUNNING)
        Spacer(Modifier.height(7.dp))
        BasicText(
            text = formatRemaining(remaining),
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                color = HomePalette.Gold,
                fontFamily = FontFamily.Serif,
                fontSize = 48.sp,
                lineHeight = 58.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
            ),
        )
        Spacer(Modifier.height(5.dp))
        BasicText(
            text = "${state.preset.title} · ${state.preset.durationMillis / 60_000} 分钟",
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Serif),
        )

        Spacer(Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TimerPreset.entries.forEach { preset ->
                val selected = preset == state.preset
                val canSelect = state.status == TimerStatus.READY || state.status == TimerStatus.FINISHED
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) HomePalette.Gold.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable(enabled = canSelect) { onSelectPreset(preset) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BasicText(
                        text = preset.title,
                        style = TextStyle(color = if (selected) HomePalette.GoldLight else HomePalette.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Serif),
                    )
                    Spacer(Modifier.height(5.dp))
                    BasicText(
                        text = "${preset.durationMillis / 60_000} 分钟",
                        style = TextStyle(color = HomePalette.TextLow, fontSize = 10.sp, fontFamily = FontFamily.Serif),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionBox(
                label = mainAction,
                primary = true,
                modifier = Modifier.weight(1f),
                onClick = {
                    when (state.status) {
                        TimerStatus.READY -> if (state.reminderExplainerSeen) onStart(state.preset) else showReminderExplanation = true
                        TimerStatus.RUNNING -> onPause()
                        TimerStatus.PAUSED -> onResume()
                        TimerStatus.FINISHED -> onStart(state.preset)
                    }
                },
            )
            ActionBox(label = "重置", primary = false, modifier = Modifier.weight(1f), onClick = onReset)
        }

        Spacer(Modifier.height(24.dp))
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(HomePalette.TextLow.copy(alpha = 0.52f)))
        Spacer(Modifier.height(17.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleSound)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "结束时播放钟磬",
                style = TextStyle(color = HomePalette.Text, fontSize = 13.sp, fontFamily = FontFamily.Serif),
            )
            Spacer(Modifier.weight(1f))
            BasicText(
                text = if (state.soundEnabled) "已开启" else "已静音",
                style = TextStyle(color = if (state.soundEnabled) HomePalette.GoldLight else HomePalette.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Serif),
            )
        }
        Spacer(Modifier.height(7.dp))
        BasicText(
            text = when {
                !notificationsAllowed -> "系统通知未开启；完成状态仍会保存在 App 内。"
                !exactAlarmAllowed -> "精确提醒尚不可用，系统通知可能延迟。"
                else -> "计时结束时${if (state.soundEnabled) "播放一次钟磬" else "静音通知"}。"
            },
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 11.sp, lineHeight = 18.sp, fontFamily = FontFamily.Serif),
        )
        if (!notificationsAllowed || !exactAlarmAllowed) {
            Spacer(Modifier.height(9.dp))
            BasicText(
                text = "设置系统提醒  ›",
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onRequestReminderPermissions)
                    .padding(horizontal = 5.dp, vertical = 7.dp),
                style = TextStyle(color = HomePalette.Preview, fontSize = 12.sp, fontFamily = FontFamily.Serif),
            )
        }
        Spacer(Modifier.height(30.dp))
    }

    if (showReminderExplanation) {
        ReminderExplanation(
            onDismiss = { showReminderExplanation = false },
            onStartWithoutSystemReminder = {
                showReminderExplanation = false
                onStart(state.preset)
            },
            onStartAndSetReminder = {
                showReminderExplanation = false
                onStart(state.preset)
                onRequestReminderPermissions()
            },
        )
    }
}

@Composable
private fun ActionBox(label: String, primary: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (primary) HomePalette.Gold.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = if (primary) HomePalette.GoldLight else HomePalette.TextMuted,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
            ),
        )
    }
}

@Composable
private fun Sandglass(remainingRatio: Float, isRunning: Boolean) {
    Canvas(Modifier.size(width = 72.dp, height = 100.dp)) {
        val cx = size.width / 2f
        val top = size.height * 0.08f
        val neckTop = size.height * 0.45f
        val neckBottom = size.height * 0.55f
        val bottom = size.height * 0.92f
        val outer = size.width * 0.32f
        val inner = size.width * 0.04f
        val stroke = HomePalette.TextMuted.copy(alpha = 0.82f)

        drawLine(stroke, androidx.compose.ui.geometry.Offset(cx - outer, top), androidx.compose.ui.geometry.Offset(cx + outer, top), 1.2.dp.toPx())
        drawLine(stroke, androidx.compose.ui.geometry.Offset(cx - outer, bottom), androidx.compose.ui.geometry.Offset(cx + outer, bottom), 1.2.dp.toPx())
        val outline = Path().apply {
            moveTo(cx - outer, top)
            cubicTo(cx - outer, top + size.height * 0.11f, cx - inner, neckTop - size.height * 0.05f, cx - inner, neckTop)
            lineTo(cx - inner, neckBottom)
            cubicTo(cx - inner, neckBottom + size.height * 0.05f, cx - outer, bottom - size.height * 0.11f, cx - outer, bottom)
            moveTo(cx + outer, top)
            cubicTo(cx + outer, top + size.height * 0.11f, cx + inner, neckTop - size.height * 0.05f, cx + inner, neckTop)
            lineTo(cx + inner, neckBottom)
            cubicTo(cx + inner, neckBottom + size.height * 0.05f, cx + outer, bottom - size.height * 0.11f, cx + outer, bottom)
        }
        drawPath(outline, stroke, style = Stroke(width = 1.2.dp.toPx()))

        val sand = HomePalette.Gold.copy(alpha = 0.72f)
        val topFill = Path().apply {
            val sandLevel = neckTop - remainingRatio * size.height * 0.27f
            moveTo(cx - outer * 0.78f, sandLevel)
            lineTo(cx + outer * 0.78f, sandLevel)
            lineTo(cx, neckTop - size.height * 0.025f)
            close()
        }
        drawPath(topFill, sand)
        val bottomAmount = 1f - remainingRatio
        if (bottomAmount > 0.015f) {
            val fillTop = bottom - bottomAmount * size.height * 0.29f
            val halfTop = outer * (0.22f + bottomAmount * 0.58f)
            val bottomFill = Path().apply {
                moveTo(cx - halfTop, fillTop)
                lineTo(cx + halfTop, fillTop)
                lineTo(cx + outer * 0.78f, bottom - size.height * 0.035f)
                lineTo(cx - outer * 0.78f, bottom - size.height * 0.035f)
                close()
            }
            drawPath(bottomFill, sand)
        }
        if (isRunning) {
            drawLine(sand, androidx.compose.ui.geometry.Offset(cx, neckTop), androidx.compose.ui.geometry.Offset(cx, neckBottom), 1.2.dp.toPx())
        }
    }
}

@Composable
private fun ReminderExplanation(
    onDismiss: () -> Unit,
    onStartWithoutSystemReminder: () -> Unit,
    onStartAndSetReminder: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HomePalette.Background)
                .padding(24.dp),
        ) {
            BasicText(
                text = "让计时在后台继续",
                style = TextStyle(color = HomePalette.GoldLight, fontSize = 18.sp, fontFamily = FontFamily.Serif),
            )
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = "开启系统通知后，计时结束时可以收到提醒。准点提醒需要系统的“闹钟和提醒”权限；未授权时计时仍会继续，但通知可能延迟。",
                style = TextStyle(color = HomePalette.Text, fontSize = 13.sp, lineHeight = 22.sp, fontFamily = FontFamily.Serif),
            )
            Spacer(Modifier.height(21.dp))
            ActionBox(
                label = "开始并设置提醒",
                primary = true,
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartAndSetReminder,
            )
            Spacer(Modifier.height(7.dp))
            BasicText(
                text = "仅开始计时",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onStartWithoutSystemReminder)
                    .padding(vertical = 15.dp),
                style = TextStyle(color = HomePalette.TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center, fontFamily = FontFamily.Serif),
            )
        }
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = (millis + 999L) / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}
