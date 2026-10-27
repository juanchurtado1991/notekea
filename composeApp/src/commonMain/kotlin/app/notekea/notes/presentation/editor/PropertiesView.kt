package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaChip
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaContextMenuState
import app.notekea.core.presentation.components.NotekeaIconButton
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
import app.notekea.resources.properties_to_panel
import org.jetbrains.compose.resources.stringResource

private const val CARD_ALPHA = 0.55f
private const val HOVER_ALPHA = 0.05f
private const val KEY_WIDTH_DP = 150
private const val PANEL_KEY_WIDTH_DP = 100
private const val PANEL_PREVIEW_WIDTH_DP = 320
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
        if (!state.isPropertiesExpanded) return@Column
        PropertiesForm(
            state = state,
            isEditingSource = isEditingSource,
            onEditingChange = { isEditingSource = it },
            onIntent = onIntent
        )
    }
}

/**
 * The properties themselves, in the note's card or the side panel's tab: the YAML as written while the source is shown,
 * being typed in ([isEditingSource], told by [onEditingChange]) or unreadable, otherwise one row per property, their
 * names [keyWidth] wide and list items as pills of [chipColor], and "Add property".
 */
@Composable
internal fun PropertiesForm(
    state: EditorState,
    isEditingSource: Boolean,
    onEditingChange: (Boolean) -> Unit,
    onIntent: (EditorIntent) -> Unit,
    keyWidth: Dp = KEY_WIDTH_DP.dp,
    chipColor: Color = MaterialTheme.colorScheme.surface
) {
    val content = state.properties
    if (content is FrontmatterContent.Invalid || state.isSourceShown || isEditingSource) {
        PropertiesSource(
            state = state,
            onEditingChange = onEditingChange,
            onIntent = onIntent,
            invalidLine = (content as? FrontmatterContent.Invalid)?.line
        )
    } else {
        val properties = (content as? FrontmatterContent.Properties)?.properties.orEmpty()
        val keys = properties.map(NoteProperty::key)
        properties.forEach {
            PropertyRow(
                property = it,
                keys = keys,
                keyWidth = keyWidth,
                chipColor = chipColor,
                today = state.today,
                onIntent = onIntent
            )
        }
        AddPropertyRow(onIntent = onIntent)
    }
}

/** "Properties" with their number, folding the form, then Show in side panel and Show source / Show properties. */
@Composable
private fun PropertiesHeader(
    state: EditorState,
    count: Int,
    isEditingSource: Boolean,
    onIntent: (EditorIntent) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FoldTitle(isExpanded = state.isPropertiesExpanded, count = count, onIntent = onIntent)
        Spacer(modifier = Modifier.weight(weight = 1f))
        NotekeaIconButton(
            icon = NotekeaIcons.PropertiesToPanel,
            label = stringResource(resource = Res.string.properties_to_panel),
            onClick = { onIntent(EditorIntent.OnPropertiesPlacementToggle) },
            takesFocus = false
        )
        if (state.isPropertiesExpanded) {
            SourceToggle(state = state, isEditingSource = isEditingSource, onIntent = onIntent)
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

/**
 * Show source / Show properties, hidden while the YAML cannot be read (it is shown anyway); while the source is being
 * typed in ([isEditingSource]), Show properties leaves it, which saves it, and shows the form.
 */
@Composable
internal fun SourceToggle(
    state: EditorState,
    isEditingSource: Boolean,
    onIntent: (EditorIntent) -> Unit
) {
    if (state.properties is FrontmatterContent.Invalid) return
    val focusManager = LocalFocusManager.current
    val isSourceShown = state.isSourceShown || isEditingSource
    val onToggle = {
        if (isEditingSource) focusManager.clearFocus()
        if (state.isSourceShown || !isEditingSource) onIntent(PropertyIntent.OnSourceToggle)
    }
    NotekeaIconButton(
        icon = if (isSourceShown) NotekeaIcons.Properties else NotekeaIcons.PropertySource,
        label = stringResource(
            resource = if (isSourceShown) Res.string.properties_show_form else Res.string.properties_show_source
        ),
        onClick = onToggle,
        takesFocus = false
    )
}

/**
 * One property: its type's icon, its name (muted, renamed in place), its value, and a ✕ that removes it, shown while the
 * row is hovered or being edited; the hovered row is lightly highlighted.
 */
@Composable
private fun PropertyRow(
    property: NoteProperty,
    keys: List<String>,
    keyWidth: Dp,
    chipColor: Color,
    today: CalendarDate?,
    onIntent: (EditorIntent) -> Unit
) {
    val hover = remember { MutableInteractionSource() }
    val isHovered by hover.collectIsHoveredAsState()
    var isEditing by remember { mutableStateOf(value = false) }
    RowFrame(
        isHighlighted = isHovered,
        modifier = Modifier.hoverable(interactionSource = hover).onFocusChanged { isEditing = it.hasFocus }
    ) {
        PropertyKey(property = property, keys = keys, width = keyWidth, onIntent = onIntent)
        Box(modifier = Modifier.weight(weight = 1f)) {
            PropertyValueEditor(property = property, onIntent = onIntent, today = today, chipColor = chipColor)
        }
        NotekeaIconButton(
            icon = NotekeaIcons.Close,
            label = stringResource(resource = Res.string.properties_remove),
            onClick = { onIntent(PropertyIntent.OnRemove(key = property.key)) },
            modifier = Modifier.alpha(alpha = if (isHovered || isEditing) 1f else 0f),
            takesFocus = false
        )
    }
}

/** A row of the card: at least a menu row high, with rounded corners, highlighted while [isHighlighted]. */
@Composable
private fun RowFrame(
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.small
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = HOVER_ALPHA)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NotekeaDimensions.MenuItemHeight)
            .clip(shape = shape)
            .background(color = if (isHighlighted) highlight else Color.Transparent, shape = shape)
            .padding(horizontal = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** The type's icon and the property's name in the muted color, renamed in place. */
@Composable
private fun PropertyKey(
    property: NoteProperty,
    keys: List<String>,
    width: Dp,
    onIntent: (EditorIntent) -> Unit
) {
    Row(modifier = Modifier.width(width = width), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = iconOf(property = property),
            contentDescription = null,
            modifier = Modifier.padding(start = NotekeaDimensions.SpacingXs).size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingXs)) {
            NotekeaPropertyField(
                value = property.key,
                onCommit = { onIntent(PropertyIntent.OnRename(from = property.key, to = it)) },
                placeholder = stringResource(resource = Res.string.properties_key_label),
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                isValid = { PropertyKeys.isValid(key = it, existing = keys, renaming = property.key) }
            )
        }
    }
}

