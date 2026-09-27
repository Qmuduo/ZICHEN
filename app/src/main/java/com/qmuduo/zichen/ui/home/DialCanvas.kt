package com.qmuduo.zichen.ui.home

import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.qmuduo.zichen.time.Shichen
import com.qmuduo.zichen.time.TimeReading
import com.qmuduo.zichen.time.WatchReading
import com.qmuduo.zichen.ui.theme.HomePalette
import java.time.LocalTime
import kotlin.math.min

private const val BASE_WIDTH = 393f
private const val BASE_HEIGHT = 852f

@androidx.compose.runtime.Composable
internal fun DialCanvas(
    modifier: Modifier,
    currentIndex: Int,
    actualTime: LocalTime,
    selectedShichen: Shichen,
    selectedReading: TimeReading,
    previewIndex: Int?,
    watchReading: WatchReading?,
) {
    Canvas(modifier = modifier) {
        val canvas = drawContext.canvas.nativeCanvas
        val scaleX = size.width / BASE_WIDTH
        val scaleY = size.height / BASE_HEIGHT
        val dialScale = min(scaleX, scaleY)
        val centerX = size.width / 2f
        val centerY = size.height * 0.36f

        canvas.drawColor(HomePalette.Background.toArgb())
        val atmosphere = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(centerX, centerY - 80f * dialScale, size.height.coerceAtLeast(1f) * 0.65f,
                intArrayOf(HomePalette.Surface.toArgb(), HomePalette.Background.toArgb()), null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, size.width, size.height, atmosphere)
        drawShichenDial(
            canvas = canvas,
            centerX = centerX,
            centerY = centerY,
            unit = dialScale,
            actualTime = actualTime,
            currentIndex = currentIndex,
            previewIndex = previewIndex,
        )

        drawCenteredTrackedText(
            canvas = canvas,
            text = "十二时辰",
            centerX = centerX,
            baselineY = 75.828f * scaleY,
            sizePx = 13f * scaleX,
            color = HomePalette.TextMuted.toArgb(),
            letterSpacingPx = 6f * scaleX,
        )
        drawReadout(
            canvas = canvas,
            centerX = centerX,
            scaleX = scaleX,
            scaleY = scaleY,
            shichen = selectedShichen,
            reading = selectedReading,
            isPreviewing = previewIndex != null,
            watchReading = watchReading,
        )
        drawBottomEntries(canvas, centerX, scaleX, scaleY, previewIndex != null)
    }
}

private fun drawReadout(
    canvas: android.graphics.Canvas,
    centerX: Float,
    scaleX: Float,
    scaleY: Float,
    shichen: Shichen,
    reading: TimeReading,
    isPreviewing: Boolean,
    watchReading: WatchReading?,
) {
    val nameColor = if (isPreviewing) HomePalette.Preview.toArgb() else HomePalette.Gold.toArgb()
    drawInlineParts(
        canvas,
        centerX,
        530.796f * scaleY,
        listOf(
            TextPart("${shichen.branch}时", 30f * scaleX, nameColor),
            TextPart(shichen.alias, 12f * scaleX, HomePalette.TextMuted.toArgb(), 12f * scaleX),
        ),
    )

    drawInlineParts(
        canvas,
        centerX,
        563.172f * scaleY,
        listOf(
            TextPart("${reading.quarterLabel} · ", 12f * scaleX, HomePalette.Text.toArgb()),
            TextPart(reading.clockText, 12f * scaleX, HomePalette.TextBright.toArgb(), numeric = true),
        ),
    )
    drawCenteredTrackedText(
        canvas,
        reading.poem,
        centerX,
        598.104f * scaleY,
        12f * scaleX,
        HomePalette.Text.toArgb(),
        2f * scaleX,
    )
    val intervalPaint = serifPaint(12f * scaleX, HomePalette.TextMuted.toArgb(), Paint.Align.CENTER).apply {
        typeface = Typeface.create("monospace", Typeface.NORMAL)
    }
    canvas.drawText(reading.interval, centerX, 639f * scaleY, intervalPaint)
    watchReading?.let {
        drawText(
            canvas,
            "此刻 · ${it.label} · 简化对照",
            centerX,
            664.56f * scaleY,
            11f * scaleX,
            HomePalette.TextMuted.toArgb(),
            Paint.Align.CENTER,
        )
    }
}

