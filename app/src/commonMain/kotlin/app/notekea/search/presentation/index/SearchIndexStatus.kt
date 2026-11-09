package app.notekea.search.presentation.index

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaPopover
import app.notekea.core.presentation.designsystem.components.NotekeaStatusLabel
import app.notekea.core.presentation.designsystem.components.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.search.domain.IndexProgress
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.resources.Res
import app.notekea.resources.search_index_indexing
import app.notekea.resources.search_index_ready
import app.notekea.resources.search_index_tooltip
import app.notekea.resources.search_index_unavailable
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val PERCENT = 100
private const val PREVIEW_READY = 1204
private const val PREVIEW_TOTAL = 4

/**
 * The search index in the status bar (`ROADMAP.md` 2.3, design B): "Indexing · 28 %" with a ring while a run goes
 * on, "1,204 notes ready to search" after it, "Search unavailable" when the model failed, nothing before the vault's
 * first scan. A click opens its details ([SearchIndexDetails]) above it.
 */
@Composable
fun SearchIndexStatus(
    state: SearchIndexState,
    onIntent: (SearchIndexIntent) -> Unit
) {
    val tooltip = stringResource(Res.string.search_index_tooltip)
    val toggle = { onIntent(SearchIndexIntent.OnDetailsToggle) }
    val ready = state.readyNotes
    Box {
        when {
            state.isRunning && state.progress != null -> NotekeaStatusLabel(
                text = stringResource(Res.string.search_index_indexing, (state.fraction * PERCENT).toInt()),
                tooltip = tooltip,
                onClick = toggle,
                leading = { NotekeaStatusRing(fraction = state.fraction) }
            )

            state.error == IndexRunError.Model -> NotekeaStatusLabel(
                text = stringResource(Res.string.search_index_unavailable),
                icon = NotekeaIcons.FeatureAi,
                tooltip = tooltip,
                onClick = toggle
            )

            ready != null -> NotekeaStatusLabel(
                text = pluralStringResource(Res.plurals.search_index_ready, ready, grouped(count = ready)),
                icon = NotekeaIcons.FeatureAi,
                tooltip = tooltip,
                onClick = toggle
            )

            else -> return@Box
        }
        if (state.isDetailsOpen) {
            NotekeaPopover(onDismiss = { onIntent(SearchIndexIntent.OnDetailsDismiss) }) {
                SearchIndexDetails(state = state, onIntent = onIntent)
            }
        }
    }
}

@Preview
@Composable
private fun SearchIndexStatusPreview() {
    val running = SearchIndexState(isRunning = true, progress = IndexProgress(total = PREVIEW_TOTAL, done = 1))
    NotekeaPreviewThemes {
        Column {
            SearchIndexStatus(state = running, onIntent = {})
            SearchIndexStatus(state = SearchIndexState(readyNotes = PREVIEW_READY), onIntent = {})
        }
    }
}
