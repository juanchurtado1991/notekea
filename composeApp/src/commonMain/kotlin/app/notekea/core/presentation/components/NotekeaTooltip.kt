package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_LABEL = "Outline"
private const val PREVIEW_SHORTCUT = "Ctrl+Shift+O"
private const val TOOLTIP_SEPARATOR = "  "

/**
 * [content] with a plain tooltip on hover: its [label], then the [shortcutHint] that does the same when there is one,
 * so shortcuts are learned from the controls themselves. It shows below [content], or beside it when [isBeside].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotekeaTooltip(
    label: String,
    shortcutHint: String? = null,
    isBeside: Boolean = false,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = if (isBeside) TooltipAnchorPosition.End else TooltipAnchorPosition.Below
        ),
        tooltip = { PlainTooltip { Text(text = listOfNotNull(label, shortcutHint).joinToString(TOOLTIP_SEPARATOR)) } },
        state = rememberTooltipState(),
        hasAction = false,
        content = content
    )
}

@Preview
@Composable
private fun NotekeaTooltipPreview() {
    NotekeaPreviewThemes {
        NotekeaTooltip(label = PREVIEW_LABEL, shortcutHint = PREVIEW_SHORTCUT) {
            Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) { Text(text = PREVIEW_LABEL) }
        }
    }
}
