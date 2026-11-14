package app.notekea.core.presentation.designsystem.components.menus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val ARROW_BOX_DP = 16
private const val ARROW_INSET_DP = 3
private const val ARROW_RADIUS_DP = 4
private const val PREVIEW_CHOICE = "Español"
private const val PREVIEW_LABEL = "Language"

/**
 * A setting with a few choices, as a macOS pop-up button: the current [choice] with its double arrow; a click opens the
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
        val shape = RoundedCornerShape(size = NotekeaDimensions.PushButtonRadius)

        Surface(
            modifier = Modifier
                .heightIn(min = NotekeaDimensions.PushButtonHeight)
                .focusBorder(
                    color = colors.primary,
                    shape = shape
                )
                .clip(shape = shape)
                .clickable(
                    onClickLabel = label,
                    role = Role.DropdownList,
                    onClick = { menuState.open(at = Offset.Zero) }
                ),
            shape = shape,
            color = colors.surface,
            border = BorderStroke(
                width = NotekeaDimensions.BorderWidth,
                color = colors.outline
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    start = NotekeaDimensions.SpacingS,
                    end = ARROW_INSET_DP.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A long folder's path ends in "…" rather than pushing the arrows out of the button.
                Text(
                    text = choice,
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                PopUpArrows()
            }
        }
    }
}

/** macOS's pop-up arrows: a double chevron on a small square in the theme's color (redesign 5A). */
@Composable
private fun PopUpArrows() {
    Box(
        modifier = Modifier
            .size(size = ARROW_BOX_DP.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size = ARROW_RADIUS_DP.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = NotekeaIcons.PopUp,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
            tint = MaterialTheme.colorScheme.onPrimary
        )
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
