package com.disspear574.swishy.media

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun loadThumbnail(id: String, widthPx: Int, heightPx: Int): ImageBitmap?
