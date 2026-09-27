package com.disspear574.swishy.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.designsystem.theme.isReduceMotionEnabled

@Composable
fun LoadingRing(
    progress: Float?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = SwishyTheme.colors
    val reduceMotion = isReduceMotionEnabled()
    val turn = if (progress == null && !reduceMotion) {
        rememberInfiniteTransition(label = "loading").animateFloat(
            initialValue = 0f,
            targetValue = FULL_TURN,
            animationSpec = infiniteRepeatable(tween(TURN_MILLIS, easing = LinearEasing), RepeatMode.Restart),
            label = "turn",
        ).value
    } else {
        0f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(RING_SIZE)
            .background(colors.mediaScrim, CircleShape)
            .semantics {
                this.contentDescription = contentDescription
                progressBarRangeInfo = if (progress == null) {
                    ProgressBarRangeInfo.Indeterminate
                } else {
                    ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize().padding(RING_INSET)) {
            val stroke = Stroke(width = STROKE.toPx(), cap = StrokeCap.Round)
            drawArc(colors.onMedia.copy(alpha = TRACK_ALPHA), 0f, FULL_TURN, false, style = stroke)
            val sweep = progress?.coerceIn(0f, 1f)?.times(FULL_TURN) ?: INDETERMINATE_SWEEP
            drawArc(colors.onMedia, START_ANGLE + turn, sweep, false, style = stroke)
        }
    }
}

@Preview
@Composable
private fun LoadingRingPreview() {
    SwishyTheme {
        Box(Modifier.background(SwishyTheme.colors.ground).padding(16.dp)) {
            LoadingRing(progress = 0.4f, contentDescription = "Loading video, 40%")
        }
    }
}

private val RING_SIZE = 48.dp
private val RING_INSET = 10.dp
private val STROKE = 3.dp
private const val FULL_TURN = 360f
private const val START_ANGLE = -90f
private const val INDETERMINATE_SWEEP = 100f
private const val TRACK_ALPHA = 0.3f
private const val TURN_MILLIS = 900
