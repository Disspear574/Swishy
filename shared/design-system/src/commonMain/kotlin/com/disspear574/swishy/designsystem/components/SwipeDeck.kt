package com.disspear574.swishy.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.designsystem.theme.isReduceMotionEnabled
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow

@Suppress("LongParameterList", "LongMethod")
@Composable
fun <T : Any> SwipeDeck(
    items: List<T>,
    key: (T) -> Any,
    keepLabel: String,
    trashLabel: String,
    onVerdict: suspend (T, SwipeVerdict) -> Boolean,
    modifier: Modifier = Modifier,
    moveLabel: String? = null,
    onHintChange: (SwipeHint) -> Unit = {},
    content: @Composable (item: T, depth: Int) -> Unit,
) {
    val top = items.firstOrNull() ?: return
    val topKey = key(top)
    val moveEnabled = moveLabel != null

    val reduceMotion = isReduceMotionEnabled()
    val scope = rememberCoroutineScope()

    val offsetX = remember(topKey) { Animatable(0f) }
    val offsetY = remember(topKey) { Animatable(0f) }
    var targetX by remember(topKey) { mutableFloatStateOf(0f) }
    var targetY by remember(topKey) { mutableFloatStateOf(0f) }
    var width by remember { mutableFloatStateOf(1f) }
    var height by remember { mutableFloatStateOf(1f) }

    val thresholdX = width * THRESHOLD_FRACTION
    val thresholdY = height * THRESHOLD_FRACTION_UP
    val hint = swipeHint(
        horizontal = offsetX.value / thresholdX,
        up = -offsetY.value / thresholdY,
        moveEnabled = moveEnabled,
    )

    LaunchedEffect(hint) { onHintChange(hint) }

    val visible = items.take(BEHIND_COUNT + 1)

    Box(modifier.fillMaxSize()) {
        visible.withIndex().reversed().forEach { (depth, item) ->
            key(key(item)) {
                DeckCard(
                    depth = depth,
                    offsetX = offsetX.value,
                    offsetY = offsetY.value,
                    width = width,
                    hint = if (reduceMotion && depth > 0) SwipeHint.None else hint,
                    labels = VerdictLabels(keep = keepLabel, trash = trashLabel, move = moveLabel),
                    reduceMotion = reduceMotion,
                    onSize = { w, h ->
                        width = w
                        height = h
                    },
                    onDrag = { amount ->
                        targetX += amount.x
                        targetY += if (amount.y > 0f && targetY >= 0f) amount.y * DOWN_RESISTANCE else amount.y
                        targetY = targetY.coerceAtMost(height * DOWN_LIMIT)
                        scope.launch { follow(offsetX, targetX, reduceMotion) }
                        scope.launch { follow(offsetY, targetY, reduceMotion) }
                    },
                    onRelease = {
                        val verdict = resolveSwipe(
                            horizontal = targetX / thresholdX,
                            up = -targetY / thresholdY,
                            moveEnabled = moveEnabled,
                        )
                        scope.launch {
                            if (verdict == null) {
                                targetX = 0f
                                targetY = 0f
                                settleBack(offsetX, offsetY, reduceMotion)
                                return@launch
                            }
                            fly(offsetX, offsetY, verdict, width, height)
                            val accepted = onVerdict(top, verdict)
                            if (!accepted) {
                                targetX = 0f
                                targetY = 0f
                                settleBack(offsetX, offsetY, reduceMotion)
                            }
                        }
                    },
                ) {
                    content(item, depth)
                }
            }
        }
    }
}

private suspend fun fly(
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    verdict: SwipeVerdict,
    width: Float,
    height: Float,
) {
    when (verdict) {
        SwipeVerdict.Move -> offsetY.animateTo(
            targetValue = -height * FLIGHT_SPAN,
            animationSpec = tween(durationMillis = FLIGHT_MILLIS),
        )
        SwipeVerdict.Keep, SwipeVerdict.Trash -> offsetX.animateTo(
            targetValue = (if (verdict == SwipeVerdict.Keep) 1f else -1f) * width * FLIGHT_SPAN,
            animationSpec = tween(durationMillis = FLIGHT_MILLIS),
        )
    }
}

private suspend fun settleBack(
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    reduceMotion: Boolean,
) {
    val spec: AnimationSpec<Float> = if (reduceMotion) {
        tween(durationMillis = 0)
    } else {
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }
    coroutineScope {
        launch { offsetX.animateTo(targetValue = 0f, animationSpec = spec) }
        launch { offsetY.animateTo(targetValue = 0f, animationSpec = spec) }
    }
}

private suspend fun follow(offset: Animatable<Float, *>, target: Float, reduceMotion: Boolean) {
    if (reduceMotion) {
        offset.snapTo(target)
    } else {
        offset.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }
}

private data class VerdictLabels(val keep: String, val trash: String, val move: String?)

@Suppress("LongParameterList")
@Composable
private fun DeckCard(
    depth: Int,
    offsetX: Float,
    offsetY: Float,
    width: Float,
    hint: SwipeHint,
    labels: VerdictLabels,
    reduceMotion: Boolean,
    onSize: (Float, Float) -> Unit,
    onDrag: (Offset) -> Unit,
    onRelease: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = SwishyTheme.colors
    val isTop = depth == 0
    val magnitude = hint.magnitude
    val verdictColor = when (hint.verdict) {
        SwipeVerdict.Keep, null -> colors.keep
        SwipeVerdict.Trash -> colors.trash
        SwipeVerdict.Move -> colors.move
    }

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
                    translationY += offsetY
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
                    Modifier.swipeGesture(onSize = onSize, onDrag = onDrag, onRelease = onRelease)
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

        val verdict = hint.verdict
        if (isTop && magnitude > 0f && verdict != null) {
            Verdict(
                magnitude = magnitude,
                color = verdictColor,
                verdict = verdict,
                label = when (verdict) {
                    SwipeVerdict.Keep -> labels.keep
                    SwipeVerdict.Trash -> labels.trash
                    SwipeVerdict.Move -> labels.move ?: labels.keep
                },
                modifier = Modifier.graphicsLayer {
                    translationX = -offsetX * VERDICT_PARALLAX
                    translationY = -offsetY * VERDICT_PARALLAX
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
    onSize: (Float, Float) -> Unit,
    onDrag: (Offset) -> Unit,
    onRelease: () -> Unit,
): Modifier {
    val currentDrag by rememberUpdatedState(onDrag)
    val currentRelease by rememberUpdatedState(onRelease)
    val currentSize by rememberUpdatedState(onSize)

    return pointerInput(Unit) {
        currentSize(size.width.toFloat(), size.height.toFloat())
        detectDragGestures(
            onDragEnd = { currentRelease() },
            onDragCancel = { currentRelease() },
            onDrag = { _, amount -> currentDrag(amount) },
        )
    }
}

private fun Modifier.verdictTint(color: Color, magnitude: Float): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
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
    color: Color,
    verdict: SwipeVerdict,
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
            when (verdict) {
                SwipeVerdict.Keep -> CheckIcon(iconColor)
                SwipeVerdict.Trash -> TrashIcon(iconColor)
                SwipeVerdict.Move -> MoveIcon(iconColor)
            }
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

private const val THRESHOLD_FRACTION_UP = 0.18f

private const val DOWN_RESISTANCE = 0.25f
private const val DOWN_LIMIT = 0.08f

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
