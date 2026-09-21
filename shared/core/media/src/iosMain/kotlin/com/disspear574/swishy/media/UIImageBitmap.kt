package com.disspear574.swishy.media

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSinceReferenceDate
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation

@OptIn(ExperimentalForeignApi::class)
internal fun UIImage.toImageBitmap(): ImageBitmap? {
    val startedAt = NSDate().timeIntervalSinceReferenceDate
    val cgImage = upright()?.CGImage ?: redrawn()?.CGImage ?: return null
    val width = CGImageGetWidth(cgImage).toInt()
    val height = CGImageGetHeight(cgImage).toInt()
    if (width <= 0 || height <= 0) return null

    val rowBytes = width * BYTES_PER_PIXEL
    val pixels = ByteArray(height * rowBytes)
    val colorSpace = CGColorSpaceCreateDeviceRGB()
    pixels.usePinned { pinned ->
        val context = CGBitmapContextCreate(
            data = pinned.addressOf(0),
            width = width.toULong(),
            height = height.toULong(),
            bitsPerComponent = BITS_PER_COMPONENT,
            bytesPerRow = rowBytes.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
        )
        CGContextDrawImage(
            context,
            CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()),
            cgImage,
        )
        CGContextRelease(context)
    }
    CGColorSpaceRelease(colorSpace)

    val bitmap = Image.makeRaster(
        imageInfo = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL),
        bytes = pixels,
        rowBytes = rowBytes,
    ).toComposeImageBitmap()
    val millis = (NSDate().timeIntervalSinceReferenceDate - startedAt) * 1000
    println("SWISHY raster ${width}x${height} ${millis.toInt()}ms")
    return bitmap
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.upright(): UIImage? {
    if (imageOrientation == UIImageOrientation.UIImageOrientationUp) return this
    return redrawn()
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.redrawn(): UIImage? {
    UIGraphicsBeginImageContextWithOptions(size = size, opaque = false, scale = scale)
    size.useContents { drawInRect(CGRectMake(0.0, 0.0, width, height)) }
    val redrawn = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return redrawn
}

private const val BYTES_PER_PIXEL = 4
private const val BITS_PER_COMPONENT = 8uL
