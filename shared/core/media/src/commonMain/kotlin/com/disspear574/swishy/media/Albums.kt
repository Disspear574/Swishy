package com.disspear574.swishy.media

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Collections computed from asset traits rather than platform albums, which Android lacks. */
enum class AlbumKind {
    SCREENSHOTS,
    VIDEOS,
    LONG_VIDEOS,
    LIVE,
    FAVORITES,
    ON_THIS_DAY,
}

private const val LONG_VIDEO_MILLIS = 60_000L

fun todayIn(timeZone: TimeZone): LocalDate = Clock.System.now().toLocalDateTime(timeZone).date

fun MediaAsset.isIn(album: AlbumKind, today: LocalDate, timeZone: TimeZone): Boolean = when (album) {
    AlbumKind.SCREENSHOTS -> isScreenshot
    AlbumKind.VIDEOS -> kind == MediaKind.VIDEO
    AlbumKind.LONG_VIDEOS -> kind == MediaKind.VIDEO && (durationMillis ?: 0) >= LONG_VIDEO_MILLIS
    AlbumKind.LIVE -> isLive
    AlbumKind.FAVORITES -> isFavorite
    AlbumKind.ON_THIS_DAY -> {
        val taken = Instant.fromEpochMilliseconds(takenAtMillis).toLocalDateTime(timeZone).date
        taken.year < today.year && taken.month == today.month && taken.day == today.day
    }
}

data class AlbumSummary(
    val album: AlbumKind,
    val count: Int,
    val sizeBytes: Long,
)

fun List<MediaAsset>.albumSummaries(today: LocalDate, timeZone: TimeZone): List<AlbumSummary> =
    AlbumKind.entries.mapNotNull { album ->
        val assets = filter { asset -> asset.isIn(album, today, timeZone) }
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
