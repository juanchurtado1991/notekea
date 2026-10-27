package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.PropertyType
import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.RelativePath

/** What the user did in the properties above the note (`ROADMAP.md` 1.14). */
sealed interface PropertyIntent : EditorIntent {
    /** A property's value was edited (a field left, a box checked, a date picked, a list item added or removed). */
    data class OnValueChange(
        val key: String,
        val value: PropertyValue
    ) : PropertyIntent

    /** "Add property": a new property of [type], with that type's empty value and a free name to rename. */
    data class OnAdd(val type: PropertyType) : PropertyIntent

    data class OnRemove(val key: String) : PropertyIntent

    data class OnRename(
        val from: String,
        val to: String
    ) : PropertyIntent

    /**
     * The YAML of [path]'s source view, the lines between the `---`, was typed: it replaces the frontmatter as written
     * once typing pauses, like the note's text. Ignored once another note is open.
     */
    data class OnSourceEdit(
        val path: RelativePath,
        val yaml: String
    ) : PropertyIntent

    /** [path]'s source view was left: its YAML replaces the frontmatter at once. Ignored once another note is open. */
    data class OnSourceCommit(
        val path: RelativePath,
        val yaml: String
    ) : PropertyIntent

    /** The Properties header: folds or opens the form, remembered for every note. */
    data object OnFoldToggle : PropertyIntent

    /** "Show source" / "Show properties". */
    data object OnSourceToggle : PropertyIntent
}
