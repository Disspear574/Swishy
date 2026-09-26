package com.disspear574.swishy.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disspear574.swishy.decisions.Decision
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.db.HashStore
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.BackButton
import com.disspear574.swishy.designsystem.components.SectionLabel
import com.disspear574.swishy.designsystem.components.SelectableThumb
import com.disspear574.swishy.designsystem.components.SwishyButton
import com.disspear574.swishy.designsystem.components.SwishyButtonTone
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.DuplicateGroup
import com.disspear574.swishy.media.DuplicateKind
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.PhotoCard
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_back
import com.disspear574.swishy.strings.a11y_duplicate_frame
import com.disspear574.swishy.strings.duplicates_group_size
import com.disspear574.swishy.strings.duplicates_identical
import com.disspear574.swishy.strings.duplicates_keep_largest
import com.disspear574.swishy.strings.duplicates_none
import com.disspear574.swishy.strings.duplicates_scanning
import com.disspear574.swishy.strings.duplicates_similar
import com.disspear574.swishy.strings.duplicates_title
import com.disspear574.swishy.strings.duplicates_trash
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DuplicatesScreen(
    index: MediaIndex,
    store: DecisionStore,
    hashStore: HashStore,
    onBack: () -> Unit,
) {
    val spacing = SwishyTheme.spacing
    val scope = rememberCoroutineScope()

    val scanner = remember(index, hashStore) { DuplicateScanner(index, hashStore) }
    LaunchedEffect(scanner) { scanner.scan() }

    val progress by scanner.progress.collectAsStateWithLifecycle()
    val groups by scanner.groups.collectAsStateWithLifecycle()
    val sizes by index.sizes.collectAsStateWithLifecycle()
    val assets by index.assets.collectAsStateWithLifecycle()

    var selected by remember { mutableStateOf(emptySet<String>()) }
    var trashed by remember { mutableStateOf(emptySet<String>()) }

    // On iOS sizes arrive in a separate stream, so the scan value is the fallback.
    val sizeOf: (String) -> Long = remember(assets, sizes) {
        val fromScan = assets.orEmpty().associate { asset -> asset.id to asset.sizeBytes }
        val lookup: (String) -> Long = { id -> sizes[id] ?: fromScan[id] ?: 0L }
        lookup
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen, vertical = spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBack, contentDescription = stringResource(Res.string.a11y_back))
            Spacer(Modifier.width(spacing.medium))
            SwishyText(
                text = stringResource(Res.string.duplicates_title),
                style = SwishyTheme.typography.title,
                modifier = Modifier.weight(1f),
            )
        }

        val visible = groups.orEmpty()
            .map { group -> group.copy(ids = group.ids.filterNot { it in trashed }) }
            .filter { it.ids.size > 1 }

        when {
            groups == null -> ScanProgress(progress, Modifier.weight(1f))
            visible.isEmpty() -> Empty(Modifier.weight(1f))
            else -> Groups(
                groups = visible,
                selected = selected,
                sizeOf = sizeOf,
                onToggle = { id ->
                    selected = if (id in selected) selected - id else selected + id
                },
                onKeepLargest = { group ->
                    val largest = group.ids.maxByOrNull(sizeOf) ?: return@Groups
                    selected = selected + group.ids.filterNot { it == largest }
                },
                modifier = Modifier.weight(1f),
            )
        }

        if (selected.isNotEmpty()) {
            SwishyButton(
                text = stringResource(Res.string.duplicates_trash, selected.size),
                onClick = {
                    val going = selected
                    scope.launch {
                        going.forEach { id ->
                            store.record(id, Decision.TRASHED, sizeOf(id))
                        }
                        store.commit()
                        trashed = trashed + going
                        selected = emptySet()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screen, vertical = spacing.medium),
            )
        }
    }
}

@Composable
private fun ScanProgress(progress: DuplicateScanner.Progress, modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SwishyText(
            text = stringResource(Res.string.duplicates_scanning, progress.done, progress.total),
            style = SwishyTheme.typography.body,
            color = SwishyTheme.colors.inkDim,
        )
    }
}

@Composable
private fun Empty(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SwishyText(
            text = stringResource(Res.string.duplicates_none),
            style = SwishyTheme.typography.body,
            color = SwishyTheme.colors.inkDim,
        )
    }
}

@Composable
private fun Groups(
    groups: List<DuplicateGroup>,
    selected: Set<String>,
    sizeOf: (String) -> Long,
    onToggle: (String) -> Unit,
    onKeepLargest: (DuplicateGroup) -> Unit,
    modifier: Modifier,
) {
    val spacing = SwishyTheme.spacing

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = spacing.huge),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        items(groups.size) { position ->
            val group = groups[position]
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.screen),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        SectionLabel(
                            text = stringResource(
                                if (group.kind == DuplicateKind.IDENTICAL) {
                                    Res.string.duplicates_identical
                                } else {
                                    Res.string.duplicates_similar
                                },
                            ),
                        )
                        SwishyText(
                            text = stringResource(
                                Res.string.duplicates_group_size,
                                group.ids.size,
                                formatSize(group.ids.sumOf(sizeOf)).label(),
                            ),
                            style = SwishyTheme.typography.caption,
                            color = SwishyTheme.colors.inkDim,
                        )
                    }
                    Spacer(Modifier.width(spacing.small))
                    SwishyButton(
                        text = stringResource(Res.string.duplicates_keep_largest),
                        onClick = { onKeepLargest(group) },
                        tone = SwishyButtonTone.Quiet,
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = spacing.screen),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    items(group.ids.size) { itemPosition ->
                        val id = group.ids[itemPosition]
                        SelectableThumb(
                            selected = id in selected,
                            onToggle = { onToggle(id) },
                            contentDescription = stringResource(
                                Res.string.a11y_duplicate_frame,
                                formatSize(sizeOf(id)).label(),
                            ),
                            modifier = Modifier.width(THUMB_WIDTH).height(THUMB_HEIGHT),
                        ) {
                            PhotoCard(
                                id = id,
                                modifier = Modifier.fillMaxSize().aspectRatio(1f),
                                preview = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val THUMB_WIDTH = 120.dp
private val THUMB_HEIGHT = 160.dp
