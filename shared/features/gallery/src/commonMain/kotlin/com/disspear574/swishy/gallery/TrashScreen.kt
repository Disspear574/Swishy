package com.disspear574.swishy.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.BackButton
import com.disspear574.swishy.designsystem.components.EmptyState
import com.disspear574.swishy.designsystem.components.HeroStat
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.DeleteResult
import com.disspear574.swishy.media.MediaAsset
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.PhotoCard
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_back
import com.disspear574.swishy.strings.a11y_restore
import com.disspear574.swishy.strings.trash_caption
import com.disspear574.swishy.strings.trash_empty
import com.disspear574.swishy.strings.trash_restore_hint
import com.disspear574.swishy.strings.trash_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TrashScreen(
    library: MediaLibrary,
    store: DecisionStore,
    onBack: () -> Unit,
) {
    val spacing = SwishyTheme.spacing

    var reloadToken by remember { mutableIntStateOf(0) }
    var outcome by remember { mutableStateOf<DeleteResult?>(null) }
    var assets by remember { mutableStateOf<List<MediaAsset>?>(null) }

    LaunchedEffect(reloadToken) { assets = library.assets(store.trashedIds()) }

    val loaded = assets ?: return

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
                text = stringResource(Res.string.trash_title),
                style = SwishyTheme.typography.title,
                modifier = Modifier.weight(1f),
            )
        }

        if (loaded.isEmpty()) {
            EmptyState(
                title = stringResource(Res.string.trash_title),
                body = stringResource(Res.string.trash_empty),
                modifier = Modifier.weight(1f),
            )
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.small),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(GRID_COLUMNS) }) {
                Column {
                    HeroStat(
                        eyebrow = stringResource(Res.string.trash_title),
                        value = formatSize(store.trashedBytes()).label(),
                        caption = stringResource(Res.string.trash_caption),
                    )
                    SwishyText(
                        text = stringResource(Res.string.trash_restore_hint),
                        style = SwishyTheme.typography.caption,
                        color = SwishyTheme.colors.inkFaint,
                        maxLines = 2,
                        modifier = Modifier.padding(vertical = spacing.small),
                    )
                }
            }

            items(loaded, key = { it.id }) { asset ->
                val restoreLabel = stringResource(Res.string.a11y_restore)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(SwishyTheme.shapes.surface)
                        .semantics { contentDescription = restoreLabel }
                        .clickable {
                            store.forget(asset.id)
                            reloadToken += 1
                        },
                ) {
                    PhotoCard(id = asset.id, modifier = Modifier.fillMaxSize())
                }
            }
        }

        DeleteBar(
            library = library,
            trashedIds = remember(reloadToken) { store.trashedIds() },
            trashedBytes = remember(reloadToken) { store.trashedBytes() },
            outcome = outcome,
            onOutcome = { result -> outcome = result },
            onDeleted = { ids ->
                ids.forEach(store::forget)
                reloadToken += 1
            },
            modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.small),
        )
    }
}

private const val GRID_COLUMNS = 3
