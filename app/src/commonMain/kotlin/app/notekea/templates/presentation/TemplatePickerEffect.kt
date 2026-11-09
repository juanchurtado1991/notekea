package app.notekea.templates.presentation

import app.notekea.core.notes.domain.RelativePath

/** One-shot events of the template picker. */
sealed interface TemplatePickerEffect {
    /** [template] was chosen for [target]. */
    data class TemplateChosen(
        val template: RelativePath,
        val target: TemplateTarget
    ) : TemplatePickerEffect

    /** Closed without choosing: the note's text gets the keyboard and its selection back. */
    data object Dismissed : TemplatePickerEffect
}
