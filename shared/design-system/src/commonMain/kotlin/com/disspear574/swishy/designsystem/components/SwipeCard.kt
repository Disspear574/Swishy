package com.disspear574.swishy.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.designsystem.theme.isReduceMotionEnabled
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

enum class SwipeVerdict { Keep, Trash }

@Composable
fun SwipeCard(
    key: Any,
    keepLabel: String,
    trashLabel: String,
    onVerdict: (SwipeVerdict) -> Unit,
    modifier: Modifier = Modifier,
    onProgressChange: (Float) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val colors = SwishyTheme.colors
    val reduceMotion = isReduceMotionEnabled()
    val scope = rememberCoroutineScope()

    var offsetX by remember(key) { mutableFloatStateOf(0f) }
    var threshold by remember(key) { mutableFloatStateOf(1f) }

    val progress = (offsetX / threshold).coerceIn(-1f, 1f)
    val magnitude = min(abs(progress), 1f)
    val armed = magnitude >= 1f
    val verdictColor = if (progress >= 0f) colors.keep else colors.trash

    LaunchedEffect(progress) { onProgressChange(progress) }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX
                    rotationZ = if (reduceMotion) 0f else progress * MAX_TILT_DEGREES
                }
                .clip(SwishyTheme.shapes.card)
                .pointerInput(key) {
                    threshold = size.width * THRESHOLD_FRACTION
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val settled = (offsetX / threshold).coerceIn(-1f, 1f)
                            if (abs(settled) >= 1f) {
                                onVerdict(
                                    if (settled > 0f) SwipeVerdict.Keep else SwipeVerdict.Trash,
                                )
                            } else {
                                scope.launch {
                                    offsetX = settleBack(offsetX, reduceMotion) { offsetX = it }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX = settleBack(offsetX, reduceMotion) { offsetX = it }
                            }
                        },
                        onHorizontalDrag = { _, amount -> offsetX += amount },
                    )
                }
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    if (magnitude > 0f) {
                        drawRect(
                            color = verdictColor,
                            alpha = magnitude * TINT_ALPHA,
                            blendMode = BlendMode.Multiply,
                        )
                    }
                },
        ) {
            content()
        }

        if (magnitude > 0f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = min(magnitude * VERDICT_FADE_SPEED, 1f)
                        val scale = VERDICT_MIN_SCALE + magnitude * (1f - VERDICT_MIN_SCALE)
                        scaleX = scale
                        scaleY = scale
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(BADGE_SIZE)
                        .clip(SwishyTheme.shapes.pill)
                        .then(
                            if (armed) {
                                Modifier.background(verdictColor)
                            } else {
                                Modifier.border(
                                    width = 3.dp,
                                    color = verdictColor,
                                    shape = SwishyTheme.shapes.pill,
                                )
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val iconColor = if (armed) colors.onAccent else verdictColor
                    if (progress >= 0f) CheckIcon(iconColor) else TrashIcon(iconColor)
                }

                Box(
                    modifier = Modifier
                        .padding(top = SwishyTheme.spacing.medium)
                        .clip(SwishyTheme.shapes.pill)
                        .background(verdictColor)
                        .padding(
                            horizontal = SwishyTheme.spacing.screen,
                            vertical = SwishyTheme.spacing.small,
                        ),
                ) {
                    SwishyText(
                        text = if (progress >= 0f) keepLabel else trashLabel,
                        style = SwishyTheme.typography.label,
                        color = colors.onAccent,
                    )
                }
            }
        }
    }
}

private suspend fun settleBack(
    from: Float,
    reduceMotion: Boolean,
    onFrame: (Float) -> Unit,
): Float {
    if (reduceMotion) {
        onFrame(0f)
        return 0f
    }
    androidx.compose.animation.core.Animatable(from).animateTo(
        targetValue = 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    ) { onFrame(value) }
    return 0f
}

private const val THRESHOLD_FRACTION = 0.25f
private const val MAX_TILT_DEGREES = 7f
private const val TINT_ALPHA = 0.55f
private const val VERDICT_FADE_SPEED = 1.6f
private const val VERDICT_MIN_SCALE = 0.78f
private val BADGE_SIZE = 92.dp
