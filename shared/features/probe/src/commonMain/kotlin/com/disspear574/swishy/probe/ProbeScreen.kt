package com.disspear574.swishy.probe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.Deck
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.media.DeleteResult
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.media.MonthSummary
import com.disspear574.swishy.media.PermissionState
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.deck_back
import com.disspear574.swishy.strings.deck_finished
import com.disspear574.swishy.strings.deck_open
import com.disspear574.swishy.strings.deck_progress
import com.disspear574.swishy.strings.deck_undo
import com.disspear574.swishy.strings.months_empty
import com.disspear574.swishy.strings.months_undecided
import com.disspear574.swishy.strings.permission_body
import com.disspear574.swishy.strings.permission_grant
import com.disspear574.swishy.strings.permission_title
import com.disspear574.swishy.strings.trash_banner
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProbeScreen(library: MediaLibrary, store: DecisionStore) {
    val scope = rememberCoroutineScope()
    var permission by remember { mutableStateOf<PermissionState?>(null) }
    var openMonth by remember { mutableStateOf<MonthKey?>(null) }

    LaunchedEffect(Unit) { permission = library.permissionState() }

    val state = permission
    val month = openMonth
    when {
        state == null -> Box(Modifier.fillMaxSize())

        state != PermissionState.GRANTED && state != PermissionState.LIMITED -> PermissionGate(
            onGrant = { scope.launch { permission = library.requestPermission() } },
        )

        month == null -> MonthsList(
            library = library,
            store = store,
            onOpen = { opened -> openMonth = opened },
        )

        else -> DeckHost(
            library = library,
            store = store,
            month = month,
            onBack = { openMonth = null },
        )
    }
}

@Composable
private fun PermissionGate(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        ProbeText(stringResource(Res.string.permission_title))
        Spacer(Modifier.height(8.dp))
        ProbeText(text = stringResource(Res.string.permission_body), maxLines = 6)
        Spacer(Modifier.height(16.dp))
        ProbeButton(text = stringResource(Res.string.permission_grant), onClick = onGrant)
    }
}

@Composable
private fun MonthsList(
    library: MediaLibrary,
    store: DecisionStore,
    onOpen: (MonthKey) -> Unit,
) {
    var months by remember { mutableStateOf<List<MonthSummary>?>(null) }
    LaunchedEffect(Unit) { months = library.months() }

    val loaded = months ?: return
    if (loaded.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ProbeText(text = stringResource(Res.string.months_empty), maxLines = 3)
        }
        return
    }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        val trashedBytes = store.trashedBytes()
        if (trashedBytes > 0) {
            ProbeText(
                text = stringResource(
                    Res.string.trash_banner,
                    store.trashedIds().size.toString(),
                    formatSize(trashedBytes).label(),
                ),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
        LazyColumn {
            items(loaded, key = { "${it.month.year}-${it.month.month}" }) { summary ->
                MonthRow(summary = summary, onClick = { onOpen(summary.month) })
            }
        }
    }
}

@Composable
private fun MonthRow(summary: MonthSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProbeText(
            text = "${summary.month.year}-${summary.month.month}",
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(12.dp))
        ProbeText(
            text = stringResource(Res.string.months_undecided, summary.count),
            maxLines = 1,
        )
        Spacer(Modifier.width(12.dp))
        ProbeText(text = formatSize(summary.sizeBytes).label(), maxLines = 1)
        Spacer(Modifier.width(12.dp))
        ProbeButton(text = stringResource(Res.string.deck_open), onClick = onClick)
    }
}

@Composable
private fun DeckHost(
    library: MediaLibrary,
    store: DecisionStore,
    month: MonthKey,
    onBack: () -> Unit,
) {
    var deck by remember(month) { mutableStateOf<Deck?>(null) }
    var reloadToken by remember(month) { mutableIntStateOf(0) }
    var outcome by remember(month) { mutableStateOf<DeleteResult?>(null) }
    LaunchedEffect(month, reloadToken) { deck = Deck.of(library.assets(month), store) }

    val current = deck ?: return
    val asset = current.current

    if (asset == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            ProbeText(stringResource(Res.string.deck_finished))
            Spacer(Modifier.height(16.dp))
            DeleteBar(
                library = library,
                trashedIds = remember(current) { store.trashedIds() },
                trashedBytes = remember(current) { store.trashedBytes() },
                outcome = outcome,
                onOutcome = { result -> outcome = result },
                onDeleted = { deletedIds ->
                    deletedIds.forEach(store::forget)
                    deck = null
                    reloadToken += 1
                },
            )
            ProbeButton(text = stringResource(Res.string.deck_back), onClick = onBack)
        }
        return
    }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(Modifier.weight(1f)) {
            DeckView(
                asset = asset,
                onDecision = { decision -> deck = current.decide(decision) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProbeText(
                text = stringResource(
                    Res.string.deck_progress,
                    current.decided + 1,
                    current.total,
                ),
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(12.dp))
            ProbeText(text = formatSize(asset.sizeBytes).label(), maxLines = 1)
            Spacer(Modifier.width(12.dp))
            ProbeButton(
                text = stringResource(Res.string.deck_undo),
                onClick = { deck = current.undo() },
            )
        }
        val trashedIds = remember(current) { store.trashedIds() }
        val trashedBytes = remember(current) { store.trashedBytes() }
        DeleteBar(
            library = library,
            trashedIds = trashedIds,
            trashedBytes = trashedBytes,
            outcome = outcome,
            onOutcome = { result -> outcome = result },
            onDeleted = { deletedIds ->
                deletedIds.forEach(store::forget)
                deck = null
                reloadToken += 1
            },
        )
        ProbeButton(
            text = stringResource(Res.string.deck_back),
            onClick = onBack,
            modifier = Modifier.padding(16.dp),
        )
    }
}
