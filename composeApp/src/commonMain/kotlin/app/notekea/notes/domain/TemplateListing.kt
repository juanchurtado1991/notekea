package app.notekea.notes.domain

/** The vault's templates (`ROADMAP.md` 1.12), as the template picker lists them. */
sealed interface TemplateListing {
    /** The templates folder does not exist yet: the picker offers to create it. */
    data object NoFolder : TemplateListing

    /** The notes in the templates folder and its subfolders, in natural order; empty when it has none yet. */
    data class Found(val templates: List<RelativePath>) : TemplateListing
}
