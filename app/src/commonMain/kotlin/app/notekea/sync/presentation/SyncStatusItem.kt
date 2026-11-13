package app.notekea.sync.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.components.layout.NotekeaStatusLabel
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.sync_status_auth
import app.notekea.resources.sync_status_conflict
import app.notekea.resources.sync_status_failed
import app.notekea.resources.sync_status_offline
import app.notekea.resources.sync_status_pending
import app.notekea.resources.sync_status_syncing
import app.notekea.resources.sync_status_tooltip
import app.notekea.resources.sync_status_up_to_date
import app.notekea.sync.domain.SyncStatus
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The vault's sync in the status bar: up to date, syncing (a ring turns), changes pending, offline, or a problem in
 * the error color; nothing for a vault that does not sync. A click opens Settings → Sync ([onClick]).
 */
@Composable
fun SyncStatusItem(
    status: SyncStatus,
    onClick: () -> Unit
) {
    if (status == SyncStatus.NotConfigured) return

    val (icon, isProblem) = lookOf(status = status)

    // While it syncs a ring turns in the icon's place (the developer's request of 2026-11-13: work shows as work).
    val isSyncing = status == SyncStatus.Syncing

    NotekeaStatusLabel(
        text = textOf(status = status),
        icon = icon.takeUnless { isSyncing },
        iconTint = if (isProblem) MaterialTheme.colorScheme.error else null,
        tooltip = stringResource(resource = Res.string.sync_status_tooltip),
        onClick = onClick,
        leading = if (isSyncing) ({ NotekeaStatusRing(fraction = null) }) else null
    )
}

@Composable
private fun textOf(status: SyncStatus): String = when (status) {
    SyncStatus.NotConfigured, SyncStatus.UpToDate ->
        stringResource(resource = Res.string.sync_status_up_to_date)

    SyncStatus.Syncing -> stringResource(resource = Res.string.sync_status_syncing)

    SyncStatus.Offline -> stringResource(resource = Res.string.sync_status_offline)

    SyncStatus.AuthFailed -> stringResource(resource = Res.string.sync_status_auth)

    is SyncStatus.Failed -> stringResource(resource = Res.string.sync_status_failed)

    is SyncStatus.Pending -> pluralStringResource(
        resource = Res.plurals.sync_status_pending,
        quantity = status.changes,
        status.changes
    )

    is SyncStatus.Conflict -> pluralStringResource(
        resource = Res.plurals.sync_status_conflict,
        quantity = status.notes.size,
        status.notes.size
    )
}

private fun lookOf(status: SyncStatus): Pair<ImageVector, Boolean> = when (status) {
    SyncStatus.NotConfigured, SyncStatus.UpToDate -> NotekeaIcons.SyncUpToDate to false
    SyncStatus.Syncing -> NotekeaIcons.Syncing to false
    is SyncStatus.Pending -> NotekeaIcons.SyncPending to false
    SyncStatus.Offline -> NotekeaIcons.SyncOffline to false
    SyncStatus.AuthFailed, is SyncStatus.Failed -> NotekeaIcons.SyncProblem to true
    is SyncStatus.Conflict -> NotekeaIcons.SyncConflict to true
}

@Preview
@Composable
private fun SyncStatusItemPreview() {
    NotekeaPreviewThemes {
        SyncStatusItem(
            status = SyncStatus.Conflict(notes = listOf(RelativePath(value = "Plan.md"))),
            onClick = {}
        )
    }
}
