package app.notekea.sync.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.sync_conflict_banner
import app.notekea.resources.sync_conflict_review
import org.jetbrains.compose.resources.stringResource

/**
 * Over the workspace while notes changed in two places: their names, and Review, which opens the first of them side
 * by side ([onReview]); once it is settled, the next one shows.
 */
@Composable
fun ConflictBanner(
    notes: List<RelativePath>,
    onReview: (RelativePath) -> Unit,
    modifier: Modifier = Modifier
) {
    val first = notes.firstOrNull() ?: return

    ErrorBanner(
        message = stringResource(
            resource = Res.string.sync_conflict_banner,
            notes.joinToString { it.nameWithoutExtension }
        ),
        modifier = modifier,
        action = {
            NotekeaButton(
                text = stringResource(resource = Res.string.sync_conflict_review),
                onClick = { onReview(first) },
                style = NotekeaButtonStyle.TEXT
            )
        }
    )
}

@Preview
@Composable
private fun ConflictBannerPreview() {
    NotekeaPreviewThemes {
        ConflictBanner(
            notes = listOf(RelativePath(value = "Ideas/Plan.md"), RelativePath(value = "Lisbon trip.md")),
            onReview = {}
        )
    }
}
