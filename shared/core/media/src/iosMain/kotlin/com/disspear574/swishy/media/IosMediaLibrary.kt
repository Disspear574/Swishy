package com.disspear574.swishy.media

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.TimeZone
import platform.Foundation.NSMutableArray
import platform.Foundation.NSNumber
import platform.Foundation.NSPredicate
import platform.Foundation.NSSortDescriptor
import platform.Foundation.addObject
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.valueForKey
import platform.Photos.PHAccessLevelReadWrite
import platform.Photos.PHAsset
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAssetMediaSubtypePhotoLive
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHAssetMediaTypeVideo
import platform.Photos.PHAssetResource
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHFetchOptions
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

class IosMediaLibrary : MediaLibrary {

    override suspend fun permissionState(): PermissionState =
        PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite).toState()

    override suspend fun requestPermission(): PermissionState =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelReadWrite) { status ->
                continuation.resume(status.toState())
            }
        }

    override suspend fun allAssets(): List<MediaAsset> = readAll()

    override suspend fun assets(month: MonthKey): List<MediaAsset> {
        val zone = TimeZone.currentSystemDefault()
        return readAll().filter { asset -> asset.monthKeyIn(zone) == month }
    }

    override suspend fun assets(ids: List<String>): List<MediaAsset> {
        val byId = readAll().associateBy { it.id }
        return ids.mapNotNull(byId::get)
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

    private fun PHAsset.fileSizeBytes(): Long {
        val resource = PHAssetResource.assetResourcesForAsset(this).firstOrNull() as? PHAssetResource
            ?: return 0
        val value = resource.valueForKey("fileSize") as? NSNumber ?: return 0
        return value.longLongValue
    }

    private fun readAll(): List<MediaAsset> = fetchAssets().map { asset ->
        val isVideo = asset.mediaType == PHAssetMediaTypeVideo
        MediaAsset(
            id = asset.localIdentifier,
            kind = if (isVideo) MediaKind.VIDEO else MediaKind.PHOTO,
            takenAtMillis = ((asset.creationDate?.timeIntervalSince1970 ?: 0.0) * 1_000).toLong(),
            sizeBytes = asset.fileSizeBytes(),
            durationMillis = (asset.duration * 1_000).toLong().takeIf { isVideo },
            isLive = asset.mediaSubtypes.toLong() and PHAssetMediaSubtypePhotoLive.toLong() != 0L,
        )
    }

    override suspend fun delete(ids: List<String>): DeleteResult {
        if (ids.isEmpty()) return DeleteResult.Deleted(ids = emptyList(), freedBytes = 0)

        val assets = fetchAssets().filter { it.localIdentifier in ids }
        if (assets.isEmpty()) return DeleteResult.Failed(reason = "assets not found")
        val freedBytes = assets.sumOf { it.fileSizeBytes() }

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
                    continuation.resume(result)
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
