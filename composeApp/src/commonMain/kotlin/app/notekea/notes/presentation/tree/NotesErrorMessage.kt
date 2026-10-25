package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.NotesError
import app.notekea.resources.Res
import app.notekea.resources.error_notes_changed_on_disk
import app.notekea.resources.error_notes_entry_missing
import app.notekea.resources.error_notes_hidden_name
import app.notekea.resources.error_notes_invalid_name
import app.notekea.resources.error_notes_name_taken
import app.notekea.resources.error_notes_operation_failed
import app.notekea.resources.error_notes_permission_denied
import app.notekea.resources.error_notes_reveal_failed
import app.notekea.resources.error_notes_vault_missing
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_ENTRY = "Recipes/Arepas.md"
private const val PREVIEW_NAME = "Arepas.md"
private const val PREVIEW_VAULT = "/home/ana/Notes"

/**
 * A [NotesError] in plain language. Entries are named by their path inside the vault (`Recipes/Arepas.md`); the vault
 * folder is written the way users read paths (`~/Notes`).
 */
@Composable
internal fun notesErrorMessage(
    error: NotesError,
    platformConventions: PlatformConventions
): String {
    val shownVault: (String) -> String = { displayPath(path = it, platformConventions = platformConventions) }
    return when (error) {
        is NotesError.ChangedOnDisk -> stringResource(Res.string.error_notes_changed_on_disk, error.path)

        is NotesError.EntryMissing -> stringResource(Res.string.error_notes_entry_missing, error.path)

        NotesError.HiddenName -> stringResource(resource = Res.string.error_notes_hidden_name)

        NotesError.InvalidName -> stringResource(resource = Res.string.error_notes_invalid_name)

        is NotesError.NameTaken -> stringResource(Res.string.error_notes_name_taken, error.name)

        is NotesError.OperationFailed -> stringResource(Res.string.error_notes_operation_failed, error.path)

        is NotesError.PermissionDenied -> stringResource(
            Res.string.error_notes_permission_denied,
            shownVault(error.path)
        )

        is NotesError.RevealFailed -> stringResource(Res.string.error_notes_reveal_failed, error.path)

        is NotesError.VaultMissing -> stringResource(Res.string.error_notes_vault_missing, shownVault(error.path))
    }
}

@Preview
@Composable
private fun notesErrorMessagePreview() {
    val errors = listOf(
        NotesError.ChangedOnDisk(path = PREVIEW_ENTRY),
        NotesError.EntryMissing(path = PREVIEW_ENTRY),
        NotesError.HiddenName,
        NotesError.InvalidName,
        NotesError.NameTaken(name = PREVIEW_NAME),
        NotesError.OperationFailed(path = PREVIEW_ENTRY),
        NotesError.PermissionDenied(path = PREVIEW_VAULT),
        NotesError.RevealFailed(path = PREVIEW_ENTRY),
        NotesError.VaultMissing(path = PREVIEW_VAULT)
    )
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            errors.forEach {
                Text(
                    text = notesErrorMessage(error = it, platformConventions = PreviewPlatformConventions.linux),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
