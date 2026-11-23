package app.notekea.sync.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaFormDialog
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.sync_large_explain
import app.notekea.resources.sync_large_ok
import app.notekea.resources.sync_large_title
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_WIDTH_DP = 480

// Past this the list scrolls: a folder of films left out at once kept OK out of the window.
private const val LIST_MAX_HEIGHT_DP = 240

/**
 * The files a sync left out of the repository because they are over 100 MB, which no Git host takes (the code review
 * of 2026-11-22): it names them and says they stay on this computer, no longer synced. OK, Esc or a click outside
 * close it ([onDismiss]); it shows again only when another file is left out.
 */
@Composable
fun LargeFilesDialog(
    files: List<RelativePath>,
    onDismiss: () -> Unit
) {
    NotekeaFormDialog(
        title = stringResource(resource = Res.string.sync_large_title),
        onDismiss = onDismiss,
        modifier = Modifier.width(width = DIALOG_WIDTH_DP.dp),
        explanation = stringResource(resource = Res.string.sync_large_explain)
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = LIST_MAX_HEIGHT_DP.dp)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            files.forEach {
                Text(
                    text = it.value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            NotekeaButton(
                text = stringResource(resource = Res.string.sync_large_ok),
                onClick = onDismiss
            )
        }
    }
}

@Preview
@Composable
private fun LargeFilesDialogPreview() {
    NotekeaPreviewThemes {
        LargeFilesDialog(
            files = listOf(RelativePath(value = "Videos/Lisbon trip.mp4")),
            onDismiss = {}
        )
    }
}
