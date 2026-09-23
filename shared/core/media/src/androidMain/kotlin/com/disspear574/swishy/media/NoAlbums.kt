package com.disspear574.swishy.media

internal object NoAlbums : AlbumLibrary {

    override val supportsAlbums: Boolean = false

    override suspend fun userAlbums(): List<UserAlbum> = emptyList()

    override suspend fun createAlbum(title: String): AlbumResult =
        AlbumResult.Failed(reason = "albums are not supported on Android yet")

    override suspend fun addToAlbum(albumId: String, ids: List<String>): Boolean = false

    override suspend fun removeFromAlbum(albumId: String, ids: List<String>): Boolean = false

    override suspend fun albumAssetIds(albumId: String): List<String> = emptyList()
}
