package app.notekea.notes.presentation.editor

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.time.CalendarDate
import app.notekea.notes.domain.FrontmatterBlocks
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.FrontmatterPatch
import app.notekea.notes.domain.PropertyKeys

/**
 * The note's frontmatter after an edit of its properties ([PropertyIntent]): one property patched, added, renamed or
 * removed, or the source view's YAML put back between the `---` lines. Null when the edit is not one to make (an
 * invalid or taken name, a property that is not there), so nothing is written.
 */
internal class PropertyEditing {
    private val patch = FrontmatterPatch()

    fun headerAfter(
        intent: PropertyIntent,
        header: String,
        content: FrontmatterContent,
        today: CalendarDate,
        lineBreak: String
    ): String? {
        val keys = (content as? FrontmatterContent.Properties)?.properties.orEmpty().map { it.key }
        return when (intent) {
            is PropertyIntent.OnValueChange -> patch.set(header = header, key = intent.key, value = intent.value)

            is PropertyIntent.OnAdd -> patch.add(header, freeKey(keys = keys), intent.type.emptyValue(today), lineBreak)

            is PropertyIntent.OnRemove -> patch.remove(header = header, key = intent.key)

            is PropertyIntent.OnRename -> if (PropertyKeys.isValid(
                    intent.to,
                    existing = keys,
                    renaming = intent.from
                )
            ) {
                patch.rename(header = header, from = intent.from, to = intent.to)
            } else {
                null
            }

            is PropertyIntent.OnSourceCommit -> sourceHeader(yaml = intent.yaml, header = header, lineBreak = lineBreak)

            PropertyIntent.OnFoldToggle, PropertyIntent.OnSourceToggle -> null
        }
    }

    /** `property`, or `property 2`, `property 3`… when it is taken: the name a new property starts with. */
    private fun freeKey(
        keys: List<String>
    ): String = generateSequence(seed = 1) { it + 1 }
        .map { if (it == 1) NEW_KEY else NEW_KEY + StringPatterns.SPACE + it }
        .first { it !in keys }

    /** The YAML of [header], the lines between its `---`, as the source view shows it; empty without frontmatter. */
    fun sourceOf(
        header: String
    ): String = FrontmatterBlocks.of(header = header)?.blocks?.flatMap { it.lines }
        ?.joinToString(separator = StringPatterns.NEW_LINE)
        .orEmpty()

    /** The frontmatter holding [yaml] as typed, its line breaks the note's; none when the source was emptied. */
    private fun sourceHeader(
        yaml: String,
        header: String,
        lineBreak: String
    ): String {
        if (yaml.isBlank()) return FrontmatterBlocks.of(header = header)?.prefix ?: StringPatterns.EMPTY
        val lines = listOf(FrontmatterBlocks.DELIMITER) + yaml.trimEnd().lines() + FrontmatterBlocks.DELIMITER
        val prefix = FrontmatterBlocks.of(header = header)?.prefix ?: StringPatterns.EMPTY
        return prefix + lines.joinToString(separator = lineBreak) + lineBreak
    }

    private companion object {
        const val NEW_KEY = "property"
    }
}
