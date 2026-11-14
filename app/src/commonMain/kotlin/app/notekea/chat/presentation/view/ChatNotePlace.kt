package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_action_in
import app.notekea.resources.chat_action_root

/** The folders a note the chat creates can go to, for the cards of the answers below. */
internal val LocalVaultFolders = staticCompositionLocalOf { emptyList<RelativePath>() }

/**
 * Where a card's new note will be created: "In" and the folder as a pop-up button of the vault's [folders], so the
 * place is seen, and changed ([onChoose]), before Do it.
 */
@Composable
internal fun ChatNotePlace(
    folder: RelativePath,
    folders: List<RelativePath>,
    onChoose: (RelativePath) -> Unit,
    modifier: Modifier = Modifier
) {
    val root = replyString(resource = Res.string.chat_action_root)
    val label = replyString(resource = Res.string.chat_action_in)

    // A folder the plan creates is not in the vault yet: it is listed all the same, as the one chosen.
    val listed = if (folder in folders) folders else folders + folder

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NotekeaPopUpButton(
            choice = if (folder.isRoot) root else folder.value,
            choices = listed.map {
                NotekeaMenuItem(
                    label = if (it.isRoot) root else it.value,
                    onClick = { onChoose(it) },
                    isChecked = it == folder
                )
            },
            label = label
        )
    }
}

@Preview
@Composable
private fun ChatNotePlacePreview() {
    NotekeaPreviewThemes {
        ChatNotePlace(
            folder = RelativePath(value = "Projects"),
            folders = listOf(RelativePath.ROOT, RelativePath(value = "Projects")),
            onChoose = {}
        )
    }
}
