package app.notekea.chat.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.ActionKind
import app.notekea.chat.domain.ActionStep
import app.notekea.chat.domain.NoteDraft
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.designsystem.theme.motionTween
import app.notekea.resources.Res
import app.notekea.resources.chat_action_add_tag
import app.notekea.resources.chat_action_add_task
import app.notekea.resources.chat_action_append_text
import app.notekea.resources.chat_action_bookmark
import app.notekea.resources.chat_action_cancel
import app.notekea.resources.chat_action_cancelled
import app.notekea.resources.chat_action_cards_count
import app.notekea.resources.chat_action_clip_url
import app.notekea.resources.chat_action_close_tabs
import app.notekea.resources.chat_action_complete_task
import app.notekea.resources.chat_action_confirm
import app.notekea.resources.chat_action_create_folder
import app.notekea.resources.chat_action_create_note
import app.notekea.resources.chat_action_done_title
import app.notekea.resources.chat_action_link_notes
import app.notekea.resources.chat_action_make_cards
import app.notekea.resources.chat_action_move
import app.notekea.resources.chat_action_notes_from_text
import app.notekea.resources.chat_action_open
import app.notekea.resources.chat_action_panel
import app.notekea.resources.chat_action_rename
import app.notekea.resources.chat_action_running
import app.notekea.resources.chat_action_search
import app.notekea.resources.chat_action_set_property
import app.notekea.resources.chat_action_source
import app.notekea.resources.chat_action_split
import app.notekea.resources.chat_action_template
import app.notekea.resources.chat_action_theme
import app.notekea.resources.chat_action_today
import app.notekea.resources.chat_action_trash
import app.notekea.resources.chat_action_trash_note
import app.notekea.resources.chat_action_undo
import app.notekea.resources.chat_action_undone
import app.notekea.resources.chat_action_will
import app.notekea.resources.chat_action_write_from_notes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_LINES = 4
private const val ARROW = " → "
private val MARKS = Regex(pattern = "^\\s*(#+|>\\s*\\[![^]]*]|>)\\s*")
private val WIKI_LINK = Regex(pattern = "\\[\\[([^\\]]+)]]")
private val EMPHASIS = Regex(pattern = "\\*\\*|__")

/**
 * The card of an action turn (3.9): what Notekea will do, one line per step with every note it touches and the start
 * of the text it will write, with Do it and Cancel; once done, the same lines under "Done:" with Undo.
 */
@Composable
internal fun ChatActionCard(
    action: ChatAction,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onUndo: () -> Unit
) {
    val fade = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)

    val title = when (action.status) {
        ActionStatus.DONE, ActionStatus.UNDONE -> Res.string.chat_action_done_title
        else -> Res.string.chat_action_will
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(all = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = replyString(resource = title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        action.steps.forEach { StepLine(step = it) }

        val isTrashing = action.steps.any {
            it.kind == ActionKind.TRASH
        }

        if (isTrashing) Quiet(resource = Res.string.chat_action_trash_note)

        AnimatedContent(
            targetState = action.status,
            transitionSpec = {
                fadeIn(animationSpec = fade).togetherWith(fadeOut(animationSpec = fade))
            }
        ) { status ->
            CardFooter(
                status = status,
                onConfirm = onConfirm,
                onCancel = onCancel,
                onUndo = onUndo
            )
        }
    }
}

@Composable
private fun CardFooter(
    status: ActionStatus,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onUndo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.End
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            ActionStatus.PROPOSED -> {
                NotekeaButton(
                    text = replyString(resource = Res.string.chat_action_cancel),
                    onClick = onCancel,
                    style = NotekeaButtonStyle.SECONDARY
                )

                NotekeaButton(
                    text = replyString(resource = Res.string.chat_action_confirm),
                    onClick = onConfirm
                )
            }

            ActionStatus.RUNNING -> Quiet(resource = Res.string.chat_action_running)

            ActionStatus.DONE -> NotekeaButton(
                text = replyString(resource = Res.string.chat_action_undo),
                onClick = onUndo,
                style = NotekeaButtonStyle.TEXT
            )

            ActionStatus.UNDONE -> Quiet(resource = Res.string.chat_action_undone)

            ActionStatus.CANCELLED -> Quiet(resource = Res.string.chat_action_cancelled)

            else -> Unit
        }
    }
}

/** One step: its icon and verb, what it acts on, and the start of what it writes. */
@Composable
private fun StepLine(step: ActionStep) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = iconOf(kind = step.kind),
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = replyString(resource = verbOf(kind = step.kind)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = targetOf(step = step),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        step.content.forEach { DraftPreview(draft = it) }
    }
}

