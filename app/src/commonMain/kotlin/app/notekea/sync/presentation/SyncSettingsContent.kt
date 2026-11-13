package app.notekea.sync.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSegment
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSegmentedControl
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSwitch
import app.notekea.core.presentation.designsystem.components.fields.NotekeaTextField
import app.notekea.core.presentation.designsystem.components.layout.NotekeaScrollArea
import app.notekea.core.presentation.designsystem.components.markdown.NotekeaCallout
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaColors
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.formatBytes
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
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
import app.notekea.resources.sync_too_large
import app.notekea.resources.sync_too_large_title
import app.notekea.resources.sync_url_hint
import app.notekea.resources.sync_url_label
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val FIELD_WIDTH_DP = 300
private const val PREVIEW_BYTES = 1_400_000_000L

/**
 * Settings → Sync: what it does, the repository's address and token, auto-sync and how often it looks for changes,
 * then Connect, Sync now and Stop syncing; the vault's status beside them and the last error over them.
 */
@Composable
fun SyncSettingsContent(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
    spotlight: SettingsSpotlight? = null
) {
    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_sync),
        modifier = modifier
    ) {
        if (state.hasVault) {
            Form(
                state = state,
                onIntent = onIntent,
                spotlight = spotlight
            )
        } else {
            Quiet(text = stringResource(resource = Res.string.sync_no_vault))
        }
    }
}

@Composable
private fun Form(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    state.error?.let { ErrorBanner(message = messageOf(error = it)) }

    if (state.isTooLargeForGitHub) TooLarge(bytes = state.vaultBytes ?: 0L)

    NotekeaSettingsGroup(
        rows = listOf(
            {
                NotekeaSpotlight(
                    shownAt = spotlight?.serialFor(place = SettingsPlace.SYNC_ADDRESS)
                ) {
                    val label = stringResource(resource = Res.string.sync_url_label)

                    NotekeaSettingsRow(label = label) {
                        NotekeaTextField(
                            value = state.url,
                            onValueChange = { onIntent(SyncSettingsIntent.OnUrlChange(url = it)) },
                            modifier = Modifier.width(width = FIELD_WIDTH_DP.dp),
                            placeholder = stringResource(resource = Res.string.sync_url_hint)
                        )
                    }
                }
            },
            {
                NotekeaSpotlight(
                    shownAt = spotlight?.serialFor(place = SettingsPlace.SYNC_TOKEN)
                ) {
                    Token(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        ),
        footnote = stringResource(resource = Res.string.sync_intro)
    )

    NotekeaSettingsGroup(
        rows = listOf(
            {
                NotekeaSpotlight(
                    shownAt = spotlight?.serialFor(place = SettingsPlace.SYNC_AUTO)
                ) {
                    AutoSync(
                        isAuto = state.isAuto,
                        onToggle = { onIntent(SyncSettingsIntent.OnAutoToggle) }
                    )
                }
            },
            {
                NotekeaSpotlight(
                    shownAt = spotlight?.serialFor(place = SettingsPlace.SYNC_INTERVAL)
                ) {
                    Interval(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        )
    )

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.SYNC_CONNECT)
    ) {
        Actions(
            state = state,
            onIntent = onIntent
        )
    }
}

@Composable
private fun Token(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit
) {
    val saved = stringResource(resource = Res.string.sync_token_saved).takeIf { state.hasToken }

    NotekeaSettingsRow(
        label = stringResource(resource = Res.string.sync_token_label),
        detail = saved
    ) {
        NotekeaTextField(
            value = state.token,
            onValueChange = { onIntent(SyncSettingsIntent.OnTokenChange(token = it)) },
            modifier = Modifier.width(width = FIELD_WIDTH_DP.dp),
            placeholder = stringResource(resource = Res.string.sync_token_hint),
            isSecret = true,
            onSubmit = { onIntent(SyncSettingsIntent.OnConnect) }
        )
    }
}

// Only for a GitHub remote, typed or connected: a vault kept on this computer is never told (2026-11-11).
@Composable
private fun TooLarge(bytes: Long) {
    NotekeaCallout(
        title = stringResource(resource = Res.string.sync_too_large_title),
        icon = NotekeaIcons.CalloutWarning,
        color = NotekeaColors.warning
    ) {
        Text(
            text = stringResource(
                resource = Res.string.sync_too_large,
                formatBytes(bytes = bytes)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AutoSync(
    isAuto: Boolean,
    onToggle: () -> Unit
) {
    val label = stringResource(resource = Res.string.sync_auto)

    NotekeaSettingsRow(
        label = label,
        detail = stringResource(resource = Res.string.sync_auto_explain)
    ) {
        NotekeaSwitch(
            isOn = isAuto,
            onToggle = { onToggle() },
            label = label
        )
    }
}

@Composable
private fun Interval(
    state: SyncSettingsState,
    onIntent: (SyncSettingsIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.sync_interval)

    NotekeaSettingsRow(label = label) {
        NotekeaPopUpButton(
            choice = pluralStringResource(
                Res.plurals.sync_minutes,
                state.intervalMinutes,
                state.intervalMinutes
            ),
            choices = SyncSettings.INTERVALS.map {
                NotekeaMenuItem(
                    label = pluralStringResource(Res.plurals.sync_minutes, it, it),
                    onClick = { onIntent(SyncSettingsIntent.OnIntervalChoose(minutes = it)) },
                    isChecked = it == state.intervalMinutes
                )
            },
            label = label
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
                status = SyncStatus.UpToDate,
                vaultBytes = PREVIEW_BYTES
            ),
            onIntent = {}
        )
    }
}
