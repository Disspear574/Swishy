package com.disspear574.swishy.probe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.disspear574.swishy.media.loadThumbnail

@Composable
internal fun AssetImage(id: String, widthPx: Int, heightPx: Int, modifier: Modifier = Modifier) {
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, id) {
        value = loadThumbnail(id = id, widthPx = widthPx, heightPx = heightPx)
    }

    Box(modifier) {
        bitmap?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
