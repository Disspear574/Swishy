package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

enum class TrackMark { Pending, Kept, Trashed, Moved }

@Composable
fun ProgressTrack(
    marks: List<TrackMark>,
    modifier: Modifier = Modifier,
) {
    val colors = SwishyTheme.colors
    if (marks.isEmpty()) return

    Canvas(modifier.fillMaxWidth().height(TRACK_HEIGHT)) {
        val detailed = marks.size <= MAX_DETAILED_MARKS
        val radius = CornerRadius(size.height / 2f, size.height / 2f)

        if (detailed) {
            val gap = GAP_PX
            val cell = (size.width - gap * (marks.size - 1)) / marks.size
            marks.forEachIndexed { index, mark ->
                drawRoundRect(
                    color = when (mark) {
                        TrackMark.Pending -> colors.hairline
                        TrackMark.Kept -> colors.keep
                        TrackMark.Trashed -> colors.trash
                        TrackMark.Moved -> colors.move
                    },
                    topLeft = Offset(x = index * (cell + gap), y = 0f),
                    size = Size(width = cell, height = size.height),
                    cornerRadius = radius,
                )
            }
            return@Canvas
        }

        val kept = marks.count { it == TrackMark.Kept }
        val trashed = marks.count { it == TrackMark.Trashed }
        val moved = marks.count { it == TrackMark.Moved }
        val keptWidth = size.width * kept / marks.size
        val trashedWidth = size.width * trashed / marks.size
        val movedWidth = size.width * moved / marks.size

        drawRoundRect(color = colors.hairline, size = size, cornerRadius = radius)
        drawRoundRect(
            color = colors.keep,
            size = Size(width = keptWidth, height = size.height),
            cornerRadius = radius,
        )
        drawRoundRect(
            color = colors.trash,
            topLeft = Offset(x = keptWidth, y = 0f),
            size = Size(width = trashedWidth, height = size.height),
            cornerRadius = radius,
        )
        drawRoundRect(
            color = colors.move,
            topLeft = Offset(x = keptWidth + trashedWidth, y = 0f),
            size = Size(width = movedWidth, height = size.height),
            cornerRadius = radius,
        )
    }
}

private val TRACK_HEIGHT = 3.dp
private const val MAX_DETAILED_MARKS = 60
private const val GAP_PX = 3f
