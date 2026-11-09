package app.notekea.editor.presentation.reading

import app.notekea.core.notes.domain.percentDecoded
import app.notekea.editor.presentation.attachments.AttachmentBlock
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.view.LocalQueryBlock
import app.notekea.core.presentation.designsystem.components.attachmentImageSize
import app.notekea.core.presentation.designsystem.components.ScaledBitmapPainter
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import app.notekea.core.notes.domain.OutlineFinder
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaMarkdown
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

/**
 * The note rendered (Read mode): its Markdown, with wiki links and tags as links, and its embedded files in their
 * place ([AttachmentBlock]). A click opens a note, searches a tag, opens a file with its app (or offers to locate a
 * missing one), or opens the browser for a web address. Each heading of the outline starts a piece of its own, whose
 * place is told to [onHeadingPlaced] with the heading's index, so the outline can scroll to it (`ROADMAP.md` 1.9). An
 * embedded note is drawn the same way inside its frame, with no [topPadding]. A query block is drawn by
 * [LocalQueryBlock].
 */
@Composable
fun ReadingView(
    text: String,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    topPadding: Dp = NotekeaDimensions.SpacingL,
    onHeadingPlaced: (Int, LayoutCoordinates) -> Unit = { _, _ -> }
) {
    val readLinks = remember { ReadModeLinks() }
    val segments = remember(text, attachments) {
        ReadingSegment.of(text = text, attachments = attachments, headings = OutlineFinder().headings(text = text))
    }
    val systemUriHandler = LocalUriHandler.current
    val density = LocalDensity.current
    val imagePainter = remember(attachments, density) {
        { address: String ->
            inlineImage(address = address, attachments = attachments, readLinks = readLinks, density = density)
        }
    }
    val onLinkClick = { uri: String ->
        when (val link = readLinks.classify(uri = uri)) {
            is ReadLink.External -> systemUriHandler.openUri(uri = link.uri)
            null -> Unit
            else -> openLink(link = link, attachments = attachments, onIntent = onIntent)
        }
    }
    Column(
        modifier = modifier.padding(top = topPadding),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
    ) {
        segments.forEach { segment ->
            when (segment) {
                is ReadingSegment.Markdown -> NotekeaMarkdown(
                    content = remember(segment.text) { readLinks.prepare(body = segment.text) },
                    onLinkClick = onLinkClick,
                    imagePainter = imagePainter,
                    modifier = headingPlace(heading = segment.heading, onHeadingPlaced = onHeadingPlaced)
                )

                is ReadingSegment.Attachment -> AttachmentBlock(view = segment.view, onIntent = onIntent)

                is ReadingSegment.Query -> QuerySegment(segment = segment)
            }
        }
    }
}

/** A query block as [LocalQueryBlock] draws it, or the code it is without one. */
@Composable
private fun QuerySegment(
    segment: ReadingSegment.Query
) {
    val block = LocalQueryBlock.current
    if (block == null) {
        NotekeaMarkdown(content = segment.source)
    } else {
        block(segment.text, segment.source)
    }
}

/**
 * An image inside a paragraph or a table cell, drawn from the picture already read for its embed, at the width the
 * note asks for or its own (one image pixel per dp), never wider than [MAX_INLINE_WIDTH_DP]; null when it is not ready.
 */
private fun inlineImage(
    address: String,
    attachments: List<AttachmentView>,
    readLinks: ReadModeLinks,
    density: Density
): Painter? {
    val (target, width) = if (address.startsWith(prefix = ReadModeLinks.FILE_SCHEME)) {
        readLinks.fileOf(address = address)
    } else {
        percentDecoded(target = address) to null
    }
    val image = attachments.firstOrNull { it.embed.target == target && it.isPicture }?.image ?: return null
    val size = attachmentImageSize(
        imageWidth = image.width,
        imageHeight = image.height,
        maxWidth = MAX_INLINE_WIDTH_DP,
        requestedWidth = width?.toFloat()
    )
    return ScaledBitmapPainter(
        bitmap = image,
        size = Size(
            width = size.width * density.density,
            height =
            size.height * density.density
        )
    )
}

/** For a piece that starts with the outline's [heading], tells [onHeadingPlaced] where it is laid out. */
private fun headingPlace(
    heading: Int?,
    onHeadingPlaced: (Int, LayoutCoordinates) -> Unit
): Modifier = heading?.let { Modifier.onGloballyPositioned { onHeadingPlaced(heading, it) } } ?: Modifier

/** A note or a file opens, and a tag is searched. */
private fun openLink(
    link: ReadLink,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit
) {
    when (link) {
        is ReadLink.Note -> onIntent(EditorIntent.OnLinkClick(link = link))
        is ReadLink.Tag -> onIntent(EditorIntent.OnTagClick(tag = link.name))
        is ReadLink.File -> openFile(target = link.target, attachments = attachments, onIntent = onIntent)
        is ReadLink.External -> Unit
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

private const val MAX_INLINE_WIDTH_DP = 600f
private const val PREVIEW_TEXT = "# Arepas\n\nMix the corn flour.\n"
