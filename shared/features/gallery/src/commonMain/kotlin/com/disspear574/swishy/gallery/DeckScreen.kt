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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.disspear574.swishy.designsystem.components.AlbumChoice
import com.disspear574.swishy.designsystem.components.AlbumPickerSheet
import com.disspear574.swishy.designsystem.components.BackButton
import com.disspear574.swishy.designsystem.components.HintBar
import com.disspear574.swishy.designsystem.components.ProgressTrack
import com.disspear574.swishy.designsystem.components.SwipeDeck
import com.disspear574.swishy.designsystem.components.SwipeHint
import com.disspear574.swishy.designsystem.components.SwipeVerdict
import com.disspear574.swishy.designsystem.components.SwishyButton
import com.disspear574.swishy.designsystem.components.SwishyButtonTone
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.components.TrackMark
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.AlbumResult
import com.disspear574.swishy.media.DeleteResult
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.MediaKind
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.PhotoCard
import com.disspear574.swishy.media.UserAlbum
import com.disspear574.swishy.media.VideoCard
import com.disspear574.swishy.media.isIn
import com.disspear574.swishy.media.monthKeyIn
import com.disspear574.swishy.media.prefetchMedia
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_back
import com.disspear574.swishy.strings.album_count
import com.disspear574.swishy.strings.album_create
import com.disspear574.swishy.strings.album_finished
import com.disspear574.swishy.strings.album_new_placeholder
import com.disspear574.swishy.strings.album_picker_title
import com.disspear574.swishy.strings.confirm_no
import com.disspear574.swishy.strings.deck_finished
import com.disspear574.swishy.strings.deck_finished_all
import com.disspear574.swishy.strings.deck_hint_keep
import com.disspear574.swishy.strings.deck_hint_move
import com.disspear574.swishy.strings.deck_hint_trash
import com.disspear574.swishy.strings.deck_keep
import com.disspear574.swishy.strings.deck_move
import com.disspear574.swishy.strings.deck_progress
import com.disspear574.swishy.strings.deck_trash
import com.disspear574.swishy.strings.deck_undo
import com.disspear574.swishy.strings.mix_title
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
internal fun DeckScreen(
    library: MediaLibrary,
    index: MediaIndex,
    store: DecisionStore,
    source: DeckSource,
    onBack: () -> Unit,
    onHintChange: (SwipeHint) -> Unit,
) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing
    val undoScope = rememberCoroutineScope()

    var deck by remember(source) { mutableStateOf<Deck?>(null) }
    // After a deletion the source is unchanged, so only this key relaunches the load.
    var reloadToken by remember(source) { mutableIntStateOf(0) }
    var outcome by remember(source) { mutableStateOf<DeleteResult?>(null) }
    var heldId by remember(source) { mutableStateOf<String?>(null) }
    var albumPick by remember(source) { mutableStateOf<CompletableDeferred<AlbumChoice?>?>(null) }
    var albums by remember { mutableStateOf<List<UserAlbum>>(emptyList()) }
    // Kept only for undo within this visit; the database does not store the album.
    val movedTo = remember(source) { mutableMapOf<String, String>() }

    val zone = remember { TimeZone.currentSystemDefault() }
    val scanned by index.assets.collectAsStateWithLifecycle()
    // Seeded once per visit so the mix does not reshuffle after every decision.
    val shuffleSeed = remember(source) { Random.nextLong() }

    LaunchedEffect(source, reloadToken, scanned) {
        index.load()
        val all = scanned ?: return@LaunchedEffect
        val assets = when (source) {
            is DeckSource.Month -> all.filter { it.monthKeyIn(zone) == source.key }
            is DeckSource.Album -> all.filter { it.isIn(source.kind) }
            is DeckSource.UserAlbum -> {
                val byId = all.associateBy { it.id }
                library.albumAssetIds(source.id).mapNotNull(byId::get)
            }
            DeckSource.Mix -> all.shuffled(Random(shuffleSeed))
        }
        // A user album still shows kept and moved photos; only trashed ones are hidden.
        val showDecided = if (source is DeckSource.UserAlbum) {
            setOf(Decision.MOVED, Decision.KEPT)
        } else {
            emptySet()
        }
        deck = Deck.of(assets, store, showDecided)
    }

    // Fetched per photo: iOS lists come without sizes, and a zero would understate the trash.
    var currentSize by remember(source) { mutableLongStateOf(0L) }

    val current = deck ?: return
    val asset = current.current

    LaunchedEffect(current) { prefetchMedia(current.upcoming(PREFETCH_DEPTH).map { it.id }) }

    // Clears the wash on leave so it does not linger over the months list.
    DisposableEffect(Unit) { onDispose { onHintChange(SwipeHint.None) } }

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
                    is DeckSource.UserAlbum -> source.title
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
                title = when (source) {
                    is DeckSource.Month -> stringResource(Res.string.deck_finished)
                    is DeckSource.UserAlbum -> stringResource(Res.string.album_finished)
                    else -> stringResource(Res.string.deck_finished_all)
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
                moveLabel = if (library.supportsAlbums) stringResource(Res.string.deck_move) else null,
                onVerdict = { item, verdict ->
                    onHintChange(SwipeHint.None)
                    val decision = when (verdict) {
                        SwipeVerdict.Keep -> Decision.KEPT
                        SwipeVerdict.Trash -> Decision.TRASHED
                        SwipeVerdict.Move -> {
                            // Suspends until an album is picked; dismissing returns the card.
                            val choice = CompletableDeferred<AlbumChoice?>()
                            albums = library.userAlbums()
                            albumPick = choice
                            val picked = choice.await()
                            albumPick = null
                            if (picked == null) return@SwipeDeck false
                            if (!library.addToAlbum(picked.id, listOf(item.id))) return@SwipeDeck false
                            movedTo[item.id] = picked.id
                            Decision.MOVED
                        }
                    }
                    val next = current.decide(decision = decision, sizeBytes = currentSize)
                    // Committed before the next card so a kill right after the swipe keeps the decision.
                    store.commit()
                    deck = next
                    true
                },
                onHintChange = onHintChange,
            ) { item, depth ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(SwishyTheme.shapes.card)
                        .background(colors.surfaceSunk)
                        .then(
                            if (depth == 0 && (item.isLive || item.kind == MediaKind.VIDEO)) {
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
                            playing = depth == 0 && heldId == item.id,
                        )
                    } else {
                        PhotoCard(
                            id = item.id,
                            modifier = Modifier.fillMaxSize(),
                            live = item.isLive,
                            playingLive = heldId == item.id,
                            // The next card loads full size too, so iCloud downloads start before it is on top.
                            preview = depth >= PREVIEW_DEPTH,
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
                    Decision.MOVED -> TrackMark.Moved
                    null -> TrackMark.Pending
                }
            },
            modifier = Modifier.padding(horizontal = spacing.screen),
        )

        HintBar(
            trashHint = stringResource(Res.string.deck_hint_trash),
            keepHint = stringResource(Res.string.deck_hint_keep),
            moveHint = if (library.supportsAlbums) stringResource(Res.string.deck_hint_move) else null,
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
                    onClick = {
                        current.lastDecided?.let { last ->
                            movedTo.remove(last.id)?.let { albumId ->
                                undoScope.launch { library.removeFromAlbum(albumId, listOf(last.id)) }
                            }
                        }
                        deck = current.undo()
                    },
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

    albumPick?.let { pick ->
        AlbumPicker(
            pick = pick,
            albums = albums,
            library = library,
            onAlbumsChanged = { albums = it },
        )
    }
}

