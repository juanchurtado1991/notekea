package app.notekea.notes.presentation.connections

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.connections_open
import app.notekea.resources.related_open_beside
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_ROW = "Right-click here"

/** A note's row in Connections, with its menu: Open ([onOpen]) and Open to the side ([onOpenBeside]). */
@Composable
fun ConnectionMenu(
    onOpen: () -> Unit,
    onOpenBeside: () -> Unit,
    content: @Composable () -> Unit
) {
    val open = stringResource(resource = Res.string.connections_open)
    val beside = stringResource(resource = Res.string.related_open_beside)
    NotekeaContextMenuArea(
        sections = listOf(
            listOf(
                NotekeaMenuItem(label = open, onClick = onOpen, icon = NotekeaIcons.Note),
                NotekeaMenuItem(label = beside, onClick = onOpenBeside, icon = NotekeaIcons.SidePanel)
            )
        ),
        content = content
    )
}

@Preview
@Composable
private fun ConnectionMenuPreview() {
    NotekeaPreviewThemes {
        ConnectionMenu(onOpen = {}, onOpenBeside = {}) { Text(text = PREVIEW_ROW) }
    }
}