private fun drawBottomEntries(
    canvas: android.graphics.Canvas,
    centerX: Float,
    scaleX: Float,
    scaleY: Float,
    isPreviewing: Boolean,
) {
    if (isPreviewing) {
        val width = 110f * scaleX
        val height = 32f * scaleY
        val top = 695.42f * scaleY
        val bounds = RectF(centerX - width / 2f, top, centerX + width / 2f, top + height)
        val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = HomePalette.Preview.copy(alpha = 0.48f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 0.8f * scaleX
        }
        canvas.drawRoundRect(bounds, height / 2f, height / 2f, outline)
        drawReturnIcon(canvas, centerX - 36f * scaleX, top + height / 2f, scaleX)
        drawText(canvas, "回到此刻", centerX + 9f * scaleX, top + height / 2f + 4f * scaleY, 12f * scaleX, HomePalette.Preview.toArgb(), Paint.Align.CENTER)
    }

    val dateX = centerX - 88f * scaleX
    val detailX = centerX
    val timerX = centerX + 88f * scaleX
    val iconY = 770.208f * scaleY
    val surface = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = HomePalette.Surface.copy(alpha = 0.65f).toArgb() }
    val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HomePalette.Rule.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 0.5f * scaleX
    }
    val tray = RectF(centerX - 143f * scaleX, 741f * scaleY, centerX + 143f * scaleX, 811f * scaleY)
    canvas.drawRoundRect(tray, 18f * scaleX, 18f * scaleX, surface)
    canvas.drawRoundRect(tray, 18f * scaleX, 18f * scaleX, edge)
    drawCalendarIcon(canvas, dateX, iconY, scaleX)
    drawBookIcon(canvas, detailX, iconY, scaleX)
    drawHourglassIcon(canvas, timerX, iconY, scaleX)
    drawText(canvas, "日期", dateX, 795.768f * scaleY, 11.5f * scaleX, HomePalette.TextMuted.toArgb(), Paint.Align.CENTER)
    drawText(canvas, "时辰笺", detailX, 795.768f * scaleY, 11.5f * scaleX, HomePalette.TextMuted.toArgb(), Paint.Align.CENTER)
    drawText(canvas, "计时", timerX, 795.768f * scaleY, 11.5f * scaleX, HomePalette.TextMuted.toArgb(), Paint.Align.CENTER)
}

private fun drawCalendarIcon(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    val paint = strokeIconPaint(unit)
    val rect = RectF(centerX - 8f * unit, centerY - 7f * unit, centerX + 8f * unit, centerY + 8f * unit)
    canvas.drawRoundRect(rect, 2f * unit, 2f * unit, paint)
    canvas.drawLine(centerX - 8f * unit, centerY - 2f * unit, centerX + 8f * unit, centerY - 2f * unit, paint)
    canvas.drawLine(centerX - 4f * unit, centerY - 10f * unit, centerX - 4f * unit, centerY - 5f * unit, paint)
    canvas.drawLine(centerX + 4f * unit, centerY - 10f * unit, centerX + 4f * unit, centerY - 5f * unit, paint)
}

private fun drawHourglassIcon(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    val paint = strokeIconPaint(unit)
    canvas.drawLine(centerX - 6f * unit, centerY - 9f * unit, centerX + 6f * unit, centerY - 9f * unit, paint)
    canvas.drawLine(centerX - 6f * unit, centerY + 9f * unit, centerX + 6f * unit, centerY + 9f * unit, paint)
    val path = Path().apply {
        moveTo(centerX - 5f * unit, centerY - 9f * unit)
        lineTo(centerX - 5f * unit, centerY - 5f * unit)
        lineTo(centerX + 5f * unit, centerY + 5f * unit)
        lineTo(centerX + 5f * unit, centerY + 9f * unit)
        moveTo(centerX + 5f * unit, centerY - 9f * unit)
        lineTo(centerX + 5f * unit, centerY - 5f * unit)
        lineTo(centerX - 5f * unit, centerY + 5f * unit)
        lineTo(centerX - 5f * unit, centerY + 9f * unit)
    }
    canvas.drawPath(path, paint)
}

