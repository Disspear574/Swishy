package com.disspear574.swishy.media

interface MediaLibrary {

    suspend fun permissionState(): PermissionState

    suspend fun requestPermission(): PermissionState

    suspend fun allAssets(): List<MediaAsset>

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
