package app.notekea.notes.presentation.editor

import androidx.compose.ui.graphics.vector.ImageVector
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.PropertyType
import app.notekea.resources.Res
import app.notekea.resources.properties_type_checkbox
import app.notekea.resources.properties_type_date
import app.notekea.resources.properties_type_list
import app.notekea.resources.properties_type_number
import app.notekea.resources.properties_type_text
import org.jetbrains.compose.resources.StringResource

/** The property types offered by "Add property" (`ROADMAP.md` 1.14), with their names and icons, in menu order. */
internal object PropertyTypes {
    val all: List<Triple<PropertyType, StringResource, ImageVector>>
        get() = PropertyType.entries.map { Triple(it, labelOf(type = it), iconOf(type = it)) }

    fun iconOf(
        type: PropertyType
    ): ImageVector = when (type) {
        PropertyType.TEXT -> NotekeaIcons.PropertyText
        PropertyType.LIST -> NotekeaIcons.PropertyList
        PropertyType.NUMBER -> NotekeaIcons.PropertyNumber
        PropertyType.CHECKBOX -> NotekeaIcons.PropertyCheckbox
        PropertyType.DATE -> NotekeaIcons.PropertyDate
    }

    private fun labelOf(
        type: PropertyType
    ): StringResource = when (type) {
        PropertyType.TEXT -> Res.string.properties_type_text
        PropertyType.LIST -> Res.string.properties_type_list
        PropertyType.NUMBER -> Res.string.properties_type_number
        PropertyType.CHECKBOX -> Res.string.properties_type_checkbox
        PropertyType.DATE -> Res.string.properties_type_date
    }
}
