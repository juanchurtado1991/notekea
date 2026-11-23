package app.notekea.sync.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaFormDialog
import app.notekea.core.presentation.designsystem.components.NotekeaOptionRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.sync_repair_changed
import app.notekea.resources.sync_repair_explain
import app.notekea.resources.sync_repair_keep
import app.notekea.resources.sync_repair_keep_hint
import app.notekea.resources.sync_repair_later
import app.notekea.resources.sync_repair_remote
import app.notekea.resources.sync_repair_remote_hint
import app.notekea.resources.sync_repair_title
import app.notekea.sync.domain.RepairChoice
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_WIDTH_DP = 520
private const val MAX_NAMES = 6

/**
 * A vault left in the middle of a merge, and the two ways to put it right ([onChoose]), each saying what happens to
 * what was written: keeping the changes made here is the one suggested, since it loses nothing. [changedHere] names
 * the files going back to the repository would replace. Esc, a click outside or Not now leave it as it is.
 */
@Composable
fun RepairDialog(
    changedHere: List<RelativePath>,
    onChoose: (RepairChoice) -> Unit,
    onDismiss: () -> Unit
) {
    NotekeaFormDialog(
        title = stringResource(resource = Res.string.sync_repair_title),
        onDismiss = onDismiss,
        modifier = Modifier.width(width = DIALOG_WIDTH_DP.dp),
        explanation = stringResource(resource = Res.string.sync_repair_explain)
    ) {
        NotekeaOptionRow(
            title = stringResource(resource = Res.string.sync_repair_keep),
            onClick = { onChoose(RepairChoice.KEEP_MINE) },
            supportingText = stringResource(resource = Res.string.sync_repair_keep_hint),
            isSuggested = true
        )

        NotekeaOptionRow(
            title = stringResource(resource = Res.string.sync_repair_remote),
            onClick = { onChoose(RepairChoice.TAKE_REMOTE) },
            supportingText = stringResource(resource = Res.string.sync_repair_remote_hint)
        )

        ChangedHere(changedHere = changedHere)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            NotekeaButton(
                text = stringResource(resource = Res.string.sync_repair_later),
                onClick = onDismiss,
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

// What going back to the repository replaces, by name: the first few, then how many more.
@Composable
private fun ChangedHere(changedHere: List<RelativePath>) {
    if (changedHere.isEmpty()) return

    val more = changedHere.size - MAX_NAMES

    val names = changedHere.take(n = MAX_NAMES).joinToString { it.nameWithoutExtension } +
        if (more > 0) " +$more" else ""

    Text(
        text = stringResource(Res.string.sync_repair_changed, names),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
private fun RepairDialogPreview() {
    NotekeaPreviewThemes {
        RepairDialog(
            changedHere = listOf(RelativePath(value = "Ideas/Plan.md"), RelativePath(value = "Lisbon trip.md")),
            onChoose = {},
            onDismiss = {}
        )
    }
}