@Composable
private fun DraftPreview(draft: NoteDraft) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(all = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = nameOf(path = draft.path),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = draft
                .text
                .lines()
                .filter { it.isNotBlank() }
                .drop(n = 1)
                .take(n = PREVIEW_LINES)
                .joinToString(separator = "\n") { readable(line = it) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = PREVIEW_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** [line] of Markdown as it reads: without heading, quote and callout marks, links as their names, bold as words. */
private fun readable(line: String): String = line
    .replace(MARKS, "")
    .replace(WIKI_LINK) { it.groupValues[1].substringAfter(delimiter = '|') }
    .replace(EMPHASIS, "")

/** What a step acts on, as the person names it: notes by name, folders by path. */
@Composable
private fun targetOf(step: ActionStep): String {
    val notes = step.notes.joinToString { it.nameWithoutExtension }
    val today = replyString(resource = Res.string.chat_action_today)
    val folder = step.folder?.value

    return when (step.kind) {
        ActionKind.CREATE_NOTE -> listOfNotNull(
            step
                .content
                .firstOrNull()
                ?.path
                ?.let { nameOf(path = it) } ?: listOfNotNull(folder, step.title).joinToString("/"),
            step.template?.let {
                replyString(Res.string.chat_action_template, it.nameWithoutExtension)
            }
        ).joinToString(separator = "\n")

        ActionKind.WRITE_FROM_NOTES -> step.content.joinToString {
            nameOf(path = it.path)
        } + "\n← " + notes

        ActionKind.NOTES_FROM_TEXT ->
            step
                .folder
                ?.value
                .orEmpty()

        ActionKind.MAKE_CARDS ->
            notes + "\n" + step
                .text
                .orEmpty()
                .lines()
                .count {
                    "::" in it
                }
                .let { replyString(Res.string.chat_action_cards_count, it) }

        ActionKind.CLIP_URL -> step.content.joinToString {
            nameOf(path = it.path)
        } + "\n← " + step.url.orEmpty()

        ActionKind.APPEND_TEXT, ActionKind.ADD_TASK ->
            notes.ifEmpty { today } + "\n" + step.text.orEmpty()

        ActionKind.SET_PROPERTY -> "${step.property}: ${step.value}\n$notes"

        ActionKind.ADD_TAG -> "#${step.tag}\n$notes"

        ActionKind.COMPLETE_TASK -> "$notes\n${step.text}"

        ActionKind.LINK_NOTES ->
            notes + ARROW + step
                .target
                ?.nameWithoutExtension
                .orEmpty()

        ActionKind.RENAME -> (folder ?: notes) + ARROW + step.title.orEmpty()

        ActionKind.MOVE -> (folder ?: notes) + ARROW + step
            .target
            ?.value
            .orEmpty()

        else -> listOfNotNull(folder, notes.ifEmpty { null }, step.panel, step.theme, step.text)
            .joinToString(separator = "\n")
    }
}

private fun nameOf(path: RelativePath): String = path.value.removeSuffix(suffix = ".md")

private fun verbOf(kind: ActionKind): StringResource = when (kind) {
    ActionKind.CREATE_NOTE -> Res.string.chat_action_create_note
    ActionKind.WRITE_FROM_NOTES -> Res.string.chat_action_write_from_notes
    ActionKind.NOTES_FROM_TEXT -> Res.string.chat_action_notes_from_text
    ActionKind.CLIP_URL -> Res.string.chat_action_clip_url
    ActionKind.MAKE_CARDS -> Res.string.chat_action_make_cards
    ActionKind.APPEND_TEXT -> Res.string.chat_action_append_text
    ActionKind.SET_PROPERTY -> Res.string.chat_action_set_property
    ActionKind.ADD_TAG -> Res.string.chat_action_add_tag
    ActionKind.ADD_TASK -> Res.string.chat_action_add_task
    ActionKind.COMPLETE_TASK -> Res.string.chat_action_complete_task
    ActionKind.LINK_NOTES -> Res.string.chat_action_link_notes
    ActionKind.BOOKMARK -> Res.string.chat_action_bookmark
    ActionKind.CREATE_FOLDER -> Res.string.chat_action_create_folder
    ActionKind.RENAME -> Res.string.chat_action_rename
    ActionKind.MOVE -> Res.string.chat_action_move
    ActionKind.TRASH -> Res.string.chat_action_trash
    ActionKind.OPEN_NOTE, ActionKind.OPEN_TODAY -> Res.string.chat_action_open
    ActionKind.SEARCH -> Res.string.chat_action_search
    ActionKind.SPLIT_PANE -> Res.string.chat_action_split
    ActionKind.CLOSE_OTHER_TABS -> Res.string.chat_action_close_tabs
    ActionKind.TOGGLE_SOURCE -> Res.string.chat_action_source
    ActionKind.SET_THEME -> Res.string.chat_action_theme
    else -> Res.string.chat_action_panel
}

private fun iconOf(kind: ActionKind): ImageVector = when (kind) {
    ActionKind.CREATE_NOTE,
    ActionKind.WRITE_FROM_NOTES,
    ActionKind.NOTES_FROM_TEXT,
    ActionKind.CLIP_URL,
    ActionKind.MAKE_CARDS ->
        NotekeaIcons.NewNote

    ActionKind.APPEND_TEXT -> NotekeaIcons.Note

    ActionKind.SET_PROPERTY -> NotekeaIcons.Properties

    ActionKind.ADD_TAG -> NotekeaIcons.Tag

    ActionKind.ADD_TASK, ActionKind.COMPLETE_TASK -> NotekeaIcons.Checklist

    ActionKind.LINK_NOTES -> NotekeaIcons.NoteLink

    ActionKind.BOOKMARK -> NotekeaIcons.Bookmark

    ActionKind.CREATE_FOLDER -> NotekeaIcons.NewFolder

    ActionKind.RENAME -> NotekeaIcons.Rename

    ActionKind.MOVE -> NotekeaIcons.FolderOpen

    ActionKind.TRASH -> NotekeaIcons.Delete

    ActionKind.OPEN_NOTE, ActionKind.OPEN_TODAY -> NotekeaIcons.Note

    ActionKind.SEARCH -> NotekeaIcons.SearchNotes

    ActionKind.TOGGLE_SOURCE -> NotekeaIcons.Source

    ActionKind.SET_THEME -> NotekeaIcons.ModeLight

    else -> NotekeaIcons.SidePanel
}

@Composable
private fun Quiet(resource: StringResource) {
    Text(
        text = replyString(resource = resource),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun ChatActionCardPreview() {
    NotekeaPreviewThemes {
        ChatActionCard(
            action = ChatPreviewData.proposed,
            onConfirm = {},
            onCancel = {},
            onUndo = {}
        )
    }
}
