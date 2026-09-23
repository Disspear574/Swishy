package com.disspear574.swishy.media

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