private fun drawBookIcon(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    val paint = strokeIconPaint(unit)
    val path = Path().apply {
        moveTo(centerX, centerY - 7f * unit)
        cubicTo(centerX - 5f * unit, centerY - 10f * unit, centerX - 9f * unit, centerY - 8f * unit, centerX - 9f * unit, centerY - 7f * unit)
        lineTo(centerX - 9f * unit, centerY + 6f * unit)
        cubicTo(centerX - 5f * unit, centerY + 4f * unit, centerX - 2f * unit, centerY + 5f * unit, centerX, centerY + 8f * unit)
        moveTo(centerX, centerY - 7f * unit)
        cubicTo(centerX + 5f * unit, centerY - 10f * unit, centerX + 9f * unit, centerY - 8f * unit, centerX + 9f * unit, centerY - 7f * unit)
        lineTo(centerX + 9f * unit, centerY + 6f * unit)
        cubicTo(centerX + 5f * unit, centerY + 4f * unit, centerX + 2f * unit, centerY + 5f * unit, centerX, centerY + 8f * unit)
    }
    canvas.drawPath(path, paint)
    canvas.drawLine(centerX, centerY - 7f * unit, centerX, centerY + 8f * unit, paint)
}

private fun drawReturnIcon(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    val paint = strokeIconPaint(unit).apply {
        color = HomePalette.Preview.toArgb()
        strokeWidth = 1.1f * unit
    }
    val bounds = RectF(centerX - 5f * unit, centerY - 5f * unit, centerX + 5f * unit, centerY + 5f * unit)
    canvas.drawArc(bounds, 35f, 285f, false, paint)
    canvas.drawLine(centerX - 4f * unit, centerY - 7f * unit, centerX - 4f * unit, centerY - 3f * unit, paint)
    canvas.drawLine(centerX - 4f * unit, centerY - 7f * unit, centerX, centerY - 7f * unit, paint)
}

private fun strokeIconPaint(unit: Float): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = HomePalette.TextMuted.toArgb()
    style = Paint.Style.STROKE
    strokeWidth = 1.1f * unit
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
}

private data class TextPart(
    val text: String,
    val sizePx: Float,
    val color: Int,
    val gapBeforePx: Float = 0f,
    val numeric: Boolean = false,
)

private fun drawInlineParts(
    canvas: android.graphics.Canvas,
    centerX: Float,
    baselineY: Float,
    parts: List<TextPart>,
) {
    val paints = parts.map { part -> serifPaint(part.sizePx, part.color, Paint.Align.LEFT).apply {
        if (part.numeric) typeface = Typeface.create("monospace", Typeface.NORMAL)
    } }
    val totalWidth = parts.indices.sumOf { index ->
        parts[index].gapBeforePx.toDouble() + paints[index].measureText(parts[index].text).toDouble()
    }.toFloat()
    var x = centerX - totalWidth / 2f
    parts.forEachIndexed { index, part ->
        x += part.gapBeforePx
        canvas.drawText(part.text, x, baselineY, paints[index])
        x += paints[index].measureText(part.text)
    }
}

private fun drawCenteredTrackedText(
    canvas: android.graphics.Canvas,
    text: String,
    centerX: Float,
    baselineY: Float,
    sizePx: Float,
    color: Int,
    letterSpacingPx: Float,
) {
    val paint = serifPaint(sizePx, color, Paint.Align.LEFT)
    val characters = text.map(Char::toString)
    val textWidth = characters.sumOf { paint.measureText(it).toDouble() }.toFloat()
    val totalWidth = textWidth + letterSpacingPx * (characters.size - 1).coerceAtLeast(0)
    var x = centerX - totalWidth / 2f
    characters.forEach { character ->
        canvas.drawText(character, x, baselineY, paint)
        x += paint.measureText(character) + letterSpacingPx
    }
}

private fun drawText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    sizePx: Float,
    color: Int,
    align: Paint.Align,
) {
    canvas.drawText(text, x, y, serifPaint(sizePx, color, align))
}

private fun serifPaint(size: Float, color: Int, align: Paint.Align): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = size
    this.color = color
    textAlign = align
    typeface = Typeface.create("serif", Typeface.NORMAL)
}
