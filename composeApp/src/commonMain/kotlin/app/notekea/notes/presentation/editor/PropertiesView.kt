package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaChip
import app.notekea.core.presentation.components.NotekeaFold
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.NoteProperty
import app.notekea.resources.Res
import app.notekea.resources.properties_show_form
import app.notekea.resources.properties_show_source
import app.notekea.resources.properties_title
import org.jetbrains.compose.resources.stringResource

private const val CARD_ALPHA = 0.55f
private const val PREVIEW_WIDTH_DP = 640
private const val SOURCE_LINE = "status: draft"

/**
 * The note's properties under its title (`ROADMAP.md` 1.14), on a soft card: a "Properties" header with their number,
 * which folds them, then one row per property (its type's icon, its muted name renamed in place, its value edited in
 * place, and a ✕ shown while the row is hovered), and "Add property", whose menu picks the type. "Show source" swaps the
 * form for the YAML as written, which can be edited too; frontmatter the form cannot read is shown that way with an
 * explanation, so nothing is lost. A note without properties shows nothing.
 */
@Composable
internal fun PropertiesView(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val content = state.properties
    // The source stays while it is being typed in, even once its YAML is valid, empty or saved: it never vanishes.
    var isEditingSource by remember(state.notePath) { mutableStateOf(value = false) }
    if (content == FrontmatterContent.None && !state.isSourceShown && !isEditingSource) return
    val properties = (content as? FrontmatterContent.Properties)?.properties.orEmpty()
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CARD_ALPHA), shape = shape)
            .padding(all = NotekeaDimensions.SpacingXs)
    ) {
        PropertiesHeader(state = state, count = properties.size, isEditingSource = isEditingSource, onIntent = onIntent)
        NotekeaFold(isOpen = state.isPropertiesExpanded) {
            val isInvalid = content is FrontmatterContent.Invalid
            if (isInvalid || state.isSourceShown || isEditingSource) {
                PropertiesSource(
                    state = state,
                    invalidLine = (content as? FrontmatterContent.Invalid)?.line,
                    onEditingChange = { isEditingSource = it },
                    onIntent = onIntent
                )
            } else {
                val keys = properties.map(NoteProperty::key)
                properties.forEach { PropertyRow(property = it, keys = keys, today = state.today, onIntent = onIntent) }
                AddPropertyRow(onIntent = onIntent)
            }
        }
    }
}

/**
 * "Properties" with their number, folding the form, and Show source / Show properties at the end; while the source is
 * being typed in, Show properties leaves it (which saves it) and shows the form.
 */
@Composable
private fun PropertiesHeader(
    state: EditorState,
    count: Int,
    isEditingSource: Boolean,
    onIntent: (EditorIntent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FoldTitle(isExpanded = state.isPropertiesExpanded, count = count, onIntent = onIntent)
        Spacer(modifier = Modifier.weight(weight = 1f))
        if (state.isPropertiesExpanded && state.properties !is FrontmatterContent.Invalid) {
            SourceToggle(isSourceShown = state.isSourceShown || isEditingSource, onToggle = {
                if (isEditingSource) focusManager.clearFocus()
                if (state.isSourceShown || !isEditingSource) onIntent(PropertyIntent.OnSourceToggle)
            })
        }
    }
}

/** The chevron, "Properties" and their number in a small pill; a click folds or opens the form. */
@Composable
private fun FoldTitle(
    isExpanded: Boolean,
    count: Int,
    onIntent: (EditorIntent) -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.small)
            .focusProperties { canFocus = false }
            .clickable { onIntent(PropertyIntent.OnFoldToggle) }
            .padding(horizontal = NotekeaDimensions.SpacingXs, vertical = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Icon(
            imageVector = if (isExpanded) NotekeaIcons.Collapse else NotekeaIcons.Expand,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = muted
        )
        Text(
            text = stringResource(resource = Res.string.properties_title),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = muted
        )
        if (count > 0) NotekeaChip(label = count.toString(), containerColor = MaterialTheme.colorScheme.surface)
    }
}

@Composable
private fun SourceToggle(
    isSourceShown: Boolean,
    onToggle: () -> Unit
) {
    NotekeaIconButton(
        icon = if (isSourceShown) NotekeaIcons.Properties else NotekeaIcons.PropertySource,
        label = stringResource(
            resource = if (isSourceShown) Res.string.properties_show_form else Res.string.properties_show_source
        ),
        onClick = onToggle,
        takesFocus = false
    )
}

@Preview
@Composable
private fun PropertiesViewPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            PropertiesView(
                state = EditorState(
                    notePath = EditorPreviewData.path,
                    properties = EditorPreviewData.typedProperties
                ),
                onIntent = {}
            )
        }
    }
}

@Preview
@Composable
private fun PropertiesViewSourcePreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            PropertiesView(
                state = EditorState(properties = FrontmatterContent.Invalid(line = 1), propertiesSource = SOURCE_LINE),
                onIntent = {}
            )
        }
    }
}
