package com.disspear574.swishy.media

import android.content.ContentUris
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
    preview: Boolean,
) {
    var bitmap by remember(id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(id, preview) {
        val decoded = decode(id = id, preview = preview)
        if (decoded != null && (!preview || bitmap == null)) {
            bitmap = decoded
        }
    }

    PhotoFrame(bitmap = bitmap, modifier = modifier)
}

@Suppress("SwallowedException")
private suspend fun decode(id: String, preview: Boolean): ImageBitmap? {
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
                decoder.setTargetSampleSize(
                    sampleSize(
                        width = info.size.width,
                        height = info.size.height,
                        preview = preview,
                    ),
                )
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

private fun sampleSize(width: Int, height: Int, preview: Boolean): Int {
    val targetWidth = if (preview) PREVIEW_WIDTH_PX else TARGET_WIDTH_PX
    val targetHeight = if (preview) PREVIEW_HEIGHT_PX else TARGET_HEIGHT_PX
    var sample = 1
    while (width / (sample * 2) >= targetWidth && height / (sample * 2) >= targetHeight) {
        sample *= 2
    }
    return sample
}

internal val supportsGainmap: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

private const val TARGET_WIDTH_PX = 1080
private const val TARGET_HEIGHT_PX = 1920

private const val PREVIEW_WIDTH_PX = 360
private const val PREVIEW_HEIGHT_PX = 640
