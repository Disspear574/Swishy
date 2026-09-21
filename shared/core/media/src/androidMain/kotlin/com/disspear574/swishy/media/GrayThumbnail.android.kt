package com.disspear574.swishy.media

import android.content.ContentUris
import android.provider.MediaStore
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Suppress("SwallowedException")
actual suspend fun grayThumbnail(id: String, side: Int): IntArray? {
    val context = MediaContext.appContext ?: return null
    val numericId = id.toLongOrNull() ?: return null

    return withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            numericId,
        )
        try {
            val bitmap = context.contentResolver.loadThumbnail(uri, Size(side, side), null)
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val gray = IntArray(side * side)
            for (y in 0 until side) {
                val sourceY = y * bitmap.height / side
                for (x in 0 until side) {
                    val sourceX = x * bitmap.width / side
                    gray[y * side + x] = luminance(pixels[sourceY * bitmap.width + sourceX])
                }
            }
            bitmap.recycle()
            gray
        } catch (io: IOException) {
            null
        } catch (denied: SecurityException) {
            null
        }
    }
}

private fun luminance(color: Int): Int {
    val red = (color shr RED_SHIFT) and BYTE_MASK
    val green = (color shr GREEN_SHIFT) and BYTE_MASK
    val blue = color and BYTE_MASK
    return (red * RED_WEIGHT + green * GREEN_WEIGHT + blue * BLUE_WEIGHT) / WEIGHT_TOTAL
}

private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val BYTE_MASK = 0xFF

private const val RED_WEIGHT = 299
private const val GREEN_WEIGHT = 587
private const val BLUE_WEIGHT = 114
private const val WEIGHT_TOTAL = 1000
