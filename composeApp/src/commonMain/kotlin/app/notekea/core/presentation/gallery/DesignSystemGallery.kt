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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.notekea.core.presentation.components.NotekeaWordmark
import app.notekea.core.presentation.theme.LocalNotekeaCodeTextStyle
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaTheme

/*
 * Developer-only gallery of the design system, rendered in the light and dark themes side by side.
 * Opened with `./gradlew :composeApp:run -Pgallery`; never reachable from a packaged build, so its sample texts are
 * constants instead of string resources (ARCHITECTURE.md §7).
 */

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
                NotekeaWordmark()
                ButtonsSection()
                InputsSection()
                TreeSection()
                MarkdownSection()
                ContextMenuSection()
                ConfirmDialogSection()
                SplitPaneSection()
                TabsSection()
                DocumentTabsSection()
                PaletteSection()
                FeedbackSection()
                AttachmentsSection()
                Text(text = CODE_SAMPLE, style = LocalNotekeaCodeTextStyle.current)
            }
        }
    }
}
