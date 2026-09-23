package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.disspear574.swishy.designsystem.theme.SwishyTheme

data class AlbumChoice(val id: String, val title: String, val count: Int)

@Composable
fun AlbumPickerSheet(
    title: String,
    albums: List<AlbumChoice>,
    newAlbumPlaceholder: String,
    createText: String,
    cancelText: String,
    countText: @Composable (Int) -> String,
    onPick: (AlbumChoice) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing
    var draft by remember { mutableStateOf("") }
    val canCreate = draft.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SwishyTheme.shapes.card)
                .background(colors.surface)
                .padding(spacing.screen),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            SwishyText(text = title, style = SwishyTheme.typography.title, maxLines = 2)

            if (albums.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = LIST_MAX_HEIGHT),
                    verticalArrangement = Arrangement.spacedBy(spacing.tiny),
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumRow(album = album, countText = countText(album.count)) { onPick(album) }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SwishyTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = newAlbumPlaceholder,
                    onDone = { if (canCreate) onCreate(draft.trim()) },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(spacing.small))
                SwishyButton(
                    text = createText,
                    onClick = { onCreate(draft.trim()) },
                    enabled = canCreate,
                )
            }

            SwishyButton(
                text = cancelText,
                onClick = onDismiss,
                tone = SwishyButtonTone.Quiet,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AlbumRow(album: AlbumChoice, countText: String, onClick: () -> Unit) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SwishyTheme.shapes.surface)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = album.title }
            .padding(horizontal = spacing.medium, vertical = spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwishyText(
            text = album.title,
            style = SwishyTheme.typography.body,
            color = colors.ink,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(spacing.small))
        Box {
            SwishyText(
                text = countText,
                style = SwishyTheme.typography.caption,
                color = colors.inkDim,
            )
        }
    }
}

private val LIST_MAX_HEIGHT = 280.dp