/** "Add property" as the card's last row; its menu picks the new property's type, which gets a free name. */
@Composable
private fun AddPropertyRow(
    onIntent: (EditorIntent) -> Unit
) {
    val menuState = remember { NotekeaContextMenuState() }
    val hover = remember { MutableInteractionSource() }
    val isHovered by hover.collectIsHoveredAsState()
    val belowRow = with(LocalDensity.current) { Offset(x = 0f, y = NotekeaDimensions.MenuItemHeight.toPx()) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    NotekeaContextMenuArea(
        sections = listOf(propertyTypeItems(onIntent = onIntent)),
        state = menuState,
        takesFocus = false
    ) {
        RowFrame(
            isHighlighted = isHovered,
            modifier = Modifier
                .hoverable(interactionSource = hover)
                .focusProperties { canFocus = false }
                .clickable(role = Role.Button) { menuState.open(at = belowRow) }
        ) {
            Icon(
                imageVector = NotekeaIcons.AddProperty,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = NotekeaDimensions.SpacingXs)
                    .size(size = NotekeaDimensions.IconSize),
                tint = muted
            )
            Text(
                text = stringResource(resource = Res.string.properties_add),
                modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingS),
                style = MaterialTheme.typography.bodyMedium,
                color = muted
            )
        }
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

/**
 * The YAML as written, edited as text: typed, it replaces the frontmatter once typing pauses; left, at once. Unreadable
 * YAML says where it fails, and the warning goes as soon as the fix is saved. [onEditingChange] says whether it is
 * being typed in.
 */
@Composable
private fun PropertiesSource(
    state: EditorState,
    onEditingChange: (Boolean) -> Unit,
    onIntent: (EditorIntent) -> Unit,
    invalidLine: Int?
) {
    val path = state.notePath ?: return
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        invalidLine?.let { ErrorBanner(message = stringResource(Res.string.properties_invalid, it)) }
        // A new field for each note, so what was typed in one note never goes to another.
        key(path) {
            PropertiesSourceField(
                source = state.propertiesSource,
                onEdit = { onIntent(PropertyIntent.OnSourceEdit(path = path, yaml = it)) },
                onCommit = { onIntent(PropertyIntent.OnSourceCommit(path = path, yaml = it)) },
                onFocusChange = onEditingChange
            )
        }
    }
}

/**
 * The YAML of the source view, edited as text: each change goes to [onEdit], and once the field is left it goes to
 * [onCommit] when it differs from [source]. What is typed is kept while the field is focused, even when the saved
 * [source] comes back written slightly differently; once the field is left, [source] is shown again.
 */
@Composable
private fun PropertiesSourceField(
    source: String,
    onEdit: (String) -> Unit,
    onCommit: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    var text by remember { mutableStateOf(value = source) }
    var isFocused by remember { mutableStateOf(value = false) }
    LaunchedEffect(source, isFocused) { if (!isFocused) text = source }
    Box(
        modifier = Modifier.onFocusChanged { focus ->
            if (focus.hasFocus == isFocused) return@onFocusChanged
            isFocused = focus.hasFocus
            onFocusChange(focus.hasFocus)
            if (!focus.hasFocus && text != source) onCommit(text)
        }
    ) {
        NotekeaTextField(value = text, onValueChange = {
            text = it
            onEdit(it)
        }, modifier = Modifier.fillMaxWidth(), singleLine = false)
    }
}

/** Tags keep the `#` of the tags written in the text (the toolbar's Tag); other lists show a list. */
private fun listIconOf(
    key: String
) = if (PropertyKeys.isTags(key = key)) NotekeaIcons.Tag else NotekeaIcons.PropertyList

private fun iconOf(
    property: NoteProperty
) = when (val value = property.value) {
    is PropertyValue.Items -> listIconOf(key = property.key)
    is PropertyValue.Text -> NotekeaIcons.PropertyText
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
private fun PropertiesFormPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PANEL_PREVIEW_WIDTH_DP.dp)) {
            PropertiesForm(
                state = EditorState(notePath = EditorPreviewData.path, properties = EditorPreviewData.typedProperties),
                isEditingSource = false,
                onEditingChange = {},
                onIntent = {},
                keyWidth = PANEL_KEY_WIDTH_DP.dp
            )
        }
    }
}

@Preview
@Composable
private fun SourceTogglePreview() {
    NotekeaPreviewThemes {
        SourceToggle(state = EditorState(properties = EditorPreviewData.typedProperties), isEditingSource = false) {}
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
