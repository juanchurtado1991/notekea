package app.notekea.notes.presentation.editor

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaPropertyField
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.NoteProperty
import app.notekea.notes.domain.PropertyKeys
import app.notekea.notes.domain.PropertyValue
import app.notekea.resources.Res
import app.notekea.resources.properties_add
import app.notekea.resources.properties_invalid
import app.notekea.resources.properties_key_label
import app.notekea.resources.properties_remove
import app.notekea.resources.properties_show_form
import app.notekea.resources.properties_show_source
import app.notekea.resources.properties_title
import org.jetbrains.compose.resources.stringResource

private const val KEY_WIDTH_DP = 140
private const val PREVIEW_WIDTH_DP = 640
private const val SOURCE_LINE = "status: draft"
private const val COUNT_SEPARATOR = " "

/**
 * The note's properties under its title (`ROADMAP.md` 1.14): a "Properties" header that folds them, then one row per
 * property (its type's icon, its name, its value edited in place, and a ✕), and "Add property", whose menu picks the
 * type. "Show source" swaps the form for the YAML as written, which can be edited too; frontmatter the form cannot read
 * is shown that way with an explanation, so nothing is lost. A note without properties shows only "Add property".
 */
@Composable
internal fun PropertiesView(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val content = state.properties
    if (content == FrontmatterContent.None && !state.isSourceShown) return
    val properties = (content as? FrontmatterContent.Properties)?.properties.orEmpty()
    Column(modifier = modifier.fillMaxWidth()) {
        PropertiesHeader(state = state, count = properties.size, onIntent = onIntent)
        if (!state.isPropertiesExpanded) return@Column
        val isInvalid = content is FrontmatterContent.Invalid
        if (isInvalid || state.isSourceShown) {
            PropertiesSource(
                state = state,
                invalidLine = (content as? FrontmatterContent.Invalid)?.line,
                onIntent = onIntent
            )
        } else {
            properties.forEach {
                PropertyRow(property = it, keys = properties.map(NoteProperty::key), onIntent = onIntent)
            }
            AddPropertyButton(onIntent = onIntent)
        }
    }
}

/** "Properties" with their number, folding the form, and Show source / Show properties at the end. */
@Composable
private fun PropertiesHeader(
    state: EditorState,
    count: Int,
    onIntent: (EditorIntent) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FoldTitle(isExpanded = state.isPropertiesExpanded, count = count, onIntent = onIntent)
        Spacer(modifier = Modifier.weight(weight = 1f))
        if (state.isPropertiesExpanded && state.properties !is FrontmatterContent.Invalid) {
            SourceToggle(isSourceShown = state.isSourceShown, onIntent = onIntent)
        }
    }
}

/** The chevron and "Properties" with their number; a click folds or opens the form. */
@Composable
private fun FoldTitle(
    isExpanded: Boolean,
    count: Int,
    onIntent: (EditorIntent) -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val title = stringResource(resource = Res.string.properties_title)
    Row(
        modifier = Modifier
            .focusProperties { canFocus = false }
            .clickable { onIntent(PropertyIntent.OnFoldToggle) }
            .padding(vertical = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) NotekeaIcons.Collapse else NotekeaIcons.Expand,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = muted
        )
        Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingXs))
        val label = if (count > 0) title + COUNT_SEPARATOR + count else title
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = muted)
    }
}

@Composable
private fun SourceToggle(
    isSourceShown: Boolean,
    onIntent: (EditorIntent) -> Unit
) {
    NotekeaIconButton(
        icon = if (isSourceShown) NotekeaIcons.Properties else NotekeaIcons.PropertySource,
        label = stringResource(
            resource = if (isSourceShown) Res.string.properties_show_form else Res.string.properties_show_source
        ),
        onClick = { onIntent(PropertyIntent.OnSourceToggle) },
        takesFocus = false
    )
}

/** One property: its type's icon, its name (renamed in place), its value, and a ✕ that removes it. */
@Composable
private fun PropertyRow(
    property: NoteProperty,
    keys: List<String>,
    onIntent: (EditorIntent) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = iconOf(value = property.value),
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingXs).width(width = KEY_WIDTH_DP.dp)) {
            NotekeaPropertyField(
                value = property.key,
                onCommit = { onIntent(PropertyIntent.OnRename(from = property.key, to = it)) },
                placeholder = stringResource(resource = Res.string.properties_key_label),
                isValid = { PropertyKeys.isValid(key = it, existing = keys, renaming = property.key) }
            )
        }
        Box(modifier = Modifier.weight(weight = 1f)) {
            PropertyValueEditor(property = property, onIntent = onIntent)
        }
        NotekeaIconButton(
            icon = NotekeaIcons.Close,
            label = stringResource(resource = Res.string.properties_remove),
            onClick = { onIntent(PropertyIntent.OnRemove(key = property.key)) },
            takesFocus = false
        )
    }
}

/** "Add property", whose menu picks the new property's type; it gets a free name, renamed in its row. */
@Composable
private fun AddPropertyButton(
    onIntent: (EditorIntent) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        NotekeaMenuButton(
            icon = NotekeaIcons.AddProperty,
            label = stringResource(resource = Res.string.properties_add),
            sections = listOf(propertyTypeItems(onIntent = onIntent)),
            takesFocus = false
        )
        Text(
            text = stringResource(resource = Res.string.properties_add),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** One menu item per property type, each adding a property of that type. */
@Composable
private fun propertyTypeItems(
    onIntent: (EditorIntent) -> Unit
): List<NotekeaMenuItem> = PropertyTypes.all.map { (type, label, icon) ->
    NotekeaMenuItem(
        label = stringResource(resource = label),
        onClick = { onIntent(PropertyIntent.OnAdd(type = type)) },
        icon = icon
    )
}

/** The YAML as written, edited as text; left, it replaces the frontmatter. Unreadable YAML says where it fails. */
@Composable
private fun PropertiesSource(
    state: EditorState,
    invalidLine: Int?,
    onIntent: (EditorIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        invalidLine?.let { ErrorBanner(message = stringResource(Res.string.properties_invalid, it)) }
        PropertiesSourceField(source = state.propertiesSource, onCommit = {
            onIntent(PropertyIntent.OnSourceCommit(yaml = it))
        })
    }
}

/** The YAML of the source view, edited as text; once the field is left, it goes to [onCommit] when it changed. */
@Composable
private fun PropertiesSourceField(
    source: String,
    onCommit: (String) -> Unit
) {
    var text by remember(source) { mutableStateOf(value = source) }
    var hasHadFocus by remember { mutableStateOf(value = false) }
    Box(
        modifier = Modifier.onFocusChanged { focus ->
            if (focus.hasFocus) hasHadFocus = true
            if (!focus.hasFocus && hasHadFocus && text != source) onCommit(text)
        }
    ) {
        NotekeaTextField(value = text, onValueChange = {
            text = it
        }, modifier = Modifier.fillMaxWidth(), singleLine = false)
    }
}

private fun iconOf(
    value: PropertyValue
) = when (value) {
    is PropertyValue.Text -> NotekeaIcons.PropertyText
    is PropertyValue.Items -> NotekeaIcons.PropertyList
    is PropertyValue.Number -> NotekeaIcons.PropertyNumber
    is PropertyValue.Checkbox -> NotekeaIcons.PropertyCheckbox
    is PropertyValue.Date -> NotekeaIcons.PropertyDate
    is PropertyValue.Other -> NotekeaIcons.PropertyOther
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
