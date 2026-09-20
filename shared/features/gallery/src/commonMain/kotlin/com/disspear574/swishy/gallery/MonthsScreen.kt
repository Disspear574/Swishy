package com.disspear574.swishy.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.EmptyState
import com.disspear574.swishy.designsystem.components.MonthRow
import com.disspear574.swishy.designsystem.components.StatLine
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.media.MonthSummary
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_open_month
import com.disspear574.swishy.strings.months_empty
import com.disspear574.swishy.strings.months_title
import com.disspear574.swishy.strings.months_undecided
import com.disspear574.swishy.strings.trash_caption
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MonthsScreen(
    library: MediaLibrary,
    store: DecisionStore,
    onOpen: (MonthKey) -> Unit,
) {
    val spacing = SwishyTheme.spacing
    var months by remember { mutableStateOf<List<MonthSummary>?>(null) }
    LaunchedEffect(Unit) { months = library.months() }

    val loaded = months ?: return

    if (loaded.isEmpty()) {
        EmptyState(
            title = stringResource(Res.string.months_title),
            body = stringResource(Res.string.months_empty),
        )
        return
    }

    val trashedBytes = store.trashedBytes()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = spacing.screen,
            end = spacing.screen,
            top = spacing.medium,
            bottom = spacing.huge,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        item(key = "title") {
            Box(Modifier.padding(vertical = spacing.small), contentAlignment = Alignment.CenterStart) {
                SwishyText(
                    text = stringResource(Res.string.months_title),
                    style = SwishyTheme.typography.title,
                )
            }
        }

        if (trashedBytes > 0) {
            item(key = "trash") {
                StatLine(
                    value = formatSize(trashedBytes).label(),
                    caption = stringResource(Res.string.trash_caption),
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
                contentDescription = stringResource(Res.string.a11y_open_month, title, summary.count),
            )
        }
    }
}
