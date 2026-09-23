package com.disspear574.swishy.media

interface AlbumLibrary {

    val supportsAlbums: Boolean

    suspend fun userAlbums(): List<UserAlbum>

    suspend fun createAlbum(title: String): AlbumResult

    suspend fun addToAlbum(albumId: String, ids: List<String>): Boolean

    suspend fun removeFromAlbum(albumId: String, ids: List<String>): Boolean

    suspend fun albumAssetIds(albumId: String): List<String>
}
