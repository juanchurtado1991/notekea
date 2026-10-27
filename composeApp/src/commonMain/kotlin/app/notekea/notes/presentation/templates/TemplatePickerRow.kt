package app.notekea.notes.presentation.templates

import app.notekea.notes.domain.RelativePath

/** One template of the picker: its note ([path]) and its [name] inside the templates folder (`Work/Weekly review`). */
data class TemplatePickerRow(
    val path: RelativePath,
    val name: String
)
