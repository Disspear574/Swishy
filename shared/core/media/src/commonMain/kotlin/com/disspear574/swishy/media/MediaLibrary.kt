package com.disspear574.swishy.media

/** Boundary between common code and the platform photo library. */
interface MediaLibrary : AlbumLibrary {

    suspend fun permissionState(): PermissionState

    suspend fun requestPermission(): PermissionState

    // Sizes may be zero here: on iOS each costs a resource lookup, so callers ask [sizeOf].
    suspend fun allAssets(): List<MediaAsset>

    suspend fun sizeOf(ids: List<String>): Map<String, Long>

    suspend fun assets(month: MonthKey): List<MediaAsset>

    suspend fun assets(ids: List<String>): List<MediaAsset>

    suspend fun delete(ids: List<String>): DeleteResult
}

enum class PermissionState { NOT_DETERMINED, GRANTED, LIMITED, DENIED }

sealed interface DeleteResult {

    data class Deleted(val ids: List<String>, val freedBytes: Long) : DeleteResult

    data object Cancelled : DeleteResult

    data class Failed(val reason: String) : DeleteResult
}
