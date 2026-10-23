package app.notekea.core.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaListItem
import app.notekea.core.presentation.components.NotekeaTab
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.components.StatusBarItem
import app.notekea.core.presentation.components.StatusIndicator
import app.notekea.core.presentation.theme.LocalNotekeaCodeTextStyle
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaTheme

/*
 * Developer-only gallery of the design system, rendered in the light and dark themes side by side.
 * Opened with `./gradlew :composeApp:run -Pgallery`; never reachable from a packaged build, so its sample texts are
 * constants instead of string resources (ARCHITECTURE.md §7).
 */

private const val BADGE_SAMPLE_COUNT = 12
private const val BADGE_TAB_INDEX = 1
private const val BUTTON_CANCEL = "Cancel"
private const val BUTTON_DELETE = "Delete note"
private const val BUTTON_DISABLED = "Disabled"
private const val BUTTON_LOAD_THEIRS = "Load theirs"
private const val BUTTON_NEW_NOTE = "New note"
private const val BUTTON_OPEN_FOLDER = "Open folder…"
private const val CODE_SAMPLE = "fun main() = println(\"Notekea\")"
private const val DARK_TITLE = "Dark"
private const val EMPTY_MESSAGE = "Create your first note to get started."
private const val EMPTY_TITLE = "This folder has no notes yet"
private const val LIGHT_TITLE = "Light"
private const val NOTE_FOLDER_JOURNAL = "Journal"
private const val NOTE_FOLDER_PROJECTS = "Projects/Notekea"
private const val NOTE_MEETING = "Meeting with Ana"
private const val NOTE_PRICING = "Pricing ideas"
private const val SAMPLE_ERROR = "This note changed outside Notekea."
private const val SAMPLE_PLACEHOLDER = "Search notes…"
private const val STATUS_INDEXED = "Indexed 1,204 notes"
private const val STATUS_MODEL = "Model: ready"
private const val STATUS_SYNC = "Sync: conflict"
private val SAMPLE_TABS = listOf("Related", "Backlinks", "Outline", "Chat")

@Composable
fun DesignSystemGallery() {
    Row(modifier = Modifier.fillMaxSize()) {
        GalleryColumn(title = LIGHT_TITLE, darkTheme = false, modifier = Modifier.weight(weight = 1f))
        GalleryColumn(title = DARK_TITLE, darkTheme = true, modifier = Modifier.weight(weight = 1f))
    }
}

@Composable
private fun GalleryColumn(
    title: String,
    darkTheme: Boolean,
    modifier: Modifier
) {
    NotekeaTheme(darkTheme = darkTheme) {
        Surface(modifier = modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(all = NotekeaDimensions.SpacingXl),
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
            ) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                ButtonsSection()
                InputsSection()
                TabsSection()
                FeedbackSection()
                Text(text = CODE_SAMPLE, style = LocalNotekeaCodeTextStyle.current)
            }
        }
    }
}

@Composable
private fun ButtonsSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        NotekeaButton(text = BUTTON_OPEN_FOLDER, onClick = {})
        NotekeaButton(text = BUTTON_CANCEL, onClick = {}, style = NotekeaButtonStyle.SECONDARY)
        NotekeaButton(text = BUTTON_DELETE, onClick = {}, style = NotekeaButtonStyle.DESTRUCTIVE)
        NotekeaButton(text = BUTTON_DISABLED, onClick = {}, enabled = false)
    }
}

@Composable
private fun InputsSection() {
    var query by remember { mutableStateOf(value = "") }
    var selectedNote by remember { mutableIntStateOf(value = 0) }
    NotekeaTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = SAMPLE_PLACEHOLDER
    )
    Column {
        NotekeaListItem(
            title = NOTE_PRICING,
            onClick = { selectedNote = 0 },
            supportingText = NOTE_FOLDER_PROJECTS,
            selected = selectedNote == 0
        )
        NotekeaListItem(
            title = NOTE_MEETING,
            onClick = { selectedNote = 1 },
            supportingText = NOTE_FOLDER_JOURNAL,
            selected = selectedNote == 1
        )
    }
}

@Composable
private fun TabsSection() {
    var selectedTab by remember { mutableIntStateOf(value = 0) }
    PrimaryTabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
        SAMPLE_TABS.forEachIndexed { index, label ->
            NotekeaTab(
                text = label,
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                badgeCount = BADGE_SAMPLE_COUNT.takeIf { index == BADGE_TAB_INDEX }
            )
        }
    }
}

@Composable
private fun FeedbackSection() {
    Row {
        StatusBarItem(label = STATUS_INDEXED, onClick = {}, indicator = StatusIndicator.ACTIVE)
        StatusBarItem(label = STATUS_MODEL, onClick = {})
        StatusBarItem(label = STATUS_SYNC, onClick = {}, indicator = StatusIndicator.ERROR)
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
