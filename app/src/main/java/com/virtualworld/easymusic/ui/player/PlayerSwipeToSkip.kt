package com.virtualworld.easymusic.ui.player

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun PlayerSwipeToSkip(
    enabled: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onSwipeToPrevious: () -> Unit,
    onSwipeToNext: () -> Unit,
    modifier: Modifier = Modifier,
    previousContent: (@Composable () -> Unit)? = null,
    nextContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val swipeEnabled = enabled && (hasPrevious || hasNext)

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { clip = true }
            .then(
                if (swipeEnabled) {
                    Modifier.pointerInput(hasPrevious, hasNext) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                val width = size.width.toFloat()
                                val resistance = 0.35f
                                val raw = offsetX + dragAmount
                                offsetX = when {
                                    raw > 0f && !hasPrevious -> raw * resistance
                                    raw < 0f && !hasNext -> raw * resistance
                                    else -> raw
                                }.coerceIn(-width, width)
                            },
                            onDragCancel = {
                                scope.launch {
                                    animateSwipeOffset(offsetX, 0f) { offsetX = it }
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val width = size.width.toFloat()
                                    val threshold = width * 0.22f
                                    when {
                                        offsetX < -threshold && hasNext -> {
                                            animateSwipeOffset(offsetX, -width) { offsetX = it }
                                            onSwipeToNext()
                                            offsetX = 0f
                                        }
                                        offsetX > threshold && hasPrevious -> {
                                            animateSwipeOffset(offsetX, width) { offsetX = it }
                                            onSwipeToPrevious()
                                            offsetX = 0f
                                        }
                                        else -> {
                                            animateSwipeOffset(offsetX, 0f) { offsetX = it }
                                        }
                                    }
                                }
                            },
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        if (previousContent != null && hasPrevious) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset((offsetX - widthPx).roundToInt(), 0) },
            ) {
                previousContent()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) },
        ) {
            content()
        }

        if (nextContent != null && hasNext) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset((offsetX + widthPx).roundToInt(), 0) },
            ) {
                nextContent()
            }
        }
    }
}

private suspend fun animateSwipeOffset(
    from: Float,
    to: Float,
    onValue: (Float) -> Unit,
) {
    animate(
        initialValue = from,
        targetValue = to,
        animationSpec = tween(durationMillis = 220),
    ) { value, _ ->
        onValue(value)
    }
}
