package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val WIDTH_DP = 340
private const val PREVIEW_TEXT = "Details"

/**
 * A card of details over the window, by the element it opens from: under it, or above it when the window has no room
 * below (a status bar's), kept inside the window ([DropdownPositionProvider]). A click outside or Esc closes it
 * ([onDismiss]); it takes the keyboard while open.
 */
@Composable
fun NotekeaPopover(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val gap = with(LocalDensity.current) { NotekeaDimensions.SpacingXs.roundToPx() }
    Popup(
        popupPositionProvider = remember(gap) { DropdownPositionProvider(gap = gap) },
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier.width(width = WIDTH_DP.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = MaterialTheme.colorScheme.outline),
            shadowElevation = NotekeaDimensions.SpacingS
        ) {
            Column(modifier = Modifier.padding(all = NotekeaDimensions.SpacingL), content = content)
        }
    }
}

@Preview
@Composable
private fun NotekeaPopoverPreview() {
    NotekeaPreviewThemes { NotekeaPopover(onDismiss = {}) { Text(text = PREVIEW_TEXT) } }
}
