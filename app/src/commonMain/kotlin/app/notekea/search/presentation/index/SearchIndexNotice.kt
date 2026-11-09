package app.notekea.search.presentation.index

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaNotice
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.search_index_dismiss
import app.notekea.resources.search_index_notice
import org.jetbrains.compose.resources.stringResource

/**
 * The first large run's notice (`ROADMAP.md` 2.3): Notekea is reading the notes, related notes and chat come soon,
 * and writing goes on meanwhile; Dismiss hides it for this run.
 */
@Composable
fun SearchIndexNotice(
    onIntent: (SearchIndexIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    NotekeaNotice(
        message = stringResource(Res.string.search_index_notice),
        icon = NotekeaIcons.FeatureAi,
        dismissLabel = stringResource(Res.string.search_index_dismiss),
        onDismiss = { onIntent(SearchIndexIntent.OnNoticeDismiss) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun SearchIndexNoticePreview() {
    NotekeaPreviewThemes { SearchIndexNotice(onIntent = {}) }
}
