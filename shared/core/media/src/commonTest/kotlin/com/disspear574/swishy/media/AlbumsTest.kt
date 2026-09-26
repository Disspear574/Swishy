package com.disspear574.swishy.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlbumsTest {

    @Suppress("LongParameterList")
    private fun asset(
        id: String,
        kind: MediaKind = MediaKind.PHOTO,
        size: Long = 100,
        live: Boolean = false,
        screenshot: Boolean = false,
        favorite: Boolean = false,
    ) = MediaAsset(
        id = id,
        kind = kind,
        takenAtMillis = 0,
        sizeBytes = size,
        durationMillis = null,
        isLive = live,
        isScreenshot = screenshot,
        isFavorite = favorite,
    )

    @Test
    fun `empty collections are not shown`() {
        val summaries = listOf(asset("a")).albumSummaries()

        assertTrue(summaries.isEmpty(), "expected an empty list, got: $summaries")
    }

    @Test
    fun `an asset falls into every collection it matches`() {
        val assets = listOf(
            asset("screen", screenshot = true, favorite = true, size = 10),
            asset("clip", kind = MediaKind.VIDEO, size = 1_000),
            asset("live", live = true, size = 20),
        )

        val byAlbum = assets.albumSummaries().associateBy { it.album }

        assertEquals(1, byAlbum[AlbumKind.SCREENSHOTS]?.count)
        assertEquals(1, byAlbum[AlbumKind.FAVORITES]?.count)
        assertEquals(1, byAlbum[AlbumKind.VIDEOS]?.count)
        assertEquals(1, byAlbum[AlbumKind.LIVE]?.count)
        // A favorite screenshot counts in both: collections overlap rather than partition.
        assertEquals(10, byAlbum[AlbumKind.FAVORITES]?.sizeBytes)
        assertEquals(1_000, byAlbum[AlbumKind.VIDEOS]?.sizeBytes)
    }

    @Test
    fun `a live photo stays a photo, not a video`() {
        val summaries = listOf(asset("live", live = true)).albumSummaries()

        assertEquals(listOf(AlbumKind.LIVE), summaries.map { it.album })
    }
}
