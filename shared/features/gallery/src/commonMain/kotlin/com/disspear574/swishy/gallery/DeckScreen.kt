package com.disspear574.swishy.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import com.disspear574.swishy.decisions.Decision
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.Deck
import com.disspear574.swishy.decisions.formatSize
import com.disspear574.swishy.designsystem.components.BackButton
import com.disspear574.swishy.designsystem.components.HintBar
import com.disspear574.swishy.designsystem.components.ProgressTrack
import com.disspear574.swishy.designsystem.components.SwipeDeck
import com.disspear574.swishy.designsystem.components.SwipeVerdict
import com.disspear574.swishy.designsystem.components.SwishyButton
import com.disspear574.swishy.designsystem.components.SwishyButtonTone
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.components.TrackMark
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.DeleteResult
import com.disspear574.swishy.media.MediaKind
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.media.PhotoCard
import com.disspear574.swishy.media.VideoCard
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_back
import com.disspear574.swishy.strings.deck_finished
import com.disspear574.swishy.strings.deck_hint_keep
import com.disspear574.swishy.strings.deck_hint_trash
import com.disspear574.swishy.strings.deck_keep
import com.disspear574.swishy.strings.deck_progress
import com.disspear574.swishy.strings.deck_trash
import com.disspear574.swishy.strings.deck_undo
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
internal fun DeckScreen(
    library: MediaLibrary,
    store: DecisionStore,
    month: MonthKey,
    onBack: () -> Unit,
) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing

    var deck by remember(month) { mutableStateOf<Deck?>(null) }
    var reloadToken by remember(month) { mutableIntStateOf(0) }
    var outcome by remember(month) { mutableStateOf<DeleteResult?>(null) }
    var progress by remember(month) { mutableFloatStateOf(0f) }
    var liveId by remember(month) { mutableStateOf<String?>(null) }

    LaunchedEffect(month, reloadToken) { deck = Deck.of(library.assets(month), store) }

    val current = deck ?: return
    val asset = current.current
    val wash = if (progress >= 0f) colors.keep else colors.trash

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                if (progress != 0f) {
                    drawRect(color = wash, alpha = abs(progress) * WASH_ALPHA)
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen, vertical = spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(
                onClick = onBack,
                contentDescription = stringResource(Res.string.a11y_back),
            )
            Spacer(Modifier.width(spacing.medium))
            SwishyText(
                text = month.displayName(),
                style = SwishyTheme.typography.title,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(spacing.medium))
            SwishyText(
                text = stringResource(
                    Res.string.deck_progress,
                    current.decided + if (asset == null) 0 else 1,
                    current.total,
                ),
                style = SwishyTheme.typography.numeric,
                color = colors.inkDim,
            )
        }

        if (asset == null) {
            DeckFinished(
                library = library,
                store = store,
                outcome = outcome,
                onOutcome = { result -> outcome = result },
                onDeleted = { ids ->
                    ids.forEach(store::forget)
                    deck = null
                    reloadToken += 1
                },
                modifier = Modifier.weight(1f),
            )
            return@Column
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.screen, vertical = spacing.small),
        ) {
            SwipeDeck(
                items = current.upcoming(DECK_DEPTH),
                key = { it.id },
                keepLabel = stringResource(Res.string.deck_keep),
                trashLabel = stringResource(Res.string.deck_trash),
                onVerdict = { _, verdict ->
                    progress = 0f
                    deck = current.decide(
                        if (verdict == SwipeVerdict.Keep) Decision.KEPT else Decision.TRASHED,
                    )
                },
                onProgressChange = { value -> progress = value },
            ) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(SwishyTheme.shapes.card)
                        .background(colors.surfaceSunk)
                        .then(
                            if (item.isLive) {
                                Modifier.holdToPlayLive(
                                    key = item.id,
                                    onChange = { held -> liveId = item.id.takeIf { held } },
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    if (item.kind == MediaKind.VIDEO) {
                        VideoCard(id = item.id, modifier = Modifier.fillMaxSize())
                    } else {
                        PhotoCard(
                            id = item.id,
                            modifier = Modifier.fillMaxSize(),
                            live = item.isLive,
                            playingLive = liveId == item.id,
                        )
                    }
                }
            }
        }

        ProgressTrack(
            marks = current.timeline().map { decision ->
                when (decision) {
                    Decision.KEPT -> TrackMark.Kept
                    Decision.TRASHED -> TrackMark.Trashed
                    null -> TrackMark.Pending
                }
            },
            modifier = Modifier.padding(horizontal = spacing.screen),
        )

        HintBar(
            trashHint = stringResource(Res.string.deck_hint_trash),
            keepHint = stringResource(Res.string.deck_hint_keep),
            modifier = Modifier.padding(
                horizontal = spacing.screen,
                vertical = spacing.medium,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen, vertical = spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwishyText(
                text = formatSize(asset.sizeBytes).label(),
                style = SwishyTheme.typography.numeric,
                color = colors.inkFaint,
                modifier = Modifier.weight(1f),
            )
            if (current.decided > 0) {
                SwishyButton(
                    text = stringResource(Res.string.deck_undo),
                    tone = SwishyButtonTone.Quiet,
                    onClick = { deck = current.undo() },
                )
            }
        }

        DeleteBar(
            library = library,
            trashedIds = remember(current) { store.trashedIds() },
            trashedBytes = remember(current) { store.trashedBytes() },
            outcome = outcome,
            onOutcome = { result -> outcome = result },
            onDeleted = { ids ->
                ids.forEach(store::forget)
                deck = null
                reloadToken += 1
            },
            modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.small),
        )
    }
}

@Composable
private fun DeckFinished(
    library: MediaLibrary,
    store: DecisionStore,
    outcome: DeleteResult?,
    onOutcome: (DeleteResult) -> Unit,
    onDeleted: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SwishyTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screen),
        verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.CenterVertically),
    ) {
        SwishyText(
            text = stringResource(Res.string.deck_finished),
            style = SwishyTheme.typography.title,
        )
        DeleteBar(
            library = library,
            trashedIds = store.trashedIds(),
            trashedBytes = store.trashedBytes(),
            outcome = outcome,
            onOutcome = onOutcome,
            onDeleted = onDeleted,
        )
    }
}

private fun Modifier.holdToPlayLive(key: Any, onChange: (Boolean) -> Unit): Modifier =
    pointerInput(key) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            val released = withTimeoutOrNull(LIVE_HOLD_MILLIS) { waitForUpOrCancellation() }
            if (released == null) {
                onChange(true)
                waitForUpOrCancellation()
                onChange(false)
            }
        }
    }

private const val LIVE_HOLD_MILLIS = 350L

private const val WASH_ALPHA = 0.28f

private const val DECK_DEPTH = 3
