package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.ColorThemeSchemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_HEIGHT_DP = 64
private const val PREVIEW_WIDTH_DP = 160
private const val SWATCH_HEIGHT_DP = 6
private const val TITLE_WIDTH_DP = 36
private const val TEXT_WIDTH_DP = 48
private const val TEXT_HEIGHT_DP = 3
private const val TAG_WIDTH_DP = 22
private const val BUTTON_WIDTH_DP = 26
private const val BUTTON_HEIGHT_DP = 9
private const val TEXT_ALPHA = 0.55f
private const val SKETCH_PADDING_DP = 6

/**
 * A color theme to choose (Settings → Appearance, `ROADMAP.md` 2.12): a sketch of a note in its [light] and [dark]
 * schemes side by side (title, text, a tag and a button), and its [name] under them. The [isSelected] one is outlined in
 * the primary color and checked. A click or Enter goes to [onClick]; the card draws the focus ring when it has the
 * keyboard, and screen readers hear it as a selectable choice.
 */
@Composable
fun NotekeaThemeCard(
    name: String,
    light: ColorScheme,
    dark: ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .width(width = PREVIEW_WIDTH_DP.dp)
            .clip(shape = shape)
            .border(
                width = if (isSelected) NotekeaDimensions.FocusRingWidth else NotekeaDimensions.BorderWidth,
                color = if (isSelected) colors.primary else colors.outlineVariant,
                shape = shape
            )
            .focusBorder(color = colors.primary, shape = shape)
            .semantics { selected = isSelected }
            .clickable(role = Role.RadioButton, onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(height = PREVIEW_HEIGHT_DP.dp)) {
            NoteSketch(scheme = light, modifier = Modifier.weight(weight = 1f))
            NoteSketch(scheme = dark, modifier = Modifier.weight(weight = 1f))
        }
        CardName(name = name, isSelected = isSelected)
    }
}

/** The theme's [name] under its sketches, checked when it [isSelected]. */
@Composable
private fun CardName(
    name: String,
    isSelected: Boolean
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.surface)
            .padding(horizontal = NotekeaDimensions.SpacingS, vertical = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Icon(
                imageVector = NotekeaIcons.Checked,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = colors.primary
            )
        }
    }
}

/** A note drawn in [scheme]'s colors: its title, a line of text, a tag and a button on the page. */
@Composable
private fun NoteSketch(
    scheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight().background(color = scheme.background).padding(all = SKETCH_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Bar(width = TITLE_WIDTH_DP, height = SWATCH_HEIGHT_DP - 1, color = scheme.onBackground)
        Bar(width = TEXT_WIDTH_DP, height = TEXT_HEIGHT_DP, color = scheme.onBackground.copy(alpha = TEXT_ALPHA))
        Bar(width = TAG_WIDTH_DP, height = SWATCH_HEIGHT_DP, color = scheme.tertiary)
        Spacer(modifier = Modifier.weight(weight = 1f))
        Bar(width = BUTTON_WIDTH_DP, height = BUTTON_HEIGHT_DP, color = scheme.primary)
    }
}

@Composable
private fun Bar(
    width: Int,
    height: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(width = width.dp, height = height.dp)
            .clip(shape = RoundedCornerShape(size = (height / 2).dp))
            .background(color = color)
    )
}

@Preview
@Composable
private fun NotekeaThemeCardPreview() {
    NotekeaPreviewThemes {
        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            listOf(ColorTheme.WORKSPACE_BLUE, ColorTheme.MIDNIGHT_LIME).forEach { theme ->
                NotekeaThemeCard(
                    name = theme.name,
                    light = ColorThemeSchemes.of(theme = theme, darkTheme = false),
                    dark = ColorThemeSchemes.of(theme = theme, darkTheme = true),
                    isSelected = theme == ColorTheme.WORKSPACE_BLUE,
                    onClick = {}
                )
            }
        }
    }
}
