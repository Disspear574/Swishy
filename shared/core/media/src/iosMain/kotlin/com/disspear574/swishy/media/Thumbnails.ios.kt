package com.disspear574.swishy.media

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Photos.PHAsset
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.coroutines.resume

@Suppress("ReturnCount")
@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadThumbnail(id: String, widthPx: Int, heightPx: Int): ImageBitmap? {
    val asset = PHAsset.fetchAssetsWithLocalIdentifiers(listOf(id), null)
        .firstObject as? PHAsset ?: return null

    val options = PHImageRequestOptions().apply {
        deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
        networkAccessAllowed = false
        synchronous = false
    }

    val image: UIImage? = suspendCancellableCoroutine { continuation ->
        val requestId = PHImageManager.defaultManager().requestImageForAsset(
            asset = asset,
            targetSize = CGSizeMake(widthPx.toDouble(), heightPx.toDouble()),
            contentMode = PHImageContentModeAspectFill,
            options = options,
        ) { result, _ ->
            continuation.resume(result)
        }
        continuation.invokeOnCancellation {
            PHImageManager.defaultManager().cancelImageRequest(requestId)
        }
    }
    if (image == null) return null

    val data: NSData = UIImageJPEGRepresentation(image, JPEG_QUALITY) ?: return null
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isEmpty()) return null
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

private const val JPEG_QUALITY = 0.9
