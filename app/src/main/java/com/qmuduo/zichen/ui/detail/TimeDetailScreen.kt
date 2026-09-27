package com.qmuduo.zichen.ui.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuduo.zichen.time.Shichen
import com.qmuduo.zichen.time.TimeDetailContents
import com.qmuduo.zichen.ui.theme.HomePalette
import com.qmuduo.zichen.ui.theme.inkSurface

@Composable
fun TimeDetailScreen(shichen: Shichen, onBack: () -> Unit) {
    val content = remember(shichen.index) { TimeDetailContents.forShichen(shichen.index) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .inkSurface()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            BasicText(
                text = "‹  返回",
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                style = TextStyle(color = HomePalette.TextMuted, fontSize = 14.sp, fontFamily = FontFamily.Serif),
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicText(
                text = "时辰笺",
                style = TextStyle(
                    color = HomePalette.TextMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(horizontal = 27.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        BasicText(
            text = "${shichen.branch}时",
            style = TextStyle(
                color = HomePalette.Gold,
                fontFamily = FontFamily.Serif,
                fontSize = 48.sp,
                lineHeight = 62.sp,
                fontWeight = FontWeight.Light,
            ),
        )
        Spacer(modifier = Modifier.height(5.dp))
        BasicText(
            text = shichen.alias,
            style = TextStyle(color = HomePalette.TextBright, fontSize = 20.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(modifier = Modifier.height(12.dp))
        BasicText(
            text = "${shichen.interval}    ·    ${shichen.branch}支    ·    ${content.zodiac}肖",
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(modifier = Modifier.height(28.dp))
        Rule()
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeading("时辰意象")
        Spacer(modifier = Modifier.height(10.dp))
        BasicText(
            text = content.impression,
            style = TextStyle(
                color = HomePalette.TextBright,
                fontSize = 18.sp,
                lineHeight = 31.sp,
                fontFamily = FontFamily.Serif,
            ),
        )

        Spacer(modifier = Modifier.height(28.dp))
        Rule()
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeading("诗词")
        Spacer(modifier = Modifier.height(11.dp))
        BasicText(
            text = content.poemTitle,
            style = TextStyle(color = HomePalette.GoldLight, fontSize = 16.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(modifier = Modifier.height(5.dp))
        BasicText(
            text = content.poet,
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(modifier = Modifier.height(17.dp))
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            content.poemLines.forEach { line ->
                BasicText(
                    text = line,
                    style = TextStyle(
                        color = HomePalette.TextBright,
                        fontSize = 16.sp,
                        lineHeight = 29.sp,
                        fontFamily = FontFamily.Serif,
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BasicText(
            text = "出处  ${content.source}",
            style = TextStyle(color = HomePalette.TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Serif),
        )
        Spacer(modifier = Modifier.height(28.dp))
        Rule()
        Spacer(modifier = Modifier.height(14.dp))
        BasicText(
            text = "本页诗句按意境配于时辰，不表示作品专属于此时。",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                color = HomePalette.TextLow,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeading(text: String) {
    BasicText(
        text = text,
        style = TextStyle(
            color = HomePalette.TextMuted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 2.sp,
        ),
    )
}

@Composable
private fun Rule() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(HomePalette.Rule),
    )
}
