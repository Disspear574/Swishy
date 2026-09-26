package com.disspear574.swishy.media

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSMutableArray
import platform.Foundation.NSSortDescriptor
import platform.Foundation.addObject
import platform.Photos.PHAsset
import platform.Photos.PHAssetCollection
import platform.Photos.PHAssetCollectionChangeRequest
import platform.Photos.PHAssetCollectionSubtypeAlbumRegular
import platform.Photos.PHAssetCollectionTypeAlbum
import platform.Photos.PHFetchOptions
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

/** PhotoKit albums: regular ones only, since smart and synced albums are read-only. */
internal object IosAlbums : AlbumLibrary {

    override val supportsAlbums: Boolean = true

    override suspend fun userAlbums(): List<UserAlbum> = fetchCollections().map { collection ->
        UserAlbum(
            id = collection.localIdentifier,
            title = collection.localizedTitle ?: "",
            count = collection.estimatedAssetCount.toInt(),
        )
    }

    override suspend fun createAlbum(title: String): AlbumResult {
        var placeholderId: String? = null
        val success = performChanges {
            placeholderId = PHAssetCollectionChangeRequest
                .creationRequestForAssetCollectionWithTitle(title)
                .placeholderForCreatedAssetCollection
                .localIdentifier
        }
        val id = placeholderId
        if (success !is ChangeOutcome.Done || id == null) {
            return when (success) {
                ChangeOutcome.Cancelled -> AlbumResult.Cancelled
                is ChangeOutcome.Failed -> AlbumResult.Failed(success.reason)
                else -> AlbumResult.Failed("album placeholder is missing")
            }
        }
        val created = fetchCollections().firstOrNull { it.localIdentifier == id }
        return if (created == null) {
            AlbumResult.Failed("album was created but cannot be fetched")
        } else {
            AlbumResult.Done(UserAlbum(id = id, title = created.localizedTitle ?: title, count = 0))
        }
    }

    override suspend fun addToAlbum(albumId: String, ids: List<String>): Boolean =
        edit(albumId, ids) { request, assets -> request.addAssets(assets) }

    override suspend fun removeFromAlbum(albumId: String, ids: List<String>): Boolean =
        edit(albumId, ids) { request, assets -> request.removeAssets(assets) }

    override suspend fun albumAssetIds(albumId: String): List<String> {
        val collection = fetchCollections().firstOrNull { it.localIdentifier == albumId }
            ?: return emptyList()
        val options = PHFetchOptions().apply {
            sortDescriptors = listOf(
                NSSortDescriptor.sortDescriptorWithKey("creationDate", ascending = false),
            )
        }
        val result = PHAsset.fetchAssetsInAssetCollection(collection, options)
        return buildList {
            for (index in 0uL until result.count) {
                (result.objectAtIndex(index) as? PHAsset)?.let { add(it.localIdentifier) }
            }
        }
    }

    private suspend fun edit(
        albumId: String,
        ids: List<String>,
        change: (PHAssetCollectionChangeRequest, NSMutableArray) -> Unit,
    ): Boolean {
        if (ids.isEmpty()) return true
        val collection = fetchCollections().firstOrNull { it.localIdentifier == albumId }
            ?: return false
        val assets = PHAsset.fetchAssetsWithLocalIdentifiers(ids, null)
        val batch = NSMutableArray()
        for (index in 0uL until assets.count) {
            assets.objectAtIndex(index)?.let(batch::addObject)
        }
        if (batch.count == 0uL) return false

        return performChanges {
            PHAssetCollectionChangeRequest.changeRequestForAssetCollection(collection)
                ?.let { request -> change(request, batch) }
        } is ChangeOutcome.Done
    }

    private fun fetchCollections(): List<PHAssetCollection> {
        val result = PHAssetCollection.fetchAssetCollectionsWithType(
            type = PHAssetCollectionTypeAlbum,
            subtype = PHAssetCollectionSubtypeAlbumRegular,
            options = null,
        )
        return buildList {
            for (index in 0uL until result.count) {
                (result.objectAtIndex(index) as? PHAssetCollection)?.let(::add)
            }
        }
    }

    private sealed interface ChangeOutcome {
        data object Done : ChangeOutcome
        data object Cancelled : ChangeOutcome
        data class Failed(val reason: String) : ChangeOutcome
    }

    private suspend fun performChanges(block: () -> Unit): ChangeOutcome =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = block,
                completionHandler = { success, error ->
                    val outcome = when {
                        success -> ChangeOutcome.Done
                        error?.code == USER_CANCELLED -> ChangeOutcome.Cancelled
                        else -> ChangeOutcome.Failed(error?.localizedDescription ?: "unknown")
                    }
                    // isActive guard: this bridge has crashed with "Already resumed".
                    if (continuation.isActive) continuation.resume(outcome)
                },
            )
        }

    private const val USER_CANCELLED = 3072L
}
