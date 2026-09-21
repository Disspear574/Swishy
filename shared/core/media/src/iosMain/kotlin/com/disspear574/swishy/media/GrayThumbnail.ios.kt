package com.disspear574.swishy.media

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceGray
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeOpportunistic
import platform.Photos.PHImageRequestOptionsResizeModeFast
import platform.UIKit.UIGraphicsPopContext
import platform.UIKit.UIGraphicsPushContext
import platform.UIKit.UIImage
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual suspend fun grayThumbnail(id: String, side: Int): IntArray? {
    val asset = fetchAsset(id) ?: return null
    val options = PHImageRequestOptions().apply {
        deliveryMode = PHImageRequestOptionsDeliveryModeOpportunistic
        resizeMode = PHImageRequestOptionsResizeModeFast
        networkAccessAllowed = false
        synchronous = false
    }

    val image = suspendCancellableCoroutine<UIImage?> { continuation ->
        var answered = false
        val requestId = photoManager.requestImageForAsset(
            asset = asset,
            targetSize = CGSizeMake(side.toDouble(), side.toDouble()),
            contentMode = PHImageContentModeAspectFill,
            options = options,
        ) { result, _ ->
            if (!answered) {
                answered = true
                continuation.resume(result)
            }
        }
        continuation.invokeOnCancellation { photoManager.cancelImageRequest(requestId) }
    } ?: return null

    return image.toGray(side)
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toGray(side: Int): IntArray? {
    val pixels = ByteArray(side * side)
    val colorSpace = CGColorSpaceCreateDeviceGray()
    pixels.usePinned { pinned ->
        val context = CGBitmapContextCreate(
            data = pinned.addressOf(0),
            width = side.toULong(),
            height = side.toULong(),
            bitsPerComponent = GRAY_BITS,
            bytesPerRow = side.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNone.value,
        )
        UIGraphicsPushContext(context)
        drawInRect(CGRectMake(0.0, 0.0, side.toDouble(), side.toDouble()))
        UIGraphicsPopContext()
        CGContextRelease(context)
    }
    CGColorSpaceRelease(colorSpace)
    return IntArray(side * side) { index -> pixels[index].toInt() and BYTE_MASK }
}

private const val GRAY_BITS = 8uL
private const val BYTE_MASK = 0xFF
