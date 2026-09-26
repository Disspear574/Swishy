package com.disspear574.swishy.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.EmptyState
import com.disspear574.swishy.designsystem.components.HeroStat
import com.disspear574.swishy.designsystem.components.MonthRow
import com.disspear574.swishy.designsystem.components.SectionLabel
import com.disspear574.swishy.designsystem.components.StatLine
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.AlbumKind
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.media.UserAlbum
import com.disspear574.swishy.media.albumSummaries
import com.disspear574.swishy.media.groupIntoMonths
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_open_album
import com.disspear574.swishy.strings.a11y_open_duplicates
import com.disspear574.swishy.strings.a11y_open_mix
import com.disspear574.swishy.strings.a11y_open_month
import com.disspear574.swishy.strings.a11y_open_settings
import com.disspear574.swishy.strings.a11y_open_trash
import com.disspear574.swishy.strings.a11y_open_user_album
import com.disspear574.swishy.strings.album_count
import com.disspear574.swishy.strings.albums_section
import com.disspear574.swishy.strings.albums_user_section
import com.disspear574.swishy.strings.duplicates_subtitle
import com.disspear574.swishy.strings.duplicates_title
import com.disspear574.swishy.strings.mix_subtitle
import com.disspear574.swishy.strings.mix_title
import com.disspear574.swishy.strings.months_empty
import com.disspear574.swishy.strings.months_section
import com.disspear574.swishy.strings.months_title
import com.disspear574.swishy.strings.months_undecided
import com.disspear574.swishy.strings.settings_subtitle
import com.disspear574.swishy.strings.settings_title
import com.disspear574.swishy.strings.trash_caption
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MonthsScreen(
    library: MediaLibrary,
    index: MediaIndex,
    store: DecisionStore,
    onOpen: (MonthKey) -> Unit,
    onOpenMix: () -> Unit,
    onOpenAlbum: (AlbumKind) -> Unit,
    onOpenUserAlbum: (UserAlbum) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDuplicates: () -> Unit,
    onOpenTrash: () -> Unit,
) {
    val spacing = SwishyTheme.spacing

    LaunchedEffect(Unit) { index.load() }
    // Re-read on every return: a swipe up may have just created a new album.
    var userAlbums by remember { mutableStateOf<List<UserAlbum>>(emptyList()) }
    LaunchedEffect(Unit) { if (library.supportsAlbums) userAlbums = library.userAlbums() }
    val assets by index.assets.collectAsStateWithLifecycle()
    val sizes by index.sizes.collectAsStateWithLifecycle()

    val scanned = assets ?: return

    val undecided = remember(scanned, store) {
        scanned.filter { asset -> store.decisionOf(asset.id) == null }
    }
    val albums = remember(undecided, sizes) {
        undecided
            .map { asset -> asset.copy(sizeBytes = sizes[asset.id] ?: asset.sizeBytes) }
            .albumSummaries()
    }
    val months = remember(undecided, sizes) {
        undecided
            .map { asset -> asset.copy(sizeBytes = sizes[asset.id] ?: asset.sizeBytes) }
            .groupIntoMonths(TimeZone.currentSystemDefault())
    }

    if (months.isEmpty()) {
        EmptyState(
            title = stringResource(Res.string.months_title),
            body = stringResource(Res.string.months_empty),
        )
        return
    }

    val totalCount = months.sumOf { it.count }
    val totalBytes = months.sumOf { it.sizeBytes }
    val trashedBytes = store.trashedBytes()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screen,
            end = spacing.screen,
            top = spacing.small,
            bottom = spacing.huge,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        item(key = "hero") {
            HeroStat(
                eyebrow = stringResource(Res.string.months_title),
                value = formatSize(totalBytes).label(),
                caption = stringResource(Res.string.months_undecided, totalCount),
                modifier = Modifier.padding(top = spacing.medium, bottom = spacing.medium),
            )
        }

        item(key = "mix") {
            MonthRow(
                title = stringResource(Res.string.mix_title),
                subtitle = stringResource(Res.string.mix_subtitle),
                size = "",
                onClick = onOpenMix,
                contentDescription = pluralStringResource(Res.plurals.a11y_open_mix, totalCount, totalCount),
            )
        }

        if (trashedBytes > 0) {
            item(key = "trash") {
                StatLine(
                    value = formatSize(trashedBytes).label(),
                    caption = stringResource(Res.string.trash_caption),
                    onClick = onOpenTrash,
                    contentDescription = stringResource(Res.string.a11y_open_trash),
                )
            }
        }

        item(key = "albums-label") {
            SectionLabel(
                text = stringResource(Res.string.albums_section),
                modifier = Modifier.padding(top = spacing.medium),
            )
        }

        item(key = "duplicates") {
            MonthRow(
                title = stringResource(Res.string.duplicates_title),
                subtitle = stringResource(Res.string.duplicates_subtitle),
                size = "",
                onClick = onOpenDuplicates,
                contentDescription = stringResource(Res.string.a11y_open_duplicates),
            )
        }

        items(albums, key = { "album-${it.album.name}" }) { summary ->
            val title = summary.album.title()
            MonthRow(
                title = title,
                subtitle = stringResource(Res.string.months_undecided, summary.count),
                size = formatSize(summary.sizeBytes).label(),
                onClick = { onOpenAlbum(summary.album) },
                contentDescription = stringResource(
                    Res.string.a11y_open_album,
                    title,
                    summary.count,
                ),
            )
        }

        if (userAlbums.isNotEmpty()) {
            item(key = "user-albums-label") {
                SectionLabel(
                    text = stringResource(Res.string.albums_user_section),
                    modifier = Modifier.padding(top = spacing.medium),
                )
            }
        }

        items(userAlbums, key = { "user-album-${it.id}" }) { album ->
            MonthRow(
                title = album.title,
                subtitle = pluralStringResource(Res.plurals.album_count, album.count, album.count),
                size = "",
                onClick = { onOpenUserAlbum(album) },
                contentDescription = stringResource(
                    Res.string.a11y_open_user_album,
                    album.title,
                    album.count,
                ),
            )
        }

        item(key = "months-label") {
            SectionLabel(
                text = stringResource(Res.string.months_section),
                modifier = Modifier.padding(top = spacing.medium),
            )
        }

        items(months, key = { "${it.month.year}-${it.month.month}" }) { summary ->
            val title = summary.month.displayName()
            MonthRow(
                title = title,
                subtitle = stringResource(Res.string.months_undecided, summary.count),
                size = formatSize(summary.sizeBytes).label(),
                onClick = { onOpen(summary.month) },
                contentDescription = stringResource(
                    Res.string.a11y_open_month,
                    title,
                    summary.count,
                ),
            )
        }

        item(key = "settings") {
            MonthRow(
                title = stringResource(Res.string.settings_title),
                subtitle = stringResource(Res.string.settings_subtitle),
                size = "",
                onClick = onOpenSettings,
                contentDescription = stringResource(Res.string.a11y_open_settings),
                modifier = Modifier.padding(top = spacing.medium),
            )
        }
    }
}
