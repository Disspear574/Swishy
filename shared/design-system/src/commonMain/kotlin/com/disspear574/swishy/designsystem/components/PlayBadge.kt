package com.disspear574.swishy.designsystem.components

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

// A mark only: the whole card takes the tap, which is a larger target than the badge.
@Composable
fun PlayBadge(modifier: Modifier = Modifier) {
    val colors = SwishyTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(BADGE_SIZE).background(colors.mediaScrim, CircleShape),
    ) {
        Canvas(Modifier.fillMaxSize().padding(start = TRIANGLE_NUDGE).padding(TRIANGLE_INSET)) {
            val triangle = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(0f, size.height)
                close()
            }
            drawPath(triangle, colors.onMedia)
        }
    }
}

@Preview
@Composable
private fun PlayBadgePreview() {
    SwishyTheme {
        Box(Modifier.background(SwishyTheme.colors.ground).padding(16.dp)) { PlayBadge() }
    }
}

private val BADGE_SIZE = 64.dp
private val TRIANGLE_INSET = 20.dp

// A triangle centred by its box looks shifted left; its visual centre sits further right.
private val TRIANGLE_NUDGE = 4.dp
