package app.notekea.core.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_CHAT = "Chat"
private const val PREVIEW_OUTLINE = "Outline"
private const val PREVIEW_RELATED = "Related"

/** Row of [NotekeaTab]s with the primary indicator under the selected one (right panel tabs). */
@Composable
fun NotekeaTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        tabs = tabs
    )
}

@Preview
@Composable
private fun NotekeaTabRowPreview() {
    NotekeaPreviewThemes {
        NotekeaTabRow(selectedTabIndex = 1) {
            NotekeaTab(text = PREVIEW_RELATED, selected = false, onClick = {})
            NotekeaTab(text = PREVIEW_OUTLINE, selected = true, onClick = {})
            NotekeaTab(text = PREVIEW_CHAT, selected = false, onClick = {})
        }
    }
}
