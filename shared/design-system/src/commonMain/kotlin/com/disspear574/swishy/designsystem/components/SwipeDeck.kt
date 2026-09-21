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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import kotlin.math.pow

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

    val offsetX = remember(topKey) { Animatable(0f) }
    var target by remember(topKey) { mutableFloatStateOf(0f) }
    var width by remember { mutableFloatStateOf(1f) }

    val threshold = width * THRESHOLD_FRACTION
    val progress = (offsetX.value / threshold).coerceIn(-1f, 1f)
    val magnitude = min(abs(progress), 1f)

    LaunchedEffect(progress) { onProgressChange(progress) }

    val visible = items.take(BEHIND_COUNT + 1)

    Box(modifier.fillMaxSize()) {
        visible.withIndex().reversed().forEach { (depth, item) ->
            key(key(item)) {
                DeckCard(
                    depth = depth,
                    offsetX = offsetX.value,
                    width = width,
                    magnitude = if (reduceMotion && depth > 0) 0f else magnitude,
                    progress = progress,
                    keepLabel = keepLabel,
                    trashLabel = trashLabel,
                    reduceMotion = reduceMotion,
                    onWidth = { width = it },
                    onDrag = { amount ->
                        target += amount
                        scope.launch { follow(offsetX, target, reduceMotion) }
                    },
                    onRelease = {
                        val settled = (target / threshold).coerceIn(-1f, 1f)
                        scope.launch {
                            if (abs(settled) >= 1f) {
                                fly(offsetX, settled, width)
                                onVerdict(
                                    top,
                                    if (settled > 0f) SwipeVerdict.Keep else SwipeVerdict.Trash,
                                )
                            } else {
                                target = 0f
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
                ) {
                    content(item, depth == 0)
                }
            }
        }
    }
}

private suspend fun follow(offsetX: Animatable<Float, *>, target: Float, reduceMotion: Boolean) {
    if (reduceMotion) {
        offsetX.snapTo(target)
    } else {
        offsetX.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }
}

private suspend fun fly(offsetX: Animatable<Float, *>, settled: Float, width: Float) {
    offsetX.animateTo(
        targetValue = (if (settled > 0f) 1f else -1f) * width * FLIGHT_SPAN,
        animationSpec = tween(durationMillis = FLIGHT_MILLIS),
    )
}

@Suppress("LongParameterList")
@Composable
private fun DeckCard(
    depth: Int,
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
    val isTop = depth == 0
    val verdictColor = if (progress >= 0f) colors.keep else colors.trash

    val advance = if (reduceMotion) 0f else (1f - (1f - magnitude).pow(3)) * DRAG_CATCHUP
    val settle = rememberSettle(depth = depth, reduceMotion = reduceMotion)

    val slot = depth - (if (isTop) 0f else advance) + (1f - DRAG_CATCHUP) * (1f - settle)
    val slotScale = 1f - SCALE_STEP * slot
    val slotLift = LIFT_STEP * slot

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = slotScale
                scaleY = slotScale
                translationY = slotLift * density
                if (isTop) {
                    translationX = offsetX
                    val travel = offsetX / width
                    val lift = travel.coerceIn(-1f, 1f)
                    if (!reduceMotion) {
                        translationY -= lift * lift * ARC_LIFT_DP * density
                    }
                    rotationZ = if (reduceMotion) {
                        0f
                    } else {
                        (travel * TILT_PER_WIDTH).coerceIn(-MAX_TILT, MAX_TILT)
                    }
                }
            }
            .clip(SwishyTheme.shapes.card)
            .then(
                if (isTop) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = SwishyTheme.colors.hairline,
                        shape = SwishyTheme.shapes.card,
                    )
                },
            )
            .then(
                if (isTop) {
                    Modifier.swipeGesture(
                        onWidth = onWidth,
                        onDrag = onDrag,
                        onRelease = onRelease,
                    )
                } else {
                    Modifier
                },
            )
            .then(
                if (isTop && magnitude > 0f) {
                    Modifier.verdictTint(color = verdictColor, magnitude = magnitude)
                } else {
                    Modifier
                },
            ),
    ) {
        content()

        if (isTop && magnitude > 0f) {
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
private fun rememberSettle(depth: Int, reduceMotion: Boolean): Float {
    val settle = remember { Animatable(1f) }
    var lastDepth by remember { mutableIntStateOf(depth) }

    LaunchedEffect(depth) {
        val rose = depth < lastDepth
        lastDepth = depth
        if (!rose) return@LaunchedEffect
        if (reduceMotion) {
            settle.snapTo(1f)
        } else {
            settle.snapTo(0f)
            settle.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }
    return settle.value
}

@Composable
private fun Modifier.swipeGesture(
    onWidth: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onRelease: () -> Unit,
): Modifier {
    val currentDrag by rememberUpdatedState(onDrag)
    val currentRelease by rememberUpdatedState(onRelease)
    val currentWidth by rememberUpdatedState(onWidth)

    return pointerInput(Unit) {
        currentWidth(size.width.toFloat())
        detectHorizontalDragGestures(
            onDragEnd = { currentRelease() },
            onDragCancel = { currentRelease() },
            onHorizontalDrag = { _, amount -> currentDrag(amount) },
        )
    }
}

private fun Modifier.verdictTint(
    color: androidx.compose.ui.graphics.Color,
    magnitude: Float,
): Modifier = graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            color = color,
            alpha = magnitude * TINT_ALPHA,
            blendMode = BlendMode.Multiply,
        )
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

private const val DRAG_CATCHUP = 0.55f

private const val SCALE_STEP = 0.1f

private const val LIFT_STEP = 18f

private const val ARC_LIFT_DP = 56f
private val BADGE_SIZE = 92.dp
