package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.presentation.components.NotekeaEmbedNotice
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.EmbedKind
import app.notekea.notes.domain.EmbedProblem
import app.notekea.notes.domain.LinkKind
import app.notekea.resources.Res
import app.notekea.resources.embed_circular
import app.notekea.resources.embed_create_note
import app.notekea.resources.embed_missing_block
import app.notekea.resources.embed_missing_heading
import app.notekea.resources.embed_missing_note
import app.notekea.resources.embed_too_deep
import app.notekea.resources.embedded_note_open
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_TARGET = "Weekend menu"

/**
 * An embedded note that cannot be shown (`ROADMAP.md` 1.15), saying why: a note that does not exist yet offers Create
 * note (as a click on a link to it does), one without the heading or block offers Open note, and one that comes back
 * around or is too deep says so with a warning, the deep one offering Open note.
 */
@Composable
internal fun EmbedNoticeBlock(
    view: AttachmentView,
    problem: EmbedProblem,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val target = view.embed.target
    val name = target.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR).trim()
    val anchor = target.substringAfter(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR, StringPatterns.EMPTY).trim()
    val shownName = view.path?.nameWithoutExtension ?: name
    val open = { link: ReadLink.Note -> onIntent(EditorIntent.OnLinkClick(link = link)) }
    val openNote = view.path?.let { path -> { open(noteLinkOf(path = path)) } }
    NotekeaEmbedNotice(
        title = listOf(shownName, anchor).filter { it.isNotBlank() }.joinToString(separator = SECTION_SEPARATOR),
        message = messageOf(problem = problem, name = shownName, anchor = anchor),
        modifier = modifier,
        isWarning = problem == EmbedProblem.CIRCULAR || problem == EmbedProblem.TOO_DEEP,
        actionLabel = when (problem) {
            EmbedProblem.MISSING_NOTE -> stringResource(resource = Res.string.embed_create_note)

            EmbedProblem.MISSING_SECTION, EmbedProblem.TOO_DEEP -> stringResource(
                resource = Res.string.embedded_note_open
            )

            EmbedProblem.CIRCULAR -> null
        },
        onAction = when (problem) {
            EmbedProblem.MISSING_NOTE -> {
                { open(ReadLink.Note(target = name, kind = LinkKind.WIKI)) }
            }

            else -> openNote ?: {}
        }
    )
}

/** Why the note is not shown, in plain words. */
@Composable
private fun messageOf(
    problem: EmbedProblem,
    name: String,
    anchor: String
): String = when (problem) {
    EmbedProblem.MISSING_NOTE -> stringResource(resource = Res.string.embed_missing_note)

    EmbedProblem.MISSING_SECTION -> if (anchor.startsWith(char = MarkdownSyntax.BLOCK_ANCHOR_CHAR)) {
        stringResource(Res.string.embed_missing_block, name, anchor)
    } else {
        stringResource(Res.string.embed_missing_heading, name, anchor)
    }

    EmbedProblem.CIRCULAR -> stringResource(resource = Res.string.embed_circular)

    EmbedProblem.TOO_DEEP -> stringResource(Res.string.embed_too_deep, EmbedProblem.MAX_EMBED_DEPTH)
}

@Preview
@Composable
private fun EmbedNoticeBlockPreview() {
    NotekeaPreviewThemes {
        EmbedNoticeBlock(
            view = AttachmentView(
                embed = Embed(range = 0..0, target = PREVIEW_TARGET, kind = EmbedKind.WIKI, isOwnLine = true),
                kind = AttachmentKind.NOTE,
                path = null,
                problem = EmbedProblem.MISSING_NOTE
            ),
            problem = EmbedProblem.MISSING_NOTE,
            onIntent = {}
        )
    }
}
