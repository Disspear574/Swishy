package com.disspear574.swishy.probe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.media.DeleteResult
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.openSystemTrash
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.delete_cancelled
import com.disspear574.swishy.strings.delete_done_body
import com.disspear574.swishy.strings.delete_done_title
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
    val scope = rememberCoroutineScope()
    var inFlight by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth().padding(16.dp)) {
        if (trashedIds.isNotEmpty()) {
            ProbeButton(
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
            )
        }

        when (val result = outcome) {
            null -> Unit

            is DeleteResult.Deleted -> {
                Spacer(Modifier.height(8.dp))
                ProbeText(
                    text = stringResource(
                        Res.string.delete_done_title,
                        formatSize(result.freedBytes).label(),
                    ),
                )
                ProbeText(text = stringResource(Res.string.delete_done_body), maxLines = 4)
                Spacer(Modifier.height(8.dp))
                ProbeButton(
                    text = stringResource(Res.string.delete_open_recently_deleted),
                    onClick = { openSystemTrash() },
                )
            }

            DeleteResult.Cancelled -> ProbeText(stringResource(Res.string.delete_cancelled))

            is DeleteResult.Failed -> ProbeText(
                text = stringResource(Res.string.delete_failed, result.reason),
                maxLines = 3,
            )
        }
    }
}
