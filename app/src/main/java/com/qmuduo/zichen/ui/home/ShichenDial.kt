package com.qmuduo.zichen.ui.home

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.qmuduo.zichen.time.TraditionalTime
import com.qmuduo.zichen.ui.theme.HomePalette
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

// Includes breathing room outside the 170-unit current-time arc.
private const val DIAL_BASE_SIZE = 360f

@Composable
internal fun ShichenDial(
    modifier: Modifier,
    actualTime: LocalTime,
) {
    Canvas(modifier = modifier) {
        drawShichenDial(
            canvas = drawContext.canvas.nativeCanvas,
            centerX = size.width / 2f,
            centerY = size.height / 2f,
            unit = size.minDimension / DIAL_BASE_SIZE,
            actualTime = actualTime,
            currentIndex = TraditionalTime.readAt(actualTime).shichen.index,
        )
    }
}

internal fun drawShichenDial(
    canvas: android.graphics.Canvas,
    centerX: Float,
    centerY: Float,
    unit: Float,
    actualTime: LocalTime,
    currentIndex: Int,
    previewIndex: Int? = null,
) {
    drawPreviewSector(canvas, centerX, centerY, unit, previewIndex)
    drawHourTicks(canvas, centerX, centerY, unit)
    drawInitialAndZheng(canvas, centerX, centerY, unit)
    drawBranches(canvas, centerX, centerY, unit, currentIndex, previewIndex)
    drawStems(canvas, centerX, centerY, unit)
    drawGnomon(canvas, centerX, centerY, unit, DialGeometry.pointerRotationDegrees(actualTime))
    drawCurrentArc(canvas, centerX, centerY, unit, currentIndex)
}

private fun drawPreviewSector(
    canvas: android.graphics.Canvas,
    centerX: Float,
    centerY: Float,
    unit: Float,
    previewIndex: Int?,
) {
    if (previewIndex == null) return

    val middleAngle = -90f + previewIndex * 30f
    val startAngle = middleAngle - 12.5f
    val outer = 164f * unit
    val inner = 108f * unit
    val outerBounds = RectF(centerX - outer, centerY - outer, centerX + outer, centerY + outer)
    val innerBounds = RectF(centerX - inner, centerY - inner, centerX + inner, centerY + inner)
    val path = Path().apply {
        val first = pointOnCircle(centerX, centerY, outer, startAngle)
        moveTo(first.x, first.y)
        arcTo(outerBounds, startAngle, 25f, false)
        val innerEnd = pointOnCircle(centerX, centerY, inner, startAngle + 25f)
        lineTo(innerEnd.x, innerEnd.y)
        arcTo(innerBounds, startAngle + 25f, -25f, false)
        close()
    }
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HomePalette.Preview.copy(alpha = 0.20f).toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawPath(path, fill)
    val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HomePalette.Preview.copy(alpha = 0.5f).toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 0.7f * unit
    }
    canvas.drawPath(path, outline)
}

private fun drawHourTicks(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    for (tick in 0 until 96) {
        val isShichenBoundary = tick % 8 == 0
        val isHalfHourBoundary = tick % 4 == 0
        val innerRadius = when {
            isShichenBoundary -> 143f
            isHalfHourBoundary -> 148f
            else -> 152f
        } * unit
        val outerRadius = 164f * unit
        val angle = -105f + tick * 3.75f
        val start = pointOnCircle(centerX, centerY, innerRadius, angle)
        val end = pointOnCircle(centerX, centerY, outerRadius, angle)
        val color = when {
            isShichenBoundary -> 0xFFB5AE9E.toInt()
            isHalfHourBoundary -> 0xFF8E8879.toInt()
            else -> 0xFF5E5A53.toInt()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = when {
                isShichenBoundary -> 1.8f
                isHalfHourBoundary -> 1.6f
                else -> 1.3f
            } * unit
            strokeCap = Paint.Cap.BUTT
        }
        canvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }
}

private fun drawInitialAndZheng(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    val paint = serifPaint(size = 11f * unit, color = HomePalette.TextMuted.toArgb(), align = Paint.Align.CENTER)
    for (position in 0 until 24) {
        val angle = -97.5f + position * 15f
        val point = pointOnCircle(centerX, centerY, 140f * unit, angle)
        val text = if (position % 2 == 0) "初" else "正"
        drawOutlinedText(canvas, text, point.x, point.y + paint.textSize * 0.36f, paint, unit)
    }
}

