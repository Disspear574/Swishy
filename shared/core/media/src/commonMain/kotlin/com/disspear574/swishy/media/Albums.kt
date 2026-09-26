package com.disspear574.swishy.media

/** Collections computed from asset traits rather than platform albums, which Android lacks. */
enum class AlbumKind {
    SCREENSHOTS,
    VIDEOS,
    LIVE,
    FAVORITES,
}

fun MediaAsset.isIn(album: AlbumKind): Boolean = when (album) {
    AlbumKind.SCREENSHOTS -> isScreenshot
    AlbumKind.VIDEOS -> kind == MediaKind.VIDEO
    AlbumKind.LIVE -> isLive
    AlbumKind.FAVORITES -> isFavorite
}

data class AlbumSummary(
    val album: AlbumKind,
    val count: Int,
    val sizeBytes: Long,
)

fun List<MediaAsset>.albumSummaries(): List<AlbumSummary> =
    AlbumKind.entries.mapNotNull { album ->
        val assets = filter { asset -> asset.isIn(album) }
        if (assets.isEmpty()) {
            null
        } else {
            AlbumSummary(
                album = album,
                count = assets.size,
                sizeBytes = assets.sumOf { it.sizeBytes },
            )
        }
    }
