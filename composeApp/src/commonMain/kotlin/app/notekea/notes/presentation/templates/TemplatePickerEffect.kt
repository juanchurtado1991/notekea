package app.notekea.notes.presentation.templates

import app.notekea.notes.domain.RelativePath

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
