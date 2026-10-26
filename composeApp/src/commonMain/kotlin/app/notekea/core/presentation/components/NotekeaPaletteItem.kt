package app.notekea.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_ALIAS = "Morning coffee"
private const val PREVIEW_AREPAS = "Arepas"
private const val PREVIEW_CAFE = "Café con leche"
private const val PREVIEW_FOLDER = "Recipes"
private const val PREVIEW_LONG_LINE =
    "Beans and corn, slowly cooked with rosemary " + "until the soup is thick enough to hold a spoon."
private const val PREVIEW_PASTA = "Pasta e fagioli"
private const val PREVIEW_WIDTH_DP = 560

/**
 * One result of a [NotekeaPalette]: a [title] with its matched characters in bold primary ([titleHighlights]), a
 * muted [supportingText] at the end of the line (the note's folder), and an optional [detail] line under it with its
 * own highlights (the alias or path that matched, or the matching line of the content), cut with an ellipsis when
 * long. The [selected] item is the one `Enter` opens; the keyboard stays in the palette's field, so items are
 * clicked, not focused.
 */
@Composable
fun NotekeaPaletteItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleHighlights: List<IntRange> = emptyList(),
    detailHighlights: List<IntRange> = emptyList(),
    selected: Boolean = false,
    supportingText: String? = null,
    detail: String? = null
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (selected) colors.primaryContainer else colors.surface,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            TitleLine(title = title, highlights = titleHighlights, supportingText = supportingText)
            detail?.let {
                Text(
                    text = highlighted(text = it, ranges = detailHighlights, color = colors.primary),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** The title, highlighted, with the muted supporting text (the folder) at the end of the line. */
@Composable
private fun TitleLine(
    title: String,
    highlights: List<IntRange>,
    supportingText: String?
) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = highlighted(text = title, ranges = highlights, color = colors.primary),
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        supportingText?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = NotekeaDimensions.SpacingM),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** [text] with the characters of [ranges] bold in [color]; ranges outside the text are ignored. */
@Composable
private fun highlighted(
    text: String,
    ranges: List<IntRange>,
    color: Color
): AnnotatedString = remember(text, ranges, color) {
    buildAnnotatedString {
        append(text = text)
        ranges.filter { it.first >= 0 && it.last < text.length && !it.isEmpty() }.forEach { range ->
            addStyle(
                style = SpanStyle(color = color, fontWeight = FontWeight.Bold),
                start = range.first,
                end = range.last + 1
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaPaletteItemPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaPaletteItem(
                title = PREVIEW_AREPAS,
                onClick = {},
                titleHighlights = listOf(0..2),
                selected = true,
                supportingText = PREVIEW_FOLDER
            )
            NotekeaPaletteItem(
                title = PREVIEW_CAFE,
                onClick = {},
                detailHighlights = listOf(0..6),
                supportingText = PREVIEW_FOLDER,
                detail = PREVIEW_ALIAS
            )
            NotekeaPaletteItem(
                title = PREVIEW_PASTA,
                onClick = {},
                detailHighlights = listOf(10..13),
                supportingText = PREVIEW_FOLDER,
                detail = PREVIEW_LONG_LINE
            )
        }
    }
}
