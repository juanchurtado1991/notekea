package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.NotekeaMarkdown
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

/**
 * The note rendered (Read mode): its Markdown, with wiki links and tags as links, and its embedded files in their
 * place ([AttachmentBlock]). A click opens a note, searches a tag, opens a file with its app (or offers to locate a
 * missing one), or opens the browser for a web address.
 */
@Composable
fun ReadingView(
    text: String,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val readLinks = remember { ReadModeLinks() }
    val segments = remember(text, attachments) { ReadingSegment.of(text = text, attachments = attachments) }
    val systemUriHandler = LocalUriHandler.current
    val onLinkClick = { uri: String ->
        when (val link = readLinks.classify(uri = uri)) {
            is ReadLink.Note -> onIntent(EditorIntent.OnLinkClick(link = link))
            is ReadLink.External -> systemUriHandler.openUri(uri = link.uri)
            is ReadLink.Tag -> onIntent(EditorIntent.OnTagClick(tag = link.name))
            is ReadLink.File -> openFile(target = link.target, attachments = attachments, onIntent = onIntent)
            null -> Unit
        }
    }
    Column(
        modifier = modifier.padding(top = NotekeaDimensions.SpacingL),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
    ) {
        segments.forEach { segment ->
            when (segment) {
                is ReadingSegment.Markdown -> NotekeaMarkdown(
                    content = remember(segment.text) { readLinks.prepare(body = segment.text) },
                    onLinkClick = onLinkClick
                )

                is ReadingSegment.Attachment -> AttachmentBlock(view = segment.view, onIntent = onIntent)
            }
        }
    }
}

/** A file link in a paragraph opens its file, or offers to locate it when it is missing. */
private fun openFile(
    target: String,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit
) {
    val view = attachments.firstOrNull { it.embed.target == target } ?: return
    val path = view.path
    onIntent(
        if (path !=
            null
        ) {
            EditorIntent.OnAttachmentOpen(path = path)
        } else {
            EditorIntent.OnAttachmentLocate(embed = view.embed)
        }
    )
}

@Preview
@Composable
private fun ReadingViewPreview() {
    NotekeaPreviewThemes {
        ReadingView(text = PREVIEW_TEXT, attachments = emptyList(), onIntent = {})
    }
}

private const val PREVIEW_TEXT = "# Arepas\n\nMix the corn flour.\n"
