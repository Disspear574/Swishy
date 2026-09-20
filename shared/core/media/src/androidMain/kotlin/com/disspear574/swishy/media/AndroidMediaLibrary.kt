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
    )

    override suspend fun permissionState(): PermissionState = MediaPermissions.state(context)

    override suspend fun requestPermission(): PermissionState {
        requestHost.requestPermissions(MediaPermissions.required())
        return MediaPermissions.state(context)
    }

    override suspend fun months(): List<MonthSummary> =
        readAll().groupIntoMonths(TimeZone.currentSystemDefault())

    override suspend fun assets(month: MonthKey): List<MediaAsset> {
        val zone = TimeZone.currentSystemDefault()
        return readAll().filter { asset -> asset.monthKeyIn(zone) == month }
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
            "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC",
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val takenIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val modifiedIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)

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
                )
            }
        }
        assets
    }

    override suspend fun delete(ids: List<String>): DeleteResult {
        if (ids.isEmpty()) return DeleteResult.Deleted(ids = emptyList(), freedBytes = 0)

        val freedBytes = readAll().filter { it.id in ids }.sumOf { it.sizeBytes }
        val uris = ids.mapNotNull { id ->
            id.toLongOrNull()?.let { numeric -> ContentUris.withAppendedId(collection, numeric) }
        }
        if (uris.isEmpty()) return DeleteResult.Failed(reason = "no valid ids")

        val request = MediaStore.createTrashRequest(context.contentResolver, uris, true)
        val confirmed = requestHost.launch(request.intentSender)

        return if (confirmed) {
            DeleteResult.Deleted(ids = ids, freedBytes = freedBytes)
        } else {
            DeleteResult.Cancelled
        }
    }

    private companion object {
        const val MILLIS_IN_SECOND = 1_000L
    }
}
