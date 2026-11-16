package app.notekea.chat.domain.skill

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath

/**
 * What a vault tells the chat about itself (`docs/agent-loop.md`): its instructions, one note always read (where new
 * notes go, the tags in use, the language: what `CLAUDE.md` is to Claude Code), and the person's own skills, a note
 * each in a folder.
 */
class VaultGuide(private val noteTextCache: NoteTextCache) {
    /** The text of the vault's instructions note at [note]; null when the vault has none or it is empty. */
    suspend fun instructionsOf(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        note: RelativePath = INSTRUCTIONS
    ): String? {
        val entry = notes.firstOrNull {
            it
                .file
                .path
                .value
                .equals(
                    other = note.value,
                    ignoreCase = true
                )
        } ?: return null

        return textsOf(
            vaultPath = vaultPath,
            notes = listOf(entry)
        )[entry.file.path]
            ?.take(n = INSTRUCTIONS_CHARS)
            ?.takeIf { it.isNotBlank() }
    }

    /**
     * The skills to choose from: the app's, and the notes of [folder], each a skill named as its note, described by
     * its first line and instructed by the rest. One named as a skill of the app replaces it.
     */
    suspend fun skillsOf(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        folder: String = SKILLS_FOLDER
    ): List<Skill> {
        val prefix = "${folder.trim('/').lowercase()}/"

        val own = notes
            .filter { note ->
                val path = note.file.path
                path.isMarkdown && path.value.lowercase().startsWith(prefix = prefix)
            }
            .take(n = MAX_SKILLS)

        val written = textsOf(
            vaultPath = vaultPath,
            notes = own
        ).mapNotNull { (path, text) ->
            skillOf(
                name = path.nameWithoutExtension,
                text = text
            )
        }

        val names = written.mapTo(destination = HashSet()) { it.name.lowercase() }

        return BuiltInSkills.all.filter {
            it.name.lowercase() !in names
        } + written.sortedBy { it.name }
    }

    private fun skillOf(
        name: String,
        text: String
    ): Skill? {
        val lines = text
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val description = lines
            .firstOrNull()
            ?.trimStart('#', ' ')
            ?: return null

        return Skill(
            name = name.lowercase(),
            description = description.take(n = DESCRIPTION_CHARS),
            instructions = lines
                .drop(n = 1)
                .joinToString(separator = "\n")
                .ifEmpty { description }
                .take(n = SKILL_CHARS)
        )
    }

    private suspend fun textsOf(
        vaultPath: String,
        notes: List<NoteEntry>
    ): Map<RelativePath, String> = noteTextCache
        .textsOf(
            vaultPath = vaultPath,
            notes = notes
        )
        .mapValues {
            it
                .value
                .body
                .trim()
        }

    companion object {
        /**
         * Where the vault's instructions and the person's skills are looked for until a setting says otherwise. The
         * instructions live in a folder of Notekea's own, where the chat's memory is meant to go too.
         */
        val INSTRUCTIONS = RelativePath(value = "Notekea/Notekea.md")
        const val SKILLS_FOLDER = "Skills"

        // Always in the prompt, so short: about 500 tokens of instructions, 300 of a skill.
        private const val INSTRUCTIONS_CHARS = 2_000
        private const val SKILL_CHARS = 1_200
        private const val DESCRIPTION_CHARS = 120
        private const val MAX_SKILLS = 12
    }
}
