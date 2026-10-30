package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaContextMenuState
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaPropertyField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.NoteProperty
import app.notekea.notes.domain.PropertyKeys
import app.notekea.notes.domain.PropertyValue
import app.notekea.resources.Res
import app.notekea.resources.properties_add
import app.notekea.resources.properties_key_label
import app.notekea.resources.properties_remove
import org.jetbrains.compose.resources.stringResource

private const val HOVER_ALPHA = 0.05f
private const val KEY_WIDTH_DP = 150

/**
 * One property: its type's icon, its name (muted, renamed in place), its value, and a ✕ that removes it, shown while the
 * row is hovered or being edited; the hovered row is lightly highlighted.
 */
@Composable
internal fun PropertyRow(
    property: NoteProperty,
    keys: List<String>,
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
        PropertyKey(property = property, keys = keys, onIntent = onIntent)
        Box(modifier = Modifier.weight(weight = 1f)) {
            PropertyValueEditor(property = property, today = today, onIntent = onIntent)
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
    onIntent: (EditorIntent) -> Unit
) {
    Row(modifier = Modifier.width(width = KEY_WIDTH_DP.dp), verticalAlignment = Alignment.CenterVertically) {
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
internal fun AddPropertyRow(
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
private fun PropertyRowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_ROWS_WIDTH_DP.dp)) {
            EditorPreviewData.typedProperties.properties.forEach { property ->
                PropertyRow(property = property, keys = emptyList(), today = null, onIntent = {})
            }
        }
    }
}

@Preview
@Composable
private fun AddPropertyRowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_ROWS_WIDTH_DP.dp)) { AddPropertyRow(onIntent = {}) }
    }
}

private const val PREVIEW_ROWS_WIDTH_DP = 640
