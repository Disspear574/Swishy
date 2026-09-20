package com.disspear574.swishy.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.designsystem.components.EmptyState
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.media.PermissionState
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.permission_body
import com.disspear574.swishy.strings.permission_grant
import com.disspear574.swishy.strings.permission_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun GalleryScreen(library: MediaLibrary, store: DecisionStore) {
    val scope = rememberCoroutineScope()
    var permission by remember { mutableStateOf<PermissionState?>(null) }
    var openMonth by remember { mutableStateOf<MonthKey?>(null) }

    LaunchedEffect(Unit) { permission = library.permissionState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SwishyTheme.colors.ground)
            .safeDrawingPadding(),
    ) {
        val state = permission
        val month = openMonth
        when {
            state == null -> Unit

            state != PermissionState.GRANTED && state != PermissionState.LIMITED -> EmptyState(
                title = stringResource(Res.string.permission_title),
                body = stringResource(Res.string.permission_body),
                actionLabel = stringResource(Res.string.permission_grant),
                onAction = { scope.launch { permission = library.requestPermission() } },
            )

            month == null -> MonthsScreen(
                library = library,
                store = store,
                onOpen = { opened -> openMonth = opened },
            )

            else -> DeckScreen(
                library = library,
                store = store,
                month = month,
                onBack = { openMonth = null },
            )
        }
    }
}
