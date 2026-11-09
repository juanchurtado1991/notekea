package app.notekea.templates.presentation

import app.notekea.core.notes.domain.RelativePath

/** One template of the picker: its note ([path]) and its [name] inside the templates folder (`Work/Weekly review`). */
data class TemplatePickerRow(
    val path: RelativePath,
    val name: String
)
