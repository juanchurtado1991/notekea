package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val PREVIEW_CHOICE = "Español"
private const val PREVIEW_LABEL = "Language"

/**
 * A setting with a few choices, as a macOS pop-up button: the current [choice] with a chevron; a click opens the
 * [choices] below it, the current one checked. [label] names the setting for screen readers.
 */
@Composable
fun NotekeaPopUpButton(
    choice: String,
    choices: List<NotekeaMenuItem>,
    label: String,
    modifier: Modifier = Modifier
) {
    val menuState = remember { NotekeaContextMenuState() }
    val colors = MaterialTheme.colorScheme

    NotekeaContextMenuArea(
        sections = listOf(choices),
        modifier = modifier,
        state = menuState
    ) {
        Surface(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.small)
                .clickable(
                    onClickLabel = label,
                    role = Role.DropdownList,
                    onClick = { menuState.open(at = Offset.Zero) }
                ),
            shape = MaterialTheme.shapes.small,
            color = colors.surface,
            border = BorderStroke(
                width = NotekeaDimensions.BorderWidth,
                color = colors.outline
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = NotekeaDimensions.SpacingS,
                    vertical = NotekeaDimensions.SpacingXs
                ),
                horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = choice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface
                )

                Icon(
                    imageVector = NotekeaIcons.Dropdown,
                    contentDescription = null,
                    modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun NotekeaPopUpButtonPreview() {
    NotekeaPreviewThemes {
        NotekeaPopUpButton(
            choice = PREVIEW_CHOICE,
            choices = listOf(
                NotekeaMenuItem(
                    label = PREVIEW_CHOICE,
                    onClick = {},
                    isChecked = true
                )
            ),
            label = PREVIEW_LABEL
        )
    }
}
