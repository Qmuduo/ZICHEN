package com.qmuduo.zichen.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.qmuduo.zichen.time.TraditionalTime
import com.qmuduo.zichen.ui.theme.HomePalette
import java.time.LocalTime
import kotlinx.coroutines.delay

private const val DESIGN_WIDTH = 393f
private const val DESIGN_HEIGHT = 852f

@Composable
fun HomeScreen(
    previewedIndex: Int,
    onPreviewedIndexChange: (Int) -> Unit,
    onOpenTimeDetail: (Int) -> Unit,
    onOpenDate: () -> Unit,
    onOpenTimer: () -> Unit,
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1_000L)
        }
    }

    val actualReading = remember(currentTime) { TraditionalTime.readAt(currentTime) }
    val previewIndex = previewedIndex.takeIf { it >= 0 }
    val dialState = remember(previewIndex) { HomeDialState(previewIndex) }
    val renderState = dialState.renderFor(actualReading.shichen.index)
    val selectedReading = remember(actualReading, previewIndex) {
        if (previewIndex == null) actualReading
        else TraditionalTime.previewAt(previewIndex, currentTime.minute)
    }
    val selectedShichen = TraditionalTime.shichen[renderState.displayIndex]
    val watchReading = remember(currentTime) { TraditionalTime.watchAt(currentTime) }
    val choices = TraditionalTime.shichen.map { shichen ->
        CustomAccessibilityAction("预览${shichen.branch}时 ${shichen.alias}") {
            onPreviewedIndexChange(shichen.index)
            true
        }
    }
    val description = buildString {
        append("十二时辰表盘。此刻${actualReading.branch}时，")
        if (renderState.isPreviewing) append("正在预览${selectedShichen.branch}时，")
        append("${selectedShichen.alias}。盘心只有晷针，读数与诗句在盘外。")
        append("${selectedReading.quarterLabel} ${selectedReading.clockText}，${selectedReading.poem}。")
        append("金色晷针与外弧始终指示此刻。")
    }

    DialCanvas(
        modifier = Modifier
            .fillMaxSize()
            .background(HomePalette.Background)
            .pointerInput(previewedIndex, renderState.displayIndex) {
            detectTapGestures { point: Offset ->
                    val horizontalScale = size.width / DESIGN_WIDTH
                    val verticalScale = size.height / DESIGN_HEIGHT
                    val dialScale = minOf(horizontalScale, verticalScale)
                    val centerX = size.width / 2f
                    val centerY = size.height * 0.36f
                    val radius = 170f * dialScale
                    val selected = DialGeometry.sectorAt(
                        x = point.x,
                        y = point.y,
                        centerX = centerX,
                        centerY = centerY,
                        radius = radius,
                )
                if (selected != null) {
                    onPreviewedIndexChange(selected)
                } else {
                    val ctaWidth = 110f * horizontalScale
                    val ctaHeight = 32f * verticalScale
                    val ctaLeft = (size.width - ctaWidth) / 2f
                    val ctaTop = 695.42f * verticalScale
                    if (previewIndex != null && point.x in ctaLeft..(ctaLeft + ctaWidth) && point.y in ctaTop..(ctaTop + ctaHeight)) {
                        onPreviewedIndexChange(-1)
                        return@detectTapGestures
                    }

                    val entryY = 770.208f * verticalScale
                    val entryHitTop = entryY - 27f * verticalScale
                    val entryHitBottom = entryY + 30f * verticalScale
                    val entryXs = listOf(
                        size.width / 2f - 88f * horizontalScale,
                        size.width / 2f,
                        size.width / 2f + 88f * horizontalScale,
                    )
                    if (point.y in entryHitTop..entryHitBottom) {
                        when {
                            point.x in (entryXs[0] - 34f * horizontalScale)..(entryXs[0] + 34f * horizontalScale) -> onOpenDate()
                            point.x in (entryXs[1] - 38f * horizontalScale)..(entryXs[1] + 38f * horizontalScale) -> onOpenTimeDetail(renderState.displayIndex)
                            point.x in (entryXs[2] - 34f * horizontalScale)..(entryXs[2] + 34f * horizontalScale) -> onOpenTimer()
                        }
                    }
                }
                }
            }
            .semantics {
                contentDescription = description
                stateDescription = if (renderState.isPreviewing) "预览${selectedShichen.branch}时" else "实时"
                onClick(label = "回到此刻") {
                    onPreviewedIndexChange(-1)
                    true
                }
                customActions = choices + CustomAccessibilityAction("回到此刻") {
                    onPreviewedIndexChange(-1)
                    true
                } + CustomAccessibilityAction("打开${selectedShichen.branch}时时辰笺") {
                    onOpenTimeDetail(renderState.displayIndex)
                    true
                } + CustomAccessibilityAction("打开日期详情") {
                    onOpenDate()
                    true
                } + CustomAccessibilityAction("打开仪式计时器") {
                    onOpenTimer()
                    true
                }
            },
        currentIndex = renderState.actualIndex,
        actualTime = actualReading.localTime,
        selectedShichen = selectedShichen,
        selectedReading = selectedReading,
        previewIndex = previewIndex,
        watchReading = watchReading,
    )
}
