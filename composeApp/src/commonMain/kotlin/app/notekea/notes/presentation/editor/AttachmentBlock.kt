package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaAttachmentImage
import app.notekea.core.presentation.components.NotekeaEmbeddedNote
import app.notekea.core.presentation.components.NotekeaFileChip
import app.notekea.core.presentation.components.NotekeaMarkdown
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.notes.domain.LinkKind
import app.notekea.resources.embedded_note_open
import app.notekea.core.presentation.components.NotekeaMissingFile
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.EmbedKind
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.attachment_locate
import app.notekea.resources.attachment_missing
import app.notekea.resources.attachment_open_hint
import org.jetbrains.compose.resources.stringResource

/**
 * One embedded file as the note shows it (`ROADMAP.md` 1.8), in Read and Edit mode alike: a picture at its width, a
 * file chip that opens the file with its app, for a file that cannot be found its path and Locate…, or an embedded
 * note's text in a frame whose title opens the note.
 */
@Composable
fun AttachmentBlock(
    view: AttachmentView,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val image = view.image
    val path = view.path
    val noteText = view.noteText
    when {
        path != null && noteText != null -> EmbeddedNote(
            path = path,
            target = view.embed.target,
            text = noteText,
            onIntent = onIntent,
            modifier = modifier
        )

        view.isPicture && image != null -> NotekeaAttachmentImage(
            bitmap = image,
            contentDescription = view.embed.label ?: view.embed.target,
            modifier = modifier,
            width = view.embed.width?.dp
        )

        path != null -> NotekeaFileChip(
            name = path.name,
            hint = stringResource(resource = Res.string.attachment_open_hint),
            onOpen = { onIntent(EditorIntent.OnAttachmentOpen(path = path)) },
            modifier = modifier
        )

        else -> NotekeaMissingFile(
            title = stringResource(resource = Res.string.attachment_missing),
            path = view.embed.target,
            locateLabel = stringResource(resource = Res.string.attachment_locate),
            onLocate = { onIntent(EditorIntent.OnAttachmentLocate(embed = view.embed)) },
            modifier = modifier
        )
    }
}

/** An embedded note: its name (and section), its text rendered with working links, and a click on the title opens it. */
@Composable
private fun EmbeddedNote(
    path: RelativePath,
    target: String,
    text: String,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier
) {
    val readLinks = remember { ReadModeLinks() }
    val uriHandler = LocalUriHandler.current
    val section = target.substringAfter(
        delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR,
        missingDelimiterValue = StringPatterns.EMPTY
    )
    val noteLink = ReadLink.Note(
        target = path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION),
        kind = LinkKind.WIKI
    )
    NotekeaEmbeddedNote(
        title = listOf(path.nameWithoutExtension, section).filter { it.isNotBlank() }
            .joinToString(separator = SECTION_SEPARATOR),
        openHint = stringResource(resource = Res.string.embedded_note_open),
        onOpen = { onIntent(EditorIntent.OnLinkClick(link = noteLink)) },
        modifier = modifier
    ) {
        NotekeaMarkdown(
            content = remember(text) { readLinks.prepare(body = text) },
            onLinkClick = { uri ->
                when (val link = readLinks.classify(uri = uri)) {
                    is ReadLink.Note -> onIntent(EditorIntent.OnLinkClick(link = link))
                    is ReadLink.Tag -> onIntent(EditorIntent.OnTagClick(tag = link.name))
                    is ReadLink.External -> uriHandler.openUri(uri = link.uri)
                    is ReadLink.File, null -> Unit
                }
            }
        )
    }
}

@Preview
@Composable
private fun AttachmentBlockPreview() {
    val embed = Embed(range = 0..0, target = PREVIEW_FILE, kind = EmbedKind.WIKI, isOwnLine = true)
    NotekeaPreviewThemes {
        Column {
            AttachmentBlock(
                view = AttachmentView(
                    embed = embed,
                    kind = AttachmentKind.OTHER,
                    path = RelativePath(value = PREVIEW_FILE)
                ),
                onIntent = {}
            )
            AttachmentBlock(view = AttachmentView(embed = embed, kind = AttachmentKind.OTHER, path = null), onIntent = {
            })
        }
    }
}

private const val PREVIEW_FILE = "attachments/menu.pdf"
private const val SECTION_SEPARATOR = " › "
