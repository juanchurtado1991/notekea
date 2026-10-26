package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaAttachmentImage
import app.notekea.core.presentation.components.NotekeaFileChip
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
 * file chip that opens the file with its app, or, for a file that cannot be found, its path and Locate….
 */
@Composable
fun AttachmentBlock(
    view: AttachmentView,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val image = view.image
    val path = view.path
    when {
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
