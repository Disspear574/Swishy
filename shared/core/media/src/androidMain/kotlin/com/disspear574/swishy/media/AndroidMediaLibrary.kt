package com.disspear574.swishy.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone

class AndroidMediaLibrary(
    private val context: Context,
    private val requestHost: SystemRequestHost,
) : MediaLibrary {

    private val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    private val columns = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.DURATION,
        MediaStore.Files.FileColumns.RELATIVE_PATH,
        MediaStore.Files.FileColumns.IS_FAVORITE,
    )

    override suspend fun permissionState(): PermissionState = MediaPermissions.state(context)

    override suspend fun requestPermission(): PermissionState {
        requestHost.requestPermissions(MediaPermissions.required())
        return MediaPermissions.state(context)
    }

    override suspend fun allAssets(): List<MediaAsset> = readAll()

    override suspend fun sizeOf(ids: List<String>): Map<String, Long> {
        val wanted = ids.toHashSet()
        return readAll().filter { it.id in wanted }.associate { it.id to it.sizeBytes }
    }

    override suspend fun assets(month: MonthKey): List<MediaAsset> {
        val zone = TimeZone.currentSystemDefault()
        return readAll().filter { asset -> asset.monthKeyIn(zone) == month }
    }

    override suspend fun assets(ids: List<String>): List<MediaAsset> {
        val byId = readAll().associateBy { it.id }
        return ids.mapNotNull(byId::get)
    }

    private suspend fun readAll(): List<MediaAsset> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )

        val assets = mutableListOf<MediaAsset>()
        context.contentResolver.query(
            collection,
            columns,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val takenIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val modifiedIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
            val pathIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
            val favoriteIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.IS_FAVORITE)

            while (cursor.moveToNext()) {
                val isVideo =
                    cursor.getInt(typeIndex) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

                val takenAt = cursor.getLong(takenIndex).takeIf { it > 0 }
                    ?: (cursor.getLong(modifiedIndex) * MILLIS_IN_SECOND)

                assets += MediaAsset(
                    id = cursor.getLong(idIndex).toString(),
                    kind = if (isVideo) MediaKind.VIDEO else MediaKind.PHOTO,
                    takenAtMillis = takenAt,
                    sizeBytes = cursor.getLong(sizeIndex),
                    durationMillis = cursor.getLong(durationIndex).takeIf { isVideo && it > 0 },
                    isScreenshot = cursor.getString(pathIndex)
                        ?.contains(SCREENSHOTS_FOLDER, ignoreCase = true) == true,
                    isFavorite = cursor.getInt(favoriteIndex) != 0,
                )
            }
        }
        assets.sortedByDescending { it.takenAtMillis }
    }

    @Suppress("ReturnCount")
    override suspend fun delete(ids: List<String>): DeleteResult {
        if (ids.isEmpty()) return DeleteResult.Deleted(ids = emptyList(), freedBytes = 0)

        val known = readAll().filter { it.id in ids }
        val freedBytes = known.sumOf { it.sizeBytes }

        val uris = known.mapNotNull { asset ->
            asset.id.toLongOrNull()?.let { numeric ->
                ContentUris.withAppendedId(asset.kind.collectionUri(), numeric)
            }
        }
        if (uris.isEmpty()) return DeleteResult.Failed(reason = "no media items")

        val request = withContext(Dispatchers.IO) {
            runCatching { MediaStore.createTrashRequest(context.contentResolver, uris, true) }
        }.getOrElse { error ->
            return DeleteResult.Failed(reason = error.message ?: "trash request failed")
        }

        val confirmed = requestHost.launch(request.intentSender)

        return if (confirmed) {
            DeleteResult.Deleted(ids = ids, freedBytes = freedBytes)
        } else {
            DeleteResult.Cancelled
        }
    }

    private fun MediaKind.collectionUri() = when (this) {
        MediaKind.PHOTO -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        MediaKind.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    private companion object {
        const val MILLIS_IN_SECOND = 1_000L

        const val SCREENSHOTS_FOLDER = "Screenshots"
    }
}
