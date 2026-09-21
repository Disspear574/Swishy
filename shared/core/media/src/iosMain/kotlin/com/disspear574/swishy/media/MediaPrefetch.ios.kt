package com.disspear574.swishy.media

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Photos.PHAsset
import platform.Photos.PHCachingImageManager
import platform.Photos.PHImageContentModeAspectFit

@OptIn(ExperimentalForeignApi::class)
actual fun prefetchMedia(ids: List<String>) {
    val wanted = ids.toSet()
    val added = wanted - cached
    val dropped = cached - wanted
    if (added.isEmpty() && dropped.isEmpty()) return

    dropped.mapNotNull(::fetchAsset).takeIf { it.isNotEmpty() }?.let { assets ->
        photoManager.stopCachingImagesForAssets(
            assets = assets,
            targetSize = fullTargetSize(),
            contentMode = PHImageContentModeAspectFit,
            options = imageOptions(),
        )
    }
    added.mapNotNull(::fetchAsset).takeIf { it.isNotEmpty() }?.let { assets ->
        photoManager.startCachingImagesForAssets(
            assets = assets,
            targetSize = fullTargetSize(),
            contentMode = PHImageContentModeAspectFit,
            options = imageOptions(),
        )
    }
    cached = wanted
}

private var cached: Set<String> = emptySet()

internal val photoManager: PHCachingImageManager = PHCachingImageManager()

internal fun fetchAsset(id: String): PHAsset? =
    PHAsset.fetchAssetsWithLocalIdentifiers(listOf(id), null).firstObject as? PHAsset
