package com.disspear574.swishy.media

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlbumsTest {

    private val today = LocalDate(2026, 9, 27)
    private val utc = TimeZone.UTC

    @Suppress("LongParameterList")
    private fun asset(
        id: String,
        kind: MediaKind = MediaKind.PHOTO,
        size: Long = 100,
        live: Boolean = false,
        screenshot: Boolean = false,
        favorite: Boolean = false,
        takenAt: Long = 0,
        duration: Long? = null,
    ) = MediaAsset(
        id = id,
        kind = kind,
        takenAtMillis = takenAt,
        sizeBytes = size,
        durationMillis = duration,
        isLive = live,
        isScreenshot = screenshot,
        isFavorite = favorite,
    )

    @Test
    fun `empty collections are not shown`() {
        val summaries = listOf(asset("a")).albumSummaries(today, utc)

        assertTrue(summaries.isEmpty(), "expected an empty list, got: $summaries")
    }

    @Test
    fun `an asset falls into every collection it matches`() {
        val assets = listOf(
            asset("screen", screenshot = true, favorite = true, size = 10),
            asset("clip", kind = MediaKind.VIDEO, size = 1_000),
            asset("live", live = true, size = 20),
        )

        val byAlbum = assets.albumSummaries(today, utc).associateBy { it.album }

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
        val summaries = listOf(asset("live", live = true)).albumSummaries(today, utc)

        assertEquals(listOf(AlbumKind.LIVE), summaries.map { it.album })
    }

    @Test
    fun `on this day takes the same date in earlier years only`() {
        val earlier = asset("earlier", takenAt = 1_727_431_200_000) // 27 Sep 2024 10:00 UTC
        val thisYear = asset("this-year", takenAt = 1_790_503_200_000) // 27 Sep 2026 10:00 UTC
        val dayBefore = asset("day-before", takenAt = 1_758_880_800_000) // 26 Sep 2025 10:00 UTC

        assertTrue(earlier.isIn(AlbumKind.ON_THIS_DAY, today, utc))
        assertTrue(!thisYear.isIn(AlbumKind.ON_THIS_DAY, today, utc))
        assertTrue(!dayBefore.isIn(AlbumKind.ON_THIS_DAY, today, utc))
    }

    @Test
    fun `long videos run for a minute or more`() {
        val long = asset("long", kind = MediaKind.VIDEO, duration = 60_000)
        val short = asset("short", kind = MediaKind.VIDEO, duration = 59_999)
        val photo = asset("photo", duration = 120_000)

        assertTrue(long.isIn(AlbumKind.LONG_VIDEOS, today, utc))
        assertTrue(!short.isIn(AlbumKind.LONG_VIDEOS, today, utc))
        assertTrue(!photo.isIn(AlbumKind.LONG_VIDEOS, today, utc))
    }
}
