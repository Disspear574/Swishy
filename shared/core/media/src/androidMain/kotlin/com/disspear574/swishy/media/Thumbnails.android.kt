package com.disspear574.swishy.media

import android.content.ContentUris
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.provider.MediaStore
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

@Suppress("SwallowedException")
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
            context.contentResolver.loadThumbnail(uri, Size(widthPx, heightPx), signal)
                .asImageBitmap()
        } catch (notFound: FileNotFoundException) {
            null
        } catch (io: IOException) {
            null
        } catch (denied: SecurityException) {
            null
        } catch (cancelled: OperationCanceledException) {
            signal.cancel()
            null
        }
    }
}
