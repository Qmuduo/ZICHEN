package com.qmuduo.zichen.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

/** Fixed, quiet light across the surface, cached until its dimensions change. */
fun Modifier.inkSurface(): Modifier = drawWithCache {
    val light = Brush.radialGradient(
        colors = listOf(HomePalette.Surface, HomePalette.Background),
        center = Offset(size.width * 0.28f, size.height * 0.12f),
        radius = size.maxDimension.coerceAtLeast(1f) * 0.82f,
    )
    onDrawBehind { drawRect(light) }
}

/** Compose animation observes the platform animator duration scale. */
@Composable
fun Modifier.polishedClick(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(140), label = "press")
    return graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = LocalIndication.current, role = Role.Button, onClick = onClick)
}
