package com.disspear574.swishy.media

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import platform.Foundation.NSMutableArray
import platform.Foundation.NSPredicate
import platform.Foundation.NSSortDescriptor
import platform.Foundation.addObject
import platform.Foundation.timeIntervalSince1970
import platform.Photos.PHAccessLevelReadWrite
import platform.Photos.PHAsset
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAssetMediaSubtypePhotoLive
import platform.Photos.PHAssetMediaSubtypePhotoScreenshot
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHAssetMediaTypeVideo
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHFetchOptions
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

class IosMediaLibrary : MediaLibrary, AlbumLibrary by IosAlbums {

    override suspend fun permissionState(): PermissionState =
        PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite).toState()

    override suspend fun requestPermission(): PermissionState =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelReadWrite) { status ->
                // The system may answer twice; a second resume would crash.
                if (continuation.isActive) {
                    continuation.resume(status.toState())
                }
            }
        }

    // PhotoKit reads block for as long as the library is large; the main thread must never wait on them.
    override suspend fun allAssets(): List<MediaAsset> = withContext(Dispatchers.Default) { readAll() }

    override suspend fun sizeOf(ids: List<String>): Map<String, Long> = withContext(Dispatchers.Default) {
        fetchAssetsById(ids).associate { it.localIdentifier to it.fileSizeBytes() }
    }

    override suspend fun assets(month: MonthKey): List<MediaAsset> = withContext(Dispatchers.Default) {
        val zone = TimeZone.currentSystemDefault()
        readAll().filter { asset -> asset.monthKeyIn(zone) == month }
    }

    override suspend fun assets(ids: List<String>): List<MediaAsset> = withContext(Dispatchers.Default) {
        val byId = readAll().associateBy { it.id }
        ids.mapNotNull(byId::get)
    }

    private fun fetchAssets(): List<PHAsset> {
        val options = PHFetchOptions().apply {
            predicate = NSPredicate.predicateWithFormat(
                "mediaType == %d OR mediaType == %d",
                PHAssetMediaTypeImage,
                PHAssetMediaTypeVideo,
            )
            sortDescriptors = listOf(
                NSSortDescriptor.sortDescriptorWithKey("creationDate", ascending = false),
            )
        }
        val result = PHAsset.fetchAssetsWithOptions(options)
        return buildList {
            for (index in 0uL until result.count) {
                (result.objectAtIndex(index) as? PHAsset)?.let(::add)
            }
        }
    }

    private fun readAll(): List<MediaAsset> = fetchAssets().map { asset ->
        val isVideo = asset.mediaType == PHAssetMediaTypeVideo
        MediaAsset(
            id = asset.localIdentifier,
            kind = if (isVideo) MediaKind.VIDEO else MediaKind.PHOTO,
            takenAtMillis = ((asset.creationDate?.timeIntervalSince1970 ?: 0.0) * 1_000).toLong(),
            // Zero on purpose: a size costs a resource lookup per asset, so it is fetched separately.
            sizeBytes = 0,
            durationMillis = (asset.duration * 1_000).toLong().takeIf { isVideo },
            isLive = asset.hasSubtype(PHAssetMediaSubtypePhotoLive),
            isScreenshot = asset.hasSubtype(PHAssetMediaSubtypePhotoScreenshot),
            isFavorite = asset.favorite,
        )
    }

    override suspend fun delete(ids: List<String>): DeleteResult {
        if (ids.isEmpty()) return DeleteResult.Deleted(ids = emptyList(), freedBytes = 0)

        val (assets, freedBytes) = withContext(Dispatchers.Default) {
            val found = fetchAssetsById(ids)
            found to found.sumOf { it.fileSizeBytes() }
        }
        if (assets.isEmpty()) return DeleteResult.Failed(reason = "assets not found")

        return suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = {
                    val batch = NSMutableArray()
                    assets.forEach(batch::addObject)
                    PHAssetChangeRequest.deleteAssets(batch)
                },
                completionHandler = { success, error ->
                    val result = when {
                        success -> DeleteResult.Deleted(ids = ids, freedBytes = freedBytes)
                        error?.code == USER_CANCELLED -> DeleteResult.Cancelled
                        else -> DeleteResult.Failed(
                            reason = error?.localizedDescription ?: "unknown",
                        )
                    }
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                },
            )
        }
    }

    private fun Long.toState(): PermissionState = when (this) {
        PHAuthorizationStatusAuthorized -> PermissionState.GRANTED
        PHAuthorizationStatusLimited -> PermissionState.LIMITED
        PHAuthorizationStatusDenied -> PermissionState.DENIED
        PHAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
        else -> PermissionState.DENIED
    }

    private companion object {
        const val USER_CANCELLED = 3072L
    }
}

private fun PHAsset.hasSubtype(subtype: ULong): Boolean =
    mediaSubtypes.toLong() and subtype.toLong() != 0L

private fun fetchAssetsById(ids: List<String>): List<PHAsset> {
    if (ids.isEmpty()) return emptyList()
    val result = PHAsset.fetchAssetsWithLocalIdentifiers(ids, null)
    return buildList {
        for (index in 0uL until result.count) {
            (result.objectAtIndex(index) as? PHAsset)?.let(::add)
        }
    }
}
