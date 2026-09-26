package com.disspear574.swishy.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

// Drawn by Compose: a platform view ignores the card transform and stays put during a swipe.
@Composable
internal fun PhotoFrame(bitmap: ImageBitmap?, modifier: Modifier = Modifier) {
    Box(modifier) {
        bitmap?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = BACKDROP_ALPHA,
                modifier = Modifier.fillMaxSize().blur(BACKDROP_BLUR),
            )
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val BACKDROP_BLUR = 32.dp
private const val BACKDROP_ALPHA = 0.5f