@Composable
private fun AlbumPicker(
    pick: CompletableDeferred<AlbumChoice?>,
    albums: List<UserAlbum>,
    library: MediaLibrary,
    onAlbumsChanged: (List<UserAlbum>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    AlbumPickerSheet(
        title = stringResource(Res.string.album_picker_title),
        albums = albums.map { AlbumChoice(id = it.id, title = it.title, count = it.count) },
        newAlbumPlaceholder = stringResource(Res.string.album_new_placeholder),
        createText = stringResource(Res.string.album_create),
        cancelText = stringResource(Res.string.confirm_no),
        countText = { count -> pluralStringResource(Res.plurals.album_count, count, count) },
        onPick = { choice -> pick.complete(choice) },
        onCreate = { title ->
            scope.launch {
                when (val result = library.createAlbum(title)) {
                    is AlbumResult.Done -> {
                        onAlbumsChanged(albums + result.album)
                        pick.complete(AlbumChoice(result.album.id, result.album.title, 0))
                    }
                    // The sheet stays open so an existing album can still be picked.
                    AlbumResult.Cancelled, is AlbumResult.Failed -> Unit
                }
            }
        },
        onDismiss = { pick.complete(null) },
    )
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
            // Observes without consuming, so the swipe stays the primary gesture.
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

private const val PREVIEW_DEPTH = 2

// Headroom for swiping faster than iCloud downloads; warming is cumulative.
private const val PREFETCH_DEPTH = 8
