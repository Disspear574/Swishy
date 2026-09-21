package com.disspear574.swishy.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.rememberUpdatedState
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
fun <T : Any> SwipeDeck(
    items: List<T>,
    key: (T) -> Any,
    keepLabel: String,
    trashLabel: String,
    onVerdict: (T, SwipeVerdict) -> Unit,
    modifier: Modifier = Modifier,
    onProgressChange: (Float) -> Unit = {},
    content: @Composable (item: T, isTop: Boolean) -> Unit,
) {
    val top = items.firstOrNull() ?: return
    val topKey = key(top)

    val reduceMotion = isReduceMotionEnabled()
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var width by remember { mutableFloatStateOf(1f) }

    val threshold = width * THRESHOLD_FRACTION
    val progress = (offsetX.value / threshold).coerceIn(-1f, 1f)
    val magnitude = min(abs(progress), 1f)

    LaunchedEffect(topKey) { offsetX.snapTo(0f) }
    LaunchedEffect(progress) { onProgressChange(progress) }

    Box(modifier.fillMaxSize()) {
        items.drop(1).take(BEHIND_COUNT).asReversed().forEachIndexed { index, item ->
            val depth = min(BEHIND_COUNT - index, BEHIND_COUNT)
            BehindCard(depth = depth, magnitude = if (reduceMotion) 0f else magnitude) {
                content(item, false)
            }
        }

        TopCard(
            offsetX = offsetX.value,
            width = width,
            magnitude = magnitude,
            progress = progress,
            keepLabel = keepLabel,
            trashLabel = trashLabel,
            reduceMotion = reduceMotion,
            onWidth = { width = it },
            onDrag = { amount -> scope.launch { offsetX.snapTo(offsetX.value + amount) } },
            onRelease = {
                val settled = (offsetX.value / threshold).coerceIn(-1f, 1f)
                scope.launch {
                    if (abs(settled) >= 1f) {
                        val verdict =
                            if (settled > 0f) SwipeVerdict.Keep else SwipeVerdict.Trash
                        offsetX.animateTo(
                            targetValue = (if (settled > 0f) 1f else -1f) * width * FLIGHT_SPAN,
                            animationSpec = tween(durationMillis = FLIGHT_MILLIS),
                        )
                        onVerdict(top, verdict)
                    } else {
                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = if (reduceMotion) {
                                tween(durationMillis = 0)
                            } else {
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            },
                        )
                    }
                }
            },
            content = { content(top, true) },
        )
    }
}

@Composable
private fun BehindCard(depth: Int, magnitude: Float, content: @Composable () -> Unit) {
    val scale = 1f - SCALE_STEP * depth
    val nextScale = 1f - SCALE_STEP * (depth - 1)
    val lift = LIFT_STEP * depth
    val nextLift = LIFT_STEP * (depth - 1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val grown = scale + (nextScale - scale) * magnitude
                scaleX = grown
                scaleY = grown
                translationY = (lift + (nextLift - lift) * magnitude) * density
            }
            .clip(SwishyTheme.shapes.card)
            .border(
                width = 1.dp,
                color = SwishyTheme.colors.hairline,
                shape = SwishyTheme.shapes.card,
            ),
    ) {
        content()
    }
}

@Suppress("LongParameterList")
@Composable
private fun TopCard(
    offsetX: Float,
    width: Float,
    magnitude: Float,
    progress: Float,
    keepLabel: String,
    trashLabel: String,
    reduceMotion: Boolean,
    onWidth: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onRelease: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = SwishyTheme.colors
    val verdictColor = if (progress >= 0f) colors.keep else colors.trash

    val currentDrag by rememberUpdatedState(onDrag)
    val currentRelease by rememberUpdatedState(onRelease)
    val currentWidth by rememberUpdatedState(onWidth)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val travel = offsetX / width
                translationX = offsetX
                val lift = travel.coerceIn(-1f, 1f)
                translationY = if (reduceMotion) 0f else -lift * lift * ARC_LIFT_DP * density
                rotationZ = if (reduceMotion) {
                    0f
                } else {
                    (travel * TILT_PER_WIDTH).coerceIn(-MAX_TILT, MAX_TILT)
                }
            }
            .clip(SwishyTheme.shapes.card)
            .pointerInput(Unit) {
                currentWidth(size.width.toFloat())
                detectHorizontalDragGestures(
                    onDragEnd = { currentRelease() },
                    onDragCancel = { currentRelease() },
                    onHorizontalDrag = { _, amount -> currentDrag(amount) },
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

        if (magnitude > 0f) {
            Verdict(
                magnitude = magnitude,
                color = verdictColor,
                keep = progress >= 0f,
                label = if (progress >= 0f) keepLabel else trashLabel,
                modifier = Modifier.graphicsLayer {
                    translationX = -offsetX * VERDICT_PARALLAX
                },
            )
        }
    }
}

@Composable
private fun Verdict(
    magnitude: Float,
    color: androidx.compose.ui.graphics.Color,
    keep: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = SwishyTheme.colors
    val armed = magnitude >= 1f

    Column(
        modifier = modifier
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
                        Modifier.background(color)
                    } else {
                        Modifier.border(
                            width = 3.dp,
                            color = color,
                            shape = SwishyTheme.shapes.pill,
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val iconColor = if (armed) colors.onAccent else color
            if (keep) CheckIcon(iconColor) else TrashIcon(iconColor)
        }

        Box(
            modifier = Modifier
                .padding(top = SwishyTheme.spacing.medium)
                .clip(SwishyTheme.shapes.pill)
                .background(color)
                .padding(
                    horizontal = SwishyTheme.spacing.screen,
                    vertical = SwishyTheme.spacing.small,
                ),
        ) {
            SwishyText(
                text = label,
                style = SwishyTheme.typography.label,
                color = colors.onAccent,
            )
        }
    }
}

private const val THRESHOLD_FRACTION = 0.25f

private const val TILT_PER_WIDTH = 14f
private const val MAX_TILT = 11f

private const val VERDICT_PARALLAX = 0.35f
private const val TINT_ALPHA = 0.55f
private const val VERDICT_FADE_SPEED = 1.6f
private const val VERDICT_MIN_SCALE = 0.78f
private const val FLIGHT_SPAN = 1.6f
private const val FLIGHT_MILLIS = 260
private const val BEHIND_COUNT = 2

private const val SCALE_STEP = 0.1f

private const val LIFT_STEP = 18f

private const val ARC_LIFT_DP = 56f
private val BADGE_SIZE = 92.dp
