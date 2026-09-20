package com.disspear574.swishy.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.StatLine
import com.disspear574.swishy.designsystem.components.SwishyButton
import com.disspear574.swishy.designsystem.components.SwishyButtonTone
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.DeleteResult
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.openSystemTrash
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.delete_cancelled
import com.disspear574.swishy.strings.delete_done_body
import com.disspear574.swishy.strings.delete_done_caption
import com.disspear574.swishy.strings.delete_failed
import com.disspear574.swishy.strings.delete_open_recently_deleted
import com.disspear574.swishy.strings.trash_delete
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteBar(
    library: MediaLibrary,
    trashedIds: List<String>,
    trashedBytes: Long,
    outcome: DeleteResult?,
    onOutcome: (DeleteResult) -> Unit,
    onDeleted: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SwishyTheme.spacing
    val scope = rememberCoroutineScope()
    var inFlight by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        if (trashedIds.isNotEmpty()) {
            SwishyButton(
                text = stringResource(
                    Res.string.trash_delete,
                    trashedIds.size,
                    formatSize(trashedBytes).label(),
                ),
                enabled = !inFlight,
                onClick = {
                    inFlight = true
                    scope.launch {
                        val result = library.delete(trashedIds)
                        onOutcome(result)
                        if (result is DeleteResult.Deleted) {
                            onDeleted(result.ids)
                        }
                        inFlight = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        when (val result = outcome) {
            null -> Unit

            is DeleteResult.Deleted -> {
                StatLine(
                    value = formatSize(result.freedBytes).label(),
                    caption = stringResource(Res.string.delete_done_caption),
                )
                SwishyText(
                    text = stringResource(Res.string.delete_done_body),
                    style = SwishyTheme.typography.caption,
                    color = SwishyTheme.colors.inkDim,
                    maxLines = Int.MAX_VALUE,
                )
                SwishyButton(
                    text = stringResource(Res.string.delete_open_recently_deleted),
                    tone = SwishyButtonTone.Quiet,
                    onClick = { openSystemTrash() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            DeleteResult.Cancelled -> SwishyText(
                text = stringResource(Res.string.delete_cancelled),
                style = SwishyTheme.typography.caption,
                color = SwishyTheme.colors.inkDim,
            )

            is DeleteResult.Failed -> SwishyText(
                text = stringResource(Res.string.delete_failed, result.reason),
                style = SwishyTheme.typography.caption,
                color = SwishyTheme.colors.trash,
                maxLines = 3,
            )
        }
    }
}
