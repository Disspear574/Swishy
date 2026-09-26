package com.disspear574.swishy.media

/** User albums of the library; Android has folders instead, so callers check [supportsAlbums]. */
interface AlbumLibrary {

    val supportsAlbums: Boolean

    suspend fun userAlbums(): List<UserAlbum>

    suspend fun createAlbum(title: String): AlbumResult

    suspend fun addToAlbum(albumId: String, ids: List<String>): Boolean

    suspend fun removeFromAlbum(albumId: String, ids: List<String>): Boolean

    suspend fun albumAssetIds(albumId: String): List<String>
}
