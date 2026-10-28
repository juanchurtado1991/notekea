package app.notekea.notes.presentation.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaTitleField
import app.notekea.core.presentation.components.NotekeaTooltip
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaColors
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.tree.notesErrorMessage
import app.notekea.resources.Res
import app.notekea.resources.editor_bookmark_add
import app.notekea.resources.editor_bookmark_remove
import app.notekea.resources.editor_next_day
import app.notekea.resources.editor_previous_day
import app.notekea.resources.editor_reveal_folder
import app.notekea.resources.editor_title_label
import org.jetbrains.compose.resources.stringResource

private const val PATH_SEPARATOR = " / "

/** The note's [folder] above its title, muted, turning to the link color under the pointer: a click shows it in Files. */
@Composable
private fun FolderPath(
    folder: RelativePath,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val color = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    NotekeaTooltip(label = stringResource(resource = Res.string.editor_reveal_folder)) {
        Row(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.small)
                .hoverable(interactionSource = interaction)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .focusProperties { canFocus = false }
                .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = NotekeaIcons.Folder,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
                tint = color
            )
            Text(
                text = folder.value.split(RelativePath.SEPARATOR).joinToString(separator = PATH_SEPARATOR),
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

/** The note's header: its folder, its title (editable: it renames the note), its star and a daily note's arrows. */
@Composable
internal fun EditorHeader(
    state: EditorState,
    header: EditorHeaderData,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val path = state.notePath ?: return
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    path.parent?.takeUnless { it.isRoot }?.let { folder ->
        FolderPath(folder = folder, onClick = { onIntent(EditorIntent.OnFolderReveal) })
        Spacer(modifier = Modifier.height(height = NotekeaDimensions.SpacingXs))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotekeaTitleField(
            value = path.nameWithoutExtension,
            onCommit = { onIntent(EditorIntent.OnTitleCommit(name = it)) },
            label = stringResource(resource = Res.string.editor_title_label),
            modifier = Modifier.weight(weight = 1f),
            isError = state.titleError != null,
            onEdit = { if (state.titleError != null) onIntent(EditorIntent.OnTitleErrorDismiss) }
        )
        BookmarkStar(isBookmarked = header.isBookmarked, platformConventions = platformConventions, onIntent = onIntent)
        header.dayNeighbors?.let {
            DayArrows(neighbors = it, platformConventions = platformConventions, onIntent = onIntent)
        }
    }
    state.titleError?.let { error ->
        Text(
            text = notesErrorMessage(error = error, platformConventions = platformConventions),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    PropertiesView(state = state, onIntent = onIntent, modifier = Modifier.padding(top = NotekeaDimensions.SpacingXs))
}

/** The star beside the title: filled in yellow while the note is bookmarked (`ROADMAP.md` 1.13). */
@Composable
private fun BookmarkStar(
    isBookmarked: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    NotekeaIconButton(
        icon = if (isBookmarked) NotekeaIcons.Bookmarked else NotekeaIcons.Bookmark,
        label = stringResource(
            resource = if (isBookmarked) Res.string.editor_bookmark_remove else Res.string.editor_bookmark_add
        ),
        onClick = { onIntent(EditorIntent.OnBookmarkToggle) },
        shortcutHint = platformConventions.bookmarkShortcut.label(
            operatingSystem = platformConventions.operatingSystem
        ),
        takesFocus = false,
        tint = if (isBookmarked) NotekeaColors.bookmark else null
    )
}

/** A daily note's arrows to the daily notes before and after it, each greyed out when there is none on its side. */
@Composable
private fun DayArrows(
    neighbors: DayNeighbors,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem
    NotekeaIconButton(
        icon = NotekeaIcons.PreviousDay,
        label = stringResource(resource = Res.string.editor_previous_day),
        onClick = { onIntent(EditorIntent.OnDayStep(step = -1)) },
        enabled = neighbors.previous != null,
        shortcutHint = platformConventions.previousDayShortcut.label(operatingSystem = operatingSystem),
        takesFocus = false
    )
    NotekeaIconButton(
        icon = NotekeaIcons.NextDay,
        label = stringResource(resource = Res.string.editor_next_day),
        onClick = { onIntent(EditorIntent.OnDayStep(step = 1)) },
        enabled = neighbors.next != null,
        shortcutHint = platformConventions.nextDayShortcut.label(operatingSystem = operatingSystem),
        takesFocus = false
    )
}

@Preview
@Composable
private fun EditorHeaderPreview() {
    NotekeaPreviewThemes {
        Column {
            EditorHeader(
                state = EditorPreviewData.state(),
                header = EditorHeaderData(dayNeighbors = null, isBookmarked = true),
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}
