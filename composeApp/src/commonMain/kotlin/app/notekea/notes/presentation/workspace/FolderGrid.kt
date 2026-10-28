package app.notekea.notes.presentation.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaGridCard
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.FolderCard
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.folder_empty_message
import app.notekea.resources.folder_empty_title
import app.notekea.resources.note_panel_folder_notes
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val CARD_MIN_WIDTH_DP = 168
private const val CARD_MAX_WIDTH_DP = CARD_MIN_WIDTH_DP * 2
private const val CARD_RATIO = 4f / 3f
private const val PREVIEW_HEIGHT_DP = 420
private const val PREVIEW_WIDTH_DP = 720

/**
 * A folder shown in a tab (`ROADMAP.md` 1.11): its name and how many files it holds in all ([fileCount], as its row
 * in the tree says), then a grid of cards for what is
 * directly inside it ([cards]): subfolders, notes and files. A note's card shows the first picture it embeds and an
 * image's card the image itself ([covers], asked for with [onCoverNeeded] as cards come into view); the others show
 * their icon. A click opens the card's folder, note or file in a tab ([onOpen]).
 */
@Composable
fun FolderGrid(
    folder: RelativePath,
    fileCount: Int,
    cards: List<FolderCard>,
    covers: Map<RelativePath, ImageBitmap?>,
    onOpen: (RelativePath) -> Unit,
    onCoverNeeded: (RelativePath, IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl)) {
            Text(text = folder.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = pluralStringResource(Res.plurals.note_panel_folder_notes, fileCount, fileCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    EmptyState(
                        title = stringResource(resource = Res.string.folder_empty_title),
                        message = stringResource(resource = Res.string.folder_empty_message)
                    )
                }
            } else {
                Cards(cards = cards, covers = covers, onOpen = onOpen, onCoverNeeded = onCoverNeeded)
            }
        }
    }
}

@Composable
private fun Cards(
    cards: List<FolderCard>,
    covers: Map<RelativePath, ImageBitmap?>,
    onOpen: (RelativePath) -> Unit,
    onCoverNeeded: (RelativePath, IntSize) -> Unit
) {
    // The largest a card is drawn, in pixels: below twice the narrowest card, or the grid would fit another column.
    val coverSize = with(LocalDensity.current) {
        IntSize(width = CARD_MAX_WIDTH_DP.dp.roundToPx(), height = (CARD_MAX_WIDTH_DP / CARD_RATIO).dp.roundToPx())
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = CARD_MIN_WIDTH_DP.dp),
        contentPadding = PaddingValues(top = NotekeaDimensions.SpacingL),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
    ) {
        items(items = cards, key = { it.path.value }) { card ->
            val hasCover = card.kind == FolderCard.Kind.NOTE || card.kind == FolderCard.Kind.IMAGE
            if (hasCover) LaunchedEffect(card.path) { onCoverNeeded(card.path, coverSize) }
            NotekeaGridCard(
                label = if (card.kind == FolderCard.Kind.NOTE) card.path.nameWithoutExtension else card.path.name,
                icon = iconOf(kind = card.kind),
                onClick = { onOpen(card.path) },
                image = covers[card.path]
            )
        }
    }
}

private fun iconOf(
    kind: FolderCard.Kind
): ImageVector = when (kind) {
    FolderCard.Kind.FOLDER -> NotekeaIcons.Folder
    FolderCard.Kind.NOTE -> NotekeaIcons.Note
    FolderCard.Kind.IMAGE -> NotekeaIcons.ImageFile
    FolderCard.Kind.FILE -> NotekeaIcons.OtherFile
}

@Preview
@Composable
private fun FolderGridPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            FolderGrid(
                folder = RelativePath(value = "Recipes"),
                fileCount = 3,
                cards = listOf("Recipes/Breakfast", "Recipes/Arepas.md", "Recipes/Menu.pdf").map {
                    FolderCard(
                        path = RelativePath(value = it),
                        kind = when {
                            it.endsWith(suffix = ".md") -> FolderCard.Kind.NOTE
                            it.endsWith(suffix = ".pdf") -> FolderCard.Kind.FILE
                            else -> FolderCard.Kind.FOLDER
                        }
                    )
                },
                covers = emptyMap(),
                onOpen = {},
                onCoverNeeded = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun FolderGridEmptyPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            FolderGrid(
                folder = RelativePath(value = "Empty"),
                fileCount = 0,
                cards = emptyList(),
                covers = emptyMap(),
                onOpen = {},
                onCoverNeeded = { _, _ -> }
            )
        }
    }
}
