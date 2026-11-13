package app.notekea.core.notes.domain.templates

import app.notekea.core.notes.domain.RelativePath

/** The vault's templates, as the template picker lists them. */
sealed interface TemplateListing {
    /** The templates folder does not exist yet: the picker offers to create it. */
    data object NoFolder : TemplateListing

    /** The notes in the templates folder and its subfolders, in natural order; empty when it has none yet. */
    data class Found(val templates: List<RelativePath>) : TemplateListing
}
