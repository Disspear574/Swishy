package com.disspear574.swishy.media

import android.content.ContentUris
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

@Composable
actual fun PhotoCard(
    id: String,
    modifier: Modifier,
    live: Boolean,
    playingLive: Boolean,
) {
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, id) {
        value = decode(id)
    }

    Box(modifier) {
        bitmap?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Suppress("SwallowedException")
private suspend fun decode(id: String): ImageBitmap? {
    val context = MediaContext.appContext ?: return null
    val numericId = id.toLongOrNull() ?: return null

    return withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            numericId,
        )
        try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.setTargetSampleSize(sampleSize(info.size.width, info.size.height))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }.asImageBitmap()
        } catch (notFound: FileNotFoundException) {
            null
        } catch (io: IOException) {
            null
        } catch (denied: SecurityException) {
            null
        }
    }
}

private fun sampleSize(width: Int, height: Int): Int {
    var sample = 1
    while (width / (sample * 2) >= TARGET_WIDTH_PX && height / (sample * 2) >= TARGET_HEIGHT_PX) {
        sample *= 2
    }
    return sample
}

internal val supportsGainmap: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

private const val TARGET_WIDTH_PX = 1080
private const val TARGET_HEIGHT_PX = 1920
