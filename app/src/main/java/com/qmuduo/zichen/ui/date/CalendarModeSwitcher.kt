package com.qmuduo.zichen.ui.date

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuduo.zichen.ui.theme.HomePalette

@Composable
internal fun CalendarModeSwitcher(
    isClockCalendar: Boolean,
    onSelectMonth: () -> Unit,
    onSelectClockCalendar: () -> Unit,
) {
    Row(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarModeOption("月历", !isClockCalendar, onSelectMonth)
        CalendarModeOption("钟历", isClockCalendar, onSelectClockCalendar)
    }
}

@Composable
private fun CalendarModeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val selectedStyle = if (selected) {
        Modifier
            .background(HomePalette.Gold.copy(alpha = 0.10f), shape)
            .border(1.dp, HomePalette.Gold.copy(alpha = 0.5f), shape)
    } else {
        Modifier
    }
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .then(selectedStyle)
            .clip(shape)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = if (selected) HomePalette.GoldLight else HomePalette.TextMuted,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
