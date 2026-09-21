package com.disspear574.swishy.gallery

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.designsystem.components.EmptyState
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.designsystem.theme.isReduceMotionEnabled
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.PermissionState
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.permission_body
import com.disspear574.swishy.strings.permission_grant
import com.disspear574.swishy.strings.permission_title
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
fun GalleryScreen(
    component: GalleryComponent,
    library: MediaLibrary,
    index: MediaIndex,
    store: DecisionStore,
) {
    val scope = rememberCoroutineScope()
    var permission by remember { mutableStateOf<PermissionState?>(null) }
    var swipeProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) { permission = library.permissionState() }

    val colors = SwishyTheme.colors
    val wash = if (swipeProgress >= 0f) colors.keep else colors.trash

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.ground)
            .drawBehind {
                if (swipeProgress != 0f) {
                    drawRect(color = wash, alpha = abs(swipeProgress) * WASH_ALPHA)
                }
            },
    ) {
        val state = permission
        when {
            state == null -> Unit

            state != PermissionState.GRANTED && state != PermissionState.LIMITED -> Box(
                Modifier.fillMaxSize().safeDrawingPadding(),
            ) {
                EmptyState(
                    title = stringResource(Res.string.permission_title),
                    body = stringResource(Res.string.permission_body),
                    actionLabel = stringResource(Res.string.permission_grant),
                    onAction = { scope.launch { permission = library.requestPermission() } },
                )
            }

            else -> GalleryStack(
                component = component,
                library = library,
                index = index,
                store = store,
                onSwipeProgress = { value -> swipeProgress = value },
            )
        }
    }
}

@Composable
private fun GalleryStack(
    component: GalleryComponent,
    library: MediaLibrary,
    index: MediaIndex,
    store: DecisionStore,
    onSwipeProgress: (Float) -> Unit,
) {
    val reduceMotion = isReduceMotionEnabled()

    Children(
        stack = component.stack,
        modifier = Modifier.fillMaxSize(),
        animation = stackAnimation(
            animator = if (reduceMotion) {
                fade(animationSpec = tween(durationMillis = FADE_MILLIS))
            } else {
                slide(animationSpec = tween(durationMillis = SLIDE_MILLIS)) +
                    fade(animationSpec = tween(durationMillis = SLIDE_MILLIS))
            },
        ),
    ) { child ->
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            when (val instance = child.instance) {
                GalleryComponent.Child.Months -> MonthsScreen(
                    index = index,
                    store = store,
                    onOpen = component::openMonth,
                    onOpenMix = component::openMix,
                    onOpenAlbum = component::openAlbum,
                    onOpenSettings = component::openSettings,
                    onOpenTrash = component::openTrash,
                )

                GalleryComponent.Child.Settings -> SettingsScreen(
                    store = store,
                    onBack = component::back,
                    onChanged = component::back,
                )

                GalleryComponent.Child.Trash -> TrashScreen(
                    library = library,
                    index = index,
                    store = store,
                    onBack = component::back,
                )

                is GalleryComponent.Child.Deck -> DeckScreen(
                    library = library,
                    index = index,
                    store = store,
                    source = instance.source,
                    onBack = component::back,
                    onSwipeProgress = onSwipeProgress,
                )
            }
        }
    }
}

private const val WASH_ALPHA = 0.28f

private const val SLIDE_MILLIS = 280
private const val FADE_MILLIS = 160
