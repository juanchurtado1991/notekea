package app.notekea.chat.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * A planned step checked against the vault (3.9): the [notes] it acts on, found by their names (up to 50, a folder or a
 * tag expanded), the [folder] and [target] it names, the [template] note, and the fields as written. [content] is the
 * text it will write, prepared before the card so the person confirms what will be written, not a promise of it.
 */
data class ActionStep(
    val kind: ActionKind,
    val notes: List<RelativePath> = emptyList(),
    val folder: RelativePath? = null,
    val target: RelativePath? = null,
    val template: RelativePath? = null,
    val title: String? = null,
    val text: String? = null,
    val property: String? = null,
    val value: String? = null,
    val tag: String? = null,
    val due: String? = null,
    val period: String? = null,
    val panel: String? = null,
    val theme: String? = null,
    val request: String? = null,
    val content: List<NoteDraft> = emptyList()
)
