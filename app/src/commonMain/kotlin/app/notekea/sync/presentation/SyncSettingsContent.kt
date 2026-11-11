package app.notekea.sync.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.ErrorBanner
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.NotekeaScrollArea
import app.notekea.core.presentation.designsystem.components.NotekeaSegment
import app.notekea.core.presentation.designsystem.components.NotekeaSegmentedControl
import app.notekea.core.presentation.designsystem.components.NotekeaTextField
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.settings_section_sync
import app.notekea.resources.sync_auto
import app.notekea.resources.sync_auto_explain
import app.notekea.resources.sync_connect
import app.notekea.resources.sync_disconnect
import app.notekea.resources.sync_error_address
import app.notekea.resources.sync_error_auth
import app.notekea.resources.sync_error_failed
import app.notekea.resources.sync_error_offline
import app.notekea.resources.sync_interval
import app.notekea.resources.sync_intro
import app.notekea.resources.sync_minutes
import app.notekea.resources.sync_no_vault
import app.notekea.resources.sync_now
import app.notekea.resources.sync_token_hint
import app.notekea.resources.sync_token_label
import app.notekea.resources.sync_token_saved
import app.notekea.resources.sync_url_hint
import app.notekea.resources.sync_url_label
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Settings → Sync: what it does, the repository's address and token, auto-sync and how often it looks for changes,
 * then Connect, Sync now and Stop syncing; the vault's status beside them and the last error over them.
 */
@Composable
fun SyncSettingsContent(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    NotekeaScrollArea(
        state = scrollState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
                .padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            Text(
                text = stringResource(resource = Res.string.settings_section_sync),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Quiet(text = stringResource(resource = Res.string.sync_intro))

            if (state.hasVault) {
                Form(
                    state = state,
                    onIntent = onIntent
                )
            } else {
                Quiet(text = stringResource(resource = Res.string.sync_no_vault))
            }
        }
    }
}

@Composable
private fun Form(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit
) {
    state.error?.let { ErrorBanner(message = messageOf(error = it)) }

    Field(label = stringResource(resource = Res.string.sync_url_label)) {
        NotekeaTextField(
            value = state.url,
            onValueChange = { onIntent(SyncSettingsIntent.OnUrlChange(url = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(resource = Res.string.sync_url_hint)
        )
    }

    Field(label = stringResource(resource = Res.string.sync_token_label)) {
        NotekeaTextField(
            value = state.token,
            onValueChange = { onIntent(SyncSettingsIntent.OnTokenChange(token = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(resource = Res.string.sync_token_hint),
            isSecret = true,
            onSubmit = { onIntent(SyncSettingsIntent.OnConnect) }
        )

        if (state.hasToken) Quiet(text = stringResource(resource = Res.string.sync_token_saved))
    }

    AutoSync(
        isAuto = state.isAuto,
        onToggle = { onIntent(SyncSettingsIntent.OnAutoToggle) }
    )

    Interval(
        state = state,
        onIntent = onIntent
    )

    Actions(
        state = state,
        onIntent = onIntent
    )
}

@Composable
private fun AutoSync(
    isAuto: Boolean,
    onToggle: () -> Unit
) {
    val label = stringResource(resource = Res.string.sync_auto)

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotekeaCheckbox(
                checked = isAuto,
                onClick = onToggle,
                label = label
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Quiet(text = stringResource(resource = Res.string.sync_auto_explain))
    }
}

@Composable
private fun Interval(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit
) {
    Field(label = stringResource(resource = Res.string.sync_interval)) {
        NotekeaSegmentedControl(
            segments = SyncSettings.INTERVALS.map {
                NotekeaSegment(
                    label = pluralStringResource(
                        resource = Res.plurals.sync_minutes,
                        quantity = it,
                        it
                    )
                )
            },
            selectedIndex = SyncSettings
                .INTERVALS
                .indexOf(state.intervalMinutes)
                .coerceAtLeast(minimumValue = 0),
            onSelect = {
                onIntent(SyncSettingsIntent.OnIntervalChoose(minutes = SyncSettings.INTERVALS[it]))
            }
        )
    }
}

@Composable
private fun Actions(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaButton(
            text = stringResource(resource = Res.string.sync_connect),
            onClick = { onIntent(SyncSettingsIntent.OnConnect) },
            enabled = state.canConnect
        )

        if (state.isConnected) {
            NotekeaButton(
                text = stringResource(resource = Res.string.sync_now),
                onClick = { onIntent(SyncSettingsIntent.OnSyncNow) },
                style = NotekeaButtonStyle.SECONDARY,
                enabled = !state.isBusy
            )

            NotekeaButton(
                text = stringResource(resource = Res.string.sync_disconnect),
                onClick = { onIntent(SyncSettingsIntent.OnDisconnect) },
                style = NotekeaButtonStyle.TEXT
            )

            SyncStatusItem(
                status = state.status,
                onClick = { onIntent(SyncSettingsIntent.OnSyncNow) }
            )
        }
    }
}

@Composable
private fun Field(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        content()
    }
}

@Composable
private fun Quiet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun messageOf(error: SyncError): String = when (error) {
    SyncError.AuthFailed -> stringResource(resource = Res.string.sync_error_auth)
    SyncError.Offline -> stringResource(resource = Res.string.sync_error_offline)
    SyncError.BadAddress -> stringResource(resource = Res.string.sync_error_address)
    is SyncError.Failed -> stringResource(resource = Res.string.sync_error_failed, error.message)
}

@Preview
@Composable
private fun SyncSettingsContentPreview() {
    NotekeaPreviewThemes {
        SyncSettingsContent(
            state = SyncSettingsState(
                hasVault = true,
                url = "https://github.com/ana/notes.git",
                hasToken = true,
                isConnected = true,
                status = SyncStatus.UpToDate
            ),
            onIntent = {}
        )
    }
}
