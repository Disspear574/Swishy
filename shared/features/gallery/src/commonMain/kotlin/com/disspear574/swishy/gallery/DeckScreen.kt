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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.MediaKind
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.PhotoCard
import com.disspear574.swishy.media.VideoCard
import com.disspear574.swishy.media.isIn
import com.disspear574.swishy.media.monthKeyIn
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_back
import com.disspear574.swishy.strings.deck_finished
import com.disspear574.swishy.strings.deck_finished_all
import com.disspear574.swishy.strings.deck_hint_keep
import com.disspear574.swishy.strings.deck_hint_trash
import com.disspear574.swishy.strings.deck_keep
import com.disspear574.swishy.strings.deck_progress
import com.disspear574.swishy.strings.deck_trash
import com.disspear574.swishy.strings.deck_undo
import com.disspear574.swishy.strings.mix_title
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
internal fun DeckScreen(
    library: MediaLibrary,
    index: MediaIndex,
    store: DecisionStore,
    source: DeckSource,
    onBack: () -> Unit,
    onSwipeProgress: (Float) -> Unit,
) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing

    var deck by remember(source) { mutableStateOf<Deck?>(null) }
    var reloadToken by remember(source) { mutableIntStateOf(0) }
    var outcome by remember(source) { mutableStateOf<DeleteResult?>(null) }
    var heldId by remember(source) { mutableStateOf<String?>(null) }

    val zone = remember { TimeZone.currentSystemDefault() }
    val scanned by index.assets.collectAsStateWithLifecycle()
    val shuffleSeed = remember(source) { Random.nextLong() }

    LaunchedEffect(source, reloadToken, scanned) {
        index.load()
        val all = scanned ?: return@LaunchedEffect
        val assets = when (source) {
            is DeckSource.Month -> all.filter { it.monthKeyIn(zone) == source.key }
            is DeckSource.Album -> all.filter { it.isIn(source.kind) }
            DeckSource.Mix -> all.shuffled(Random(shuffleSeed))
        }
        deck = Deck.of(assets, store)
    }

    var currentSize by remember(source) { mutableLongStateOf(0L) }

    val current = deck ?: return
    val asset = current.current

    DisposableEffect(Unit) { onDispose { onSwipeProgress(0f) } }

    LaunchedEffect(asset?.id) {
        val id = asset?.id ?: return@LaunchedEffect
        currentSize = if (asset.sizeBytes > 0) asset.sizeBytes else index.sizeOf(id)
    }
    Column(Modifier.fillMaxSize()) {
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
                text = when (source) {
                    is DeckSource.Month -> source.key.displayName()
                    is DeckSource.Album -> source.kind.title()
                    DeckSource.Mix -> stringResource(Res.string.mix_title)
                },
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
                title = if (source is DeckSource.Month) {
                    stringResource(Res.string.deck_finished)
                } else {
                    stringResource(Res.string.deck_finished_all)
                },
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
                    onSwipeProgress(0f)
                    val next = current.decide(
                        decision = if (verdict == SwipeVerdict.Keep) {
                            Decision.KEPT
                        } else {
                            Decision.TRASHED
                        },
                        sizeBytes = currentSize,
                    )
                    store.commit()
                    deck = next
                },
                onProgressChange = onSwipeProgress,
            ) { item, isTop ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(SwishyTheme.shapes.card)
                        .background(colors.surfaceSunk)
                        .then(
                            if (isTop && (item.isLive || item.kind == MediaKind.VIDEO)) {
                                Modifier.holdToPlay(
                                    key = item.id,
                                    onChange = { held -> heldId = item.id.takeIf { held } },
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    if (item.kind == MediaKind.VIDEO) {
                        VideoCard(
                            id = item.id,
                            modifier = Modifier.fillMaxSize(),
                            playing = isTop && heldId == item.id,
                        )
                    } else {
                        PhotoCard(
                            id = item.id,
                            modifier = Modifier.fillMaxSize(),
                            live = item.isLive,
                            playingLive = heldId == item.id,
                            preview = !isTop,
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
                text = formatSize(currentSize).label(),
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
                index.invalidate()
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
    title: String,
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
            text = title,
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

private fun Modifier.holdToPlay(key: Any, onChange: (Boolean) -> Unit): Modifier =
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

private const val DECK_DEPTH = 3
