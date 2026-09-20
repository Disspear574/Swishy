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
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.EmptyState
import com.disspear574.swishy.designsystem.components.HeroStat
import com.disspear574.swishy.designsystem.components.MonthRow
import com.disspear574.swishy.designsystem.components.StatLine
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.media.MonthSummary
import com.disspear574.swishy.media.groupIntoMonths
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_open_month
import com.disspear574.swishy.strings.a11y_open_trash
import com.disspear574.swishy.strings.months_empty
import com.disspear574.swishy.strings.months_title
import com.disspear574.swishy.strings.months_undecided
import com.disspear574.swishy.strings.trash_caption
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MonthsScreen(
    library: MediaLibrary,
    store: DecisionStore,
    onOpen: (MonthKey) -> Unit,
    onOpenTrash: () -> Unit,
) {
    val spacing = SwishyTheme.spacing
    var months by remember { mutableStateOf<List<MonthSummary>?>(null) }
    LaunchedEffect(Unit) {
        months = library.allAssets()
            .filter { asset -> store.decisionOf(asset.id) == null }
            .groupIntoMonths(TimeZone.currentSystemDefault())
    }

    val loaded = months ?: return

    if (loaded.isEmpty()) {
        EmptyState(
            title = stringResource(Res.string.months_title),
            body = stringResource(Res.string.months_empty),
        )
        return
    }

    val totalCount = loaded.sumOf { it.count }
    val totalBytes = loaded.sumOf { it.sizeBytes }
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
                modifier = Modifier.padding(
                    top = spacing.medium,
                    bottom = spacing.medium,
                ),
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

        items(loaded, key = { "${it.month.year}-${it.month.month}" }) { summary ->
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
    }
}
