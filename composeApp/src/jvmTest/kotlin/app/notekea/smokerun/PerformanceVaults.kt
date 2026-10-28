package app.notekea.smokerun

import java.io.File

/**
 * The vaults a performance pass measures that are generated rather than kept in `smoke-data/` (`ROADMAP.md` 1.18):
 * the Large Vault, as `smoke.sh large` writes it, and a long note to scroll in Edit and Preview.
 */
internal object PerformanceVaults {
    const val LARGE_VAULT = "Large Vault"
    const val LONG_NOTE = "Long note"
    const val LONG_NOTE_END = "The end of the long note."
    private const val LARGE_FOLDERS = 50
    private const val LARGE_NOTES_PER_FOLDER = 100
    private const val LONG_NOTE_SECTIONS = 300

    /** Writes the Large Vault into [vaults]: 5,000 notes in 50 folders, each with frontmatter and a link. */
    fun writeLargeVault(
        vaults: File
    ) {
        val vault = File(vaults, LARGE_VAULT)
        (1..LARGE_FOLDERS).forEach { folderNumber ->
            val folder = number(value = folderNumber)
            val parent = File(vault, "Folder $folder").apply { mkdirs() }
            (1..LARGE_NOTES_PER_FOLDER).forEach { noteNumber ->
                val note = largeNoteName(folder = folderNumber, note = noteNumber).substringAfter(delimiter = "Note ")
                File(parent, "Note $note.md").writeText(
                    text = "---\ntags: [large, folder-$folder]\naliases: [Note ${note.replace(' ', '-')}]\n---\n\n" +
                        "# Note $note\n\nSee [[Note $folder 001]].\n"
                )
            }
        }
    }

    /** The name of note [note] of folder [folder] in the Large Vault, as `Note 07 042`. */
    fun largeNoteName(
        folder: Int,
        note: Int
    ): String = "Note ${number(value = folder)} ${number(value = note, digits = 3)}"

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

    private fun number(
        value: Int,
        digits: Int = 2
    ): String = value.toString().padStart(length = digits, padChar = '0')
}
