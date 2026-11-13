package app.notekea.shell.presentation.follow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.ai.presentation.ModelDownloadViewModel
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.presentation.ConflictBanner
import app.notekea.sync.presentation.ConflictDialog
import app.notekea.sync.presentation.SyncStatusEffect
import app.notekea.sync.presentation.SyncStatusIntent
import app.notekea.sync.presentation.SyncStatusItem
import app.notekea.sync.presentation.SyncStatusViewModel

/** The status bar's sync entry of the open vault; a click opens Settings → Sync. */
@Composable
internal fun SyncStatusOf(sync: SyncStatusViewModel) {
    val state by sync.state.collectAsState()
    val dialogs = LocalWindowDialogs.current

    LaunchedEffect(sync) {
        sync.effects.collect { effect ->
            when (effect) {
                SyncStatusEffect.OpenSettings -> dialogs.shown = WindowDialog.SYNC
            }
        }
    }

    SyncStatusItem(
        status = state.status,
        onClick = { sync.onIntent(SyncStatusIntent.OnOpenSettings) }
    )
}

/** The status bar's entries of the open vault: its sync, then the AI model's download and the search index. */
@Composable
internal fun VaultStatusOf(
    sync: SyncStatusViewModel,
    searchIndex: SearchIndexViewModel,
    modelDownload: ModelDownloadViewModel
) {
    SyncStatusOf(sync = sync)
    AiStatusOf(searchIndex, modelDownload)
}

/** Over the workspace while notes changed in two places: the banner, and the review of one side by side. */
@Composable
internal fun SyncConflictsOf(sync: SyncStatusViewModel) {
    val state by sync.state.collectAsState()

    ConflictBanner(
        notes = state.conflicts,
        onReview = { sync.onIntent(SyncStatusIntent.OnReview(note = it)) }
    )

    state.review?.let { review ->
        ConflictDialog(
            review = review,
            onChoose = { sync.onIntent(SyncStatusIntent.OnChoose(choice = it)) },
            onDismiss = { sync.onIntent(SyncStatusIntent.OnReviewDismiss) }
        )
    }
}

@Preview
@Composable
private fun SyncStatusOfPreview() {
    NotekeaPreviewThemes {
        SyncStatusItem(
            status = SyncStatus.UpToDate,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun SyncConflictsOfPreview() {
    NotekeaPreviewThemes {
        ConflictBanner(
            notes = listOf(RelativePath(value = "Plan.md")),
            onReview = {}
        )
    }
}

@Preview
@Composable
private fun VaultStatusOfPreview() {
    NotekeaPreviewThemes {
        SyncStatusItem(
            status = SyncStatus.Pending(changes = 2),
            onClick = {}
        )
    }
}
