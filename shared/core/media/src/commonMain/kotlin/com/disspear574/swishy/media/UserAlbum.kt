package com.disspear574.swishy.media

/** An album stored in the system library, unlike an [AlbumKind] collection computed by the app. */
data class UserAlbum(
    val id: String,
    val title: String,
    val count: Int,
)

sealed interface AlbumResult {

    data class Done(val album: UserAlbum) : AlbumResult

    data object Cancelled : AlbumResult

    data class Failed(val reason: String) : AlbumResult
}
