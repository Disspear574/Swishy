package com.disspear574.swishy.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.disspear574.swishy.decisions.Decision
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.designsystem.components.BackButton
import com.disspear574.swishy.designsystem.components.ConfirmDialog
import com.disspear574.swishy.designsystem.components.MonthRow
import com.disspear574.swishy.designsystem.components.SwishyText
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.a11y_back
import com.disspear574.swishy.strings.confirm_no
import com.disspear574.swishy.strings.confirm_yes
import com.disspear574.swishy.strings.settings_about
import com.disspear574.swishy.strings.settings_count
import com.disspear574.swishy.strings.settings_kept_body
import com.disspear574.swishy.strings.settings_kept_title
import com.disspear574.swishy.strings.settings_none
import com.disspear574.swishy.strings.settings_title
import com.disspear574.swishy.strings.settings_trashed_body
import com.disspear574.swishy.strings.settings_trashed_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    store: DecisionStore,
    onBack: () -> Unit,
    onChanged: () -> Unit,
) {
    val spacing = SwishyTheme.spacing
    val scope = rememberCoroutineScope()

    var pending by remember { mutableStateOf<Decision?>(null) }
    var version by remember { mutableStateOf(0) }
    val kept = remember(version) { store.count(Decision.KEPT) }
    val trashed = remember(version) { store.count(Decision.TRASHED) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
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
                text = stringResource(Res.string.settings_title),
                style = SwishyTheme.typography.title,
                modifier = Modifier.weight(1f),
            )
        }

        if (kept == 0 && trashed == 0) {
            SwishyText(
                text = stringResource(Res.string.settings_none),
                style = SwishyTheme.typography.body,
                color = SwishyTheme.colors.inkDim,
                modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.medium),
            )
        }

        if (kept > 0) {
            ResetRow(
                title = stringResource(Res.string.settings_kept_title),
                subtitle = stringResource(Res.string.settings_count, kept),
                onClick = { pending = Decision.KEPT },
            )
        }
        if (trashed > 0) {
            ResetRow(
                title = stringResource(Res.string.settings_trashed_title),
                subtitle = stringResource(Res.string.settings_count, trashed),
                onClick = { pending = Decision.TRASHED },
            )
        }

        SwishyText(
            text = stringResource(Res.string.settings_about),
            style = SwishyTheme.typography.caption,
            color = SwishyTheme.colors.inkFaint,
            maxLines = ABOUT_LINES,
            modifier = Modifier.padding(spacing.screen),
        )
    }

    pending?.let { decision ->
        val isKept = decision == Decision.KEPT
        ConfirmDialog(
            title = stringResource(
                if (isKept) Res.string.settings_kept_title else Res.string.settings_trashed_title,
            ),
            body = stringResource(
                if (isKept) Res.string.settings_kept_body else Res.string.settings_trashed_body,
                if (isKept) kept else trashed,
            ),
            confirmText = stringResource(Res.string.confirm_yes),
            cancelText = stringResource(Res.string.confirm_no),
            onDismiss = { pending = null },
            onConfirm = {
                store.forgetAll(decision)
                pending = null
                version += 1
                scope.launch {
                    store.commit()
                    onChanged()
                }
            },
        )
    }
}

@Composable
private fun ResetRow(title: String, subtitle: String, onClick: () -> Unit) {
    MonthRow(
        title = title,
        subtitle = subtitle,
        size = "",
        onClick = onClick,
        contentDescription = title,
    )
}

private const val ABOUT_LINES = 6
