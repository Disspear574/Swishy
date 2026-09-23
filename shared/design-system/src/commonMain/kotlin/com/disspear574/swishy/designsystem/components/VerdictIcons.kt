package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun CheckIcon(color: Color, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val stroke = Stroke(
            width = this.size.minDimension * STROKE_RATIO,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.20f, this@Canvas.size.height * 0.53f)
            lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.74f)
            lineTo(this@Canvas.size.width * 0.80f, this@Canvas.size.height * 0.28f)
        }
        drawPath(path = path, color = color, style = stroke)
    }
}

@Composable
internal fun TrashIcon(color: Color, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(
            width = this.size.minDimension * STROKE_RATIO * 0.8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

        val lid = Path().apply {
            moveTo(w * 0.16f, h * 0.28f)
            lineTo(w * 0.84f, h * 0.28f)
        }
        drawPath(lid, color, style = stroke)

        val handle = Path().apply {
            moveTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.38f, h * 0.17f)
            lineTo(w * 0.62f, h * 0.17f)
            lineTo(w * 0.62f, h * 0.28f)
        }
        drawPath(handle, color, style = stroke)

        val bin = Path().apply {
            moveTo(w * 0.25f, h * 0.28f)
            lineTo(w * 0.31f, h * 0.83f)
            lineTo(w * 0.69f, h * 0.83f)
            lineTo(w * 0.75f, h * 0.28f)
        }
        drawPath(bin, color, style = stroke)
    }
}

private const val STROKE_RATIO = 0.11f

@Composable
internal fun MoveIcon(color: Color, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(
            width = this.size.minDimension * STROKE_RATIO,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val tray = Path().apply {
            moveTo(w * 0.20f, h * 0.60f)
            lineTo(w * 0.20f, h * 0.80f)
            lineTo(w * 0.80f, h * 0.80f)
            lineTo(w * 0.80f, h * 0.60f)
        }
        val arrow = Path().apply {
            moveTo(w * 0.50f, h * 0.66f)
            lineTo(w * 0.50f, h * 0.20f)
            moveTo(w * 0.34f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.20f)
            lineTo(w * 0.66f, h * 0.36f)
        }
        drawPath(path = tray, color = color, style = stroke)
        drawPath(path = arrow, color = color, style = stroke)
    }
}
