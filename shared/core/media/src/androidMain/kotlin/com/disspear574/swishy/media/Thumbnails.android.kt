package com.disspear574.swishy.media

import android.content.ContentUris
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun loadThumbnail(id: String, widthPx: Int, heightPx: Int): ImageBitmap? {
    val context = MediaContext.appContext ?: return null
    val numericId = id.toLongOrNull() ?: return null

    return withContext(Dispatchers.IO) {
        val signal = CancellationSignal()
        val uri = ContentUris.withAppendedId(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            numericId,
        )
        try {
            val bitmap = runInterruptible(signal) {
                context.contentResolver.loadThumbnail(uri, Size(widthPx, heightPx), signal)
            }
            bitmap?.asImageBitmap()
        } catch (error: Exception) {
            null
        }
    }
}

private inline fun <T> runInterruptible(signal: CancellationSignal, block: () -> T): T? = try {
    block()
} catch (error: android.os.OperationCanceledException) {
    signal.cancel()
    null
}
