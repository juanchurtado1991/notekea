package app.notekea.notes.domain

/** What a note's frontmatter holds for the properties view (`ROADMAP.md` 1.14). */
sealed interface FrontmatterContent {
    /** The note has no frontmatter. */
    data object None : FrontmatterContent

    /** Its [properties], in the order they are written. */
    data class Properties(val properties: List<NoteProperty>) : FrontmatterContent

    /** Text the form cannot read as properties, at [line] (1 for the first line under `---`): shown as source only. */
    data class Invalid(val line: Int) : FrontmatterContent
}
