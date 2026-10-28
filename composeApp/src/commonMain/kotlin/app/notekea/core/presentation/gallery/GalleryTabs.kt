package app.notekea.core.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaAttachmentImage
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaCompactTabs
import app.notekea.core.presentation.components.NotekeaEmbedNotice
import app.notekea.core.presentation.components.NotekeaEmbeddedNote
import app.notekea.core.presentation.components.NotekeaFileChip
import app.notekea.core.presentation.components.NotekeaGridCard
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMissingFile
import app.notekea.core.presentation.components.NotekeaPalette
import app.notekea.core.presentation.components.NotekeaPaletteItem
import app.notekea.core.presentation.components.NotekeaProgressIndicator
import app.notekea.core.presentation.components.NotekeaTab
import app.notekea.core.presentation.components.NotekeaTabRow
import app.notekea.core.presentation.components.NotekeaTabStrip
import app.notekea.core.presentation.components.NotekeaTabStripItem
import app.notekea.core.presentation.components.NotekeaToolRail
import app.notekea.core.presentation.components.NotekeaToolRailItem
import app.notekea.core.presentation.components.NotekeaToolbarPill
import app.notekea.core.presentation.components.StatusBarItem
import app.notekea.core.presentation.components.StatusIndicator
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

@Composable
internal fun TabsSection() {
    var selectedTab by remember { mutableIntStateOf(value = 0) }
    NotekeaTabRow(selectedTabIndex = selectedTab) {
        SAMPLE_TABS.forEachIndexed { index, label ->
            NotekeaTab(
                text = label,
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                badgeCount = BADGE_SAMPLE_COUNT.takeIf { index == BADGE_TAB_INDEX }
            )
        }
    }
    var selectedCompactTab by remember { mutableIntStateOf(value = 0) }
    NotekeaCompactTabs(
        labels = SAMPLE_COMPACT_TABS,
        selectedIndex = selectedCompactTab,
        onSelect = { selectedCompactTab = it },
        shortcutHint = TABS_SHORTCUT
    )
}

/**
 * Document tabs above a pane, a tool rail, grid cards and a toolbar capsule: the workspace's pieces (`ROADMAP.md` 1.11,
 * 1.17).
 */
@Composable
internal fun DocumentTabsSection() {
    var documents by remember { mutableStateOf(value = SAMPLE_DOCUMENTS) }
    var selectedDocument by remember { mutableIntStateOf(value = 0) }
    NotekeaTabStrip(
        tabs = documents.map { NotekeaTabStripItem(key = it, label = it, icon = NotekeaIcons.Note) },
        selectedIndex = selectedDocument.coerceAtMost(maximumValue = documents.lastIndex),
        isActive = true,
        closeLabel = DOCUMENT_CLOSE,
        onSelect = { selectedDocument = it },
        onClose = { closed -> documents = documents.filterIndexed { index, _ -> index != closed } },
        onDrop = { _, _ -> },
        onReorder = { from, to ->
            documents = documents.toMutableList().apply { add(index = to, element = removeAt(index = from)) }
            selectedDocument = to
        }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)) {
        NotekeaToolRail(
            items = SAMPLE_DOCUMENTS.map { NotekeaToolRailItem(icon = NotekeaIcons.Note, label = it, onClick = {}) },
            label = RAIL_LABEL,
            modifier = Modifier.height(height = RAIL_HEIGHT_DP.dp)
        )
        SAMPLE_DOCUMENTS.take(n = 2).forEach { name ->
            Box(modifier = Modifier.width(width = GRID_CARD_WIDTH_DP.dp)) {
                NotekeaGridCard(label = name, icon = NotekeaIcons.Note, onClick = {})
            }
        }
    }
    NotekeaToolbarPill(
        groups = listOf(
            { SAMPLE_DOCUMENTS.forEach { NotekeaIconButton(icon = NotekeaIcons.Note, label = it, onClick = {}) } },
            { NotekeaIconButton(icon = NotekeaIcons.More, label = DOCUMENT_CLOSE, onClick = {}) }
        )
    )
}

/** The palette inside a card instead of over the window; arrows move the selection, Enter and Esc do nothing here. */
@Composable
internal fun PaletteSection() {
    var query by remember { mutableStateOf(value = "") }
    var selected by remember { mutableIntStateOf(value = 0) }
    NotekeaCard {
        Box(modifier = Modifier.fillMaxWidth().height(height = PALETTE_HEIGHT_DP.dp)) {
            NotekeaPalette(
                query = query,
                onQueryChange = { query = it },
                placeholder = SAMPLE_PLACEHOLDER,
                onMove = { selected = (selected + it).mod(other = PALETTE_RESULTS.size) },
                onConfirm = {},
                onDismiss = { query = "" }
            ) {
                PALETTE_RESULTS.forEachIndexed { index, (title, folder) ->
                    NotekeaPaletteItem(
                        title = title,
                        onClick = { selected = index },
                        selected = index == selected,
                        supportingText = folder
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeedbackSection() {
    Row {
        StatusBarItem(label = STATUS_INDEXED, onClick = {}, indicator = StatusIndicator.ACTIVE)
        StatusBarItem(label = STATUS_MODEL, onClick = {})
        StatusBarItem(label = STATUS_SYNC, onClick = {}, indicator = StatusIndicator.ERROR)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaProgressIndicator(label = PROGRESS_LABEL)
        Text(text = PROGRESS_LABEL, style = MaterialTheme.typography.bodySmall)
    }
    ErrorBanner(
        message = SAMPLE_ERROR,
        onDismiss = {},
        action = { NotekeaButton(text = BUTTON_LOAD_THEIRS, onClick = {}, style = NotekeaButtonStyle.SECONDARY) }
    )
    EmptyState(
        title = EMPTY_TITLE,
        message = EMPTY_MESSAGE,
        action = { NotekeaButton(text = BUTTON_NEW_NOTE, onClick = {}) }
    )
}

@Composable
internal fun AttachmentsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        NotekeaAttachmentImage(
            bitmap = ImageBitmap(width = SAMPLE_IMAGE_WIDTH, height = SAMPLE_IMAGE_HEIGHT),
            contentDescription = null
        )
        NotekeaFileChip(name = SAMPLE_FILE, hint = SAMPLE_FILE_HINT, onOpen = {})
        NotekeaEmbeddedNote(title = SAMPLE_EMBEDDED_NOTE, openHint = SAMPLE_EMBEDDED_NOTE_HINT, onOpen = {}) {
            Text(text = SAMPLE_EMBEDDED_NOTE_TEXT, style = MaterialTheme.typography.bodyLarge)
        }
        NotekeaEmbedNotice(
            title = SAMPLE_EMBED_MISSING_TITLE,
            message = SAMPLE_EMBED_MISSING,
            actionLabel = SAMPLE_EMBED_CREATE
        )
        NotekeaEmbedNotice(title = SAMPLE_EMBEDDED_NOTE, message = SAMPLE_EMBED_CIRCULAR, isWarning = true)
        NotekeaMissingFile(title = SAMPLE_MISSING, path = SAMPLE_MISSING_PATH, locateLabel = SAMPLE_LOCATE, onLocate = {
        })
    }
}
