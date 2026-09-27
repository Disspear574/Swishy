package com.disspear574.swishy.gallery

import androidx.compose.runtime.Composable
import com.disspear574.swishy.media.AlbumKind
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.album_favorites
import com.disspear574.swishy.strings.album_live
import com.disspear574.swishy.strings.album_long_videos
import com.disspear574.swishy.strings.album_on_this_day
import com.disspear574.swishy.strings.album_screenshots
import com.disspear574.swishy.strings.album_videos
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AlbumKind.title(): String = stringResource(
    when (this) {
        AlbumKind.SCREENSHOTS -> Res.string.album_screenshots
        AlbumKind.VIDEOS -> Res.string.album_videos
        AlbumKind.LONG_VIDEOS -> Res.string.album_long_videos
        AlbumKind.LIVE -> Res.string.album_live
        AlbumKind.FAVORITES -> Res.string.album_favorites
        AlbumKind.ON_THIS_DAY -> Res.string.album_on_this_day
    },
)
