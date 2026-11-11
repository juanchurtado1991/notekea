package app.notekea.sync.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaCard
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.sync_conflict_explain
import app.notekea.resources.sync_conflict_mine
import app.notekea.resources.sync_conflict_theirs
import app.notekea.resources.sync_conflict_title
import app.notekea.resources.sync_keep_both
import app.notekea.resources.sync_keep_mine
import app.notekea.resources.sync_keep_theirs
import app.notekea.sync.domain.ConflictChoice
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_WIDTH_DP = 860
private const val DIALOG_HEIGHT_DP = 560

/**
 * A note changed in two places, both versions side by side as written (ours on the left), and what to keep: mine,
 * theirs, or both ([onChoose]). Esc or a click outside leaves it for later ([onDismiss]).
 */
@Composable
fun ConflictDialog(
    review: ConflictReview,
    onChoose: (ConflictChoice) -> Unit,
    onDismiss: () -> Unit
) {
    NotekeaDialog(
        onDismiss = onDismiss,
        modifier = Modifier.size(
            width = DIALOG_WIDTH_DP.dp,
            height = DIALOG_HEIGHT_DP.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = stringResource(
                    resource = Res.string.sync_conflict_title,
                    review.note.nameWithoutExtension
                ),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(resource = Res.string.sync_conflict_explain),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.weight(weight = 1f),
                horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
            ) {
                Version(
                    title = stringResource(resource = Res.string.sync_conflict_mine),
                    text = review.mine
                )

                Version(
                    title = stringResource(resource = Res.string.sync_conflict_theirs),
                    text = review.theirs
                )
            }

            Choices(onChoose = onChoose)
        }
    }
}

@Composable
private fun RowScope.Version(
    title: String,
    text: String
) {
    Column(
        modifier = Modifier
            .weight(weight = 1f)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NotekeaCard(modifier = Modifier.fillMaxWidth().weight(weight = 1f)) {
            Text(
                text = text,
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(all = NotekeaDimensions.SpacingM),
                style = MaterialTheme
                    .typography
                    .bodySmall
                    .copy(fontFamily = FontFamily.Monospace)
            )
        }
    }
}

@Composable
private fun Choices(onChoose: (ConflictChoice) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.End
        )
    ) {
        NotekeaButton(
            text = stringResource(resource = Res.string.sync_keep_both),
            onClick = { onChoose(ConflictChoice.BOTH) },
            style = NotekeaButtonStyle.TEXT
        )

        NotekeaButton(
            text = stringResource(resource = Res.string.sync_keep_theirs),
            onClick = { onChoose(ConflictChoice.THEIRS) },
            style = NotekeaButtonStyle.SECONDARY
        )

        NotekeaButton(
            text = stringResource(resource = Res.string.sync_keep_mine),
            onClick = { onChoose(ConflictChoice.MINE) }
        )
    }
}

@Preview
@Composable
private fun ConflictDialogPreview() {
    NotekeaPreviewThemes {
        ConflictDialog(
            review = ConflictReview(
                note = RelativePath(value = "Plan.md"),
                mine = "# Plan\n\nBuy seeds on Monday.\n",
                theirs = "# Plan\n\nBuy seeds on Friday.\n"
            ),
            onChoose = {},
            onDismiss = {}
        )
    }
}