private fun drawBranches(
    canvas: android.graphics.Canvas,
    centerX: Float,
    centerY: Float,
    unit: Float,
    currentIndex: Int,
    previewIndex: Int?,
) {
    ShichenOrder.forEachIndexed { index, branch ->
        val point = pointOnCircle(centerX, centerY, 120f * unit, -90f + index * 30f)
        val color = when {
            previewIndex == index -> HomePalette.Preview.toArgb()
            currentIndex == index -> HomePalette.Gold.toArgb()
            else -> 0xFFCFC9BB.toInt()
        }
        val paint = serifPaint(size = 14f * unit, color = color, align = Paint.Align.CENTER)
        drawOutlinedText(canvas, branch, point.x, point.y + paint.textSize * 0.36f, paint, unit)
    }
}

private val ShichenOrder = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

private fun drawStems(canvas: android.graphics.Canvas, centerX: Float, centerY: Float, unit: Float) {
    val stems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    val paint = serifPaint(size = 11f * unit, color = HomePalette.TextMuted.toArgb(), align = Paint.Align.CENTER)
    stems.forEachIndexed { index, stem ->
        val point = pointOnCircle(centerX, centerY, 99f * unit, -90f + index * 36f)
        drawOutlinedText(canvas, stem, point.x, point.y + paint.textSize * 0.36f, paint, unit)
    }
}

private fun drawGnomon(
    canvas: android.graphics.Canvas,
    centerX: Float,
    centerY: Float,
    unit: Float,
    rotationDegrees: Float,
) {
    canvas.save()
    canvas.translate(centerX, centerY)
    canvas.rotate(rotationDegrees)
    val needle = Path().apply {
        moveTo(-2.4f * unit, -7.2f * unit)
        lineTo(0f, -88f * unit)
        lineTo(2.4f * unit, -7.2f * unit)
        close()
    }
    val beam = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            -7.2f * unit,
            0f,
            -88f * unit,
            intArrayOf(
                HomePalette.Gold.copy(alpha = 0f).toArgb(),
                HomePalette.Gold.copy(alpha = 0.35f).toArgb(),
                HomePalette.Gold.copy(alpha = 0.7f).toArgb(),
                HomePalette.GoldLight.toArgb(),
            ),
            floatArrayOf(0f, 0.12f, 0.85f, 1f),
            Shader.TileMode.CLAMP,
        )
        style = Paint.Style.FILL
    }
    canvas.drawPath(needle, beam)
    canvas.restore()

    val hubRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HomePalette.Gold.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 1.4f * unit
    }
    canvas.drawCircle(centerX, centerY, 7.2f * unit, hubRing)
    val hub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = HomePalette.Gold.toArgb() }
    canvas.drawCircle(centerX, centerY, 2.2f * unit, hub)
}

private fun drawCurrentArc(
    canvas: android.graphics.Canvas,
    centerX: Float,
    centerY: Float,
    unit: Float,
    currentIndex: Int,
) {
    val bounds = RectF(
        centerX - 170f * unit,
        centerY - 170f * unit,
        centerX + 170f * unit,
        centerY + 170f * unit,
    )
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HomePalette.Gold.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 1.4f * unit
        strokeCap = Paint.Cap.ROUND
    }
    val angle = -90f + currentIndex * 30f
    canvas.drawArc(bounds, angle - 12.5f, 25f, false, paint)
}

private fun serifPaint(size: Float, color: Int, align: Paint.Align): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = size
    this.color = color
    textAlign = align
    typeface = Typeface.create("serif", Typeface.NORMAL)
}

private fun drawOutlinedText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    fillPaint: Paint,
    unit: Float,
) {
    val outline = Paint(fillPaint).apply {
        color = HomePalette.Background.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 1.8f * unit
        strokeJoin = Paint.Join.ROUND
    }
    canvas.drawText(text, x, y, outline)
    canvas.drawText(text, x, y, fillPaint)
}

private fun pointOnCircle(centerX: Float, centerY: Float, radius: Float, angleDegrees: Float): Offset {
    val angle = Math.toRadians(angleDegrees.toDouble())
    return Offset(
        x = centerX + cos(angle).toFloat() * radius,
        y = centerY + sin(angle).toFloat() * radius,
    )
}
