package app.notekea.smokerun

import java.io.File

/** What a performance pass measures that is generated rather than kept in `smoke-data/` (`ROADMAP.md` 1.18). */
internal object PerformanceVaults {
    const val LONG_NOTE = "Long note"
    const val LONG_NOTE_END = "The end of the long note."
    private const val LONG_NOTE_SECTIONS = 300

    /** Writes [LONG_NOTE] into the vault [vault]: 300 sections of headings, text, lists, tasks and code. */
    fun writeLongNote(
        vault: File
    ) {
        val text = buildString {
            append("# Long note\n\n")
            (1..LONG_NOTE_SECTIONS).forEach { section ->
                append("## Section $section\n\n")
                append("Paragraph $section with **bold**, *italic*, `code` and a [[Welcome]] link. ".repeat(n = 3))
                append("\n\n- Item one\n- Item two\n- [ ] A task of section $section\n\n")
                append("```kotlin\nval section = $section\n```\n\n")
            }
            append(LONG_NOTE_END).append('\n')
        }
        File(vault, "$LONG_NOTE.md").writeText(text = text)
    }
}
