package com.disspear574.swishy.probe

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.disspear574.swishy.decisions.Decision
import com.disspear574.swishy.media.MediaAsset
import com.disspear574.swishy.media.MediaKind
import com.disspear574.swishy.media.VideoCard

@Composable
internal fun DeckView(
    asset: MediaAsset,
    onDecision: (Decision) -> Unit,
    modifier: Modifier = Modifier,
) {
    var offsetX by remember(asset.id) { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationX = offsetX }
            .pointerInput(asset.id) {
                val threshold = size.width / THRESHOLD_FRACTION
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX <= -threshold -> onDecision(Decision.TRASHED)
                            offsetX >= threshold -> onDecision(Decision.KEPT)
                            else -> offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, amount -> offsetX += amount },
                )
            },
    ) {
        when (asset.kind) {
            MediaKind.VIDEO -> VideoCard(id = asset.id, modifier = Modifier.fillMaxSize())
            MediaKind.PHOTO -> AssetImage(
                id = asset.id,
                widthPx = THUMBNAIL_WIDTH_PX,
                heightPx = THUMBNAIL_HEIGHT_PX,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private const val THRESHOLD_FRACTION = 4f

private const val THUMBNAIL_WIDTH_PX = 1080
private const val THUMBNAIL_HEIGHT_PX = 1920
