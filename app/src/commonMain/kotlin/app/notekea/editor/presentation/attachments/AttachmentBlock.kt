package app.notekea.editor.presentation.attachments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.LinkKind
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.YouTubeLinks
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaAttachmentImage
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaEmbeddedNote
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaFileChip
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaMissingFile
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaVideoCard
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalVideoPlayer
import app.notekea.core.presentation.platform.LocalYouTubePlayer
import app.notekea.editor.domain.Embed
import app.notekea.editor.domain.EmbedKind
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.reading.ReadLink
import app.notekea.editor.presentation.reading.ReadingView
import app.notekea.editor.presentation.view.EmbedNoticeBlock
import app.notekea.resources.Res
import app.notekea.resources.attachment_locate
import app.notekea.resources.attachment_missing
import app.notekea.resources.attachment_open_hint
import app.notekea.resources.embedded_note_open
import app.notekea.resources.video_cannot_play
import app.notekea.resources.video_local_subtitle
import app.notekea.resources.video_play
import app.notekea.resources.video_youtube
import app.notekea.resources.video_youtube_untitled
import org.jetbrains.compose.resources.stringResource

/**
 * One embedded file as the note shows it: a picture at its width, a file chip that opens
 * the file with its app, its path and Locate… for a file that cannot be found, or an embedded note's text in a frame
 * whose title opens the note; a note that cannot be shown says why ([EmbedNoticeBlock]).
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
    val problem = view.problem

    when {
        problem != null -> EmbedNoticeBlock(
            view = view,
            problem = problem,
            onIntent = onIntent,
            modifier = modifier
        )

        path != null && noteText != null -> EmbeddedNote(
            path = path,
            target = view.embed.target,
            text = noteText,
            nested = view.nested,
            onIntent = onIntent,
            modifier = modifier
        )

        view.youTubeId != null -> YouTubeEmbed(
            id = view.youTubeId,
            title = view.embed.label,
            thumbnail = view.image,
            modifier = modifier
        )

        path != null && view.location != null -> VideoEmbed(
            path = path,
            location = view.location,
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

/**
 * A YouTube video in the note: its card, with the title the note gives it, which plays it in Notekea's window, or in
 * the browser where there is none. Showing the card never goes on the network; only a click does.
 */
@Composable
private fun YouTubeEmbed(
    id: String,
    modifier: Modifier,
    title: String?,
    thumbnail: ImageBitmap?
) {
    val player = LocalYouTubePlayer.current
    val browser = LocalUriHandler.current
    val shown = title ?: stringResource(resource = Res.string.video_youtube_untitled)

    NotekeaVideoCard(
        title = shown,
        subtitle = stringResource(resource = Res.string.video_youtube),
        playLabel = stringResource(resource = Res.string.video_play),
        onPlay = {
            if (player != null) {
                player(id, shown)
            } else {
                browser.openUri(uri = YouTubeLinks.pageOf(id = id))
            }
        },
        modifier = modifier,
        thumbnail = thumbnail
    )
}

/**
 * A video in the note: its card until it is played, then the system's player in its place, at most
 * [VIDEO_MAX_WIDTH_DP] wide; ✕ puts the card back. One this system cannot play says so and opens with its app.
 */
@Composable
private fun VideoEmbed(
    path: RelativePath,
    location: String,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier
) {
    val player = LocalVideoPlayer.current
    var isPlaying by remember(location) { mutableStateOf(value = false) }
    var cannotPlay by remember(location) { mutableStateOf(value = false) }
    val isPlayable = player != null && !cannotPlay

    if (isPlaying && player != null) {
        player(
            location,
            { isPlaying = false },
            {
                isPlaying = false
                cannotPlay = true
            },
            modifier
                .fillMaxWidth()
                .widthIn(max = VIDEO_MAX_WIDTH_DP.dp)
        )
    } else {
        NotekeaVideoCard(
            title = path.name.substringBeforeLast(delimiter = '.'),
            subtitle = stringResource(
                resource = Res.string.video_local_subtitle,
                path
                    .name
                    .substringAfterLast(delimiter = '.')
                    .uppercase()
            ),
            playLabel = stringResource(resource = Res.string.video_play),
            onPlay = {
                if (player != null) {
                    isPlaying = true
                } else {
                    onIntent(EditorIntent.OnAttachmentOpen(path = path))
                }
            },
            modifier = modifier
        )
    }
}

/**
 * An embedded note: its name (and section), its text rendered as Preview renders a note, with working links and the
 * files and notes it embeds in turn ([nested]), and a click on the title opens it.
 */
@Composable
private fun EmbeddedNote(
    path: RelativePath,
    target: String,
    text: String,
    nested: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier
) {
    val section = target.substringAfter(
        delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR,
        missingDelimiterValue = StringPatterns.EMPTY
    )

    NotekeaEmbeddedNote(
        title = listOf(path.nameWithoutExtension, section).filter { it.isNotBlank() }
            .joinToString(separator = SECTION_SEPARATOR),
        openHint = stringResource(resource = Res.string.embedded_note_open),
        onOpen = { onIntent(EditorIntent.OnLinkClick(link = noteLinkOf(path = path))) },
        modifier = modifier
    ) {
        ReadingView(
            text = text,
            attachments = nested,
            onIntent = onIntent
        )
    }
}

/** The link that opens the note at [path], as a click on a wiki link to it does. */
internal fun noteLinkOf(path: RelativePath): ReadLink.Note = ReadLink.Note(
    target = path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION),
    kind = LinkKind.WIKI
)

@Preview
@Composable
private fun AttachmentBlockPreview() {
    val embed = Embed(
        range = 0..0,
        target = PREVIEW_FILE,
        kind = EmbedKind.WIKI,
        isOwnLine = true
    )

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

            AttachmentBlock(
                view = AttachmentView(
                    embed = embed,
                    kind = AttachmentKind.OTHER,
                    path = null
                ),
                onIntent = {
                }
            )
        }
    }
}

private const val PREVIEW_FILE = "attachments/menu.pdf"
private const val VIDEO_MAX_WIDTH_DP = 720
internal const val SECTION_SEPARATOR = " › "
