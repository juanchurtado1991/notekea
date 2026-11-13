package app.notekea.chat.domain.prompt

import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.notes.domain.NoteEntry

/**
 * What the model is given to say what the vault holds ("what is in my notes?"): its folders with how many notes and
 * their names, the notes at its root and its most used tags. Names only: a few passages would describe a few notes.
 */
object OverviewPrompt {
    private const val INSTRUCTIONS =
        "You are the assistant of Notekea, a notes app. Say what the user's notes are about, from the map of the " +
            "vault below: group them by folder, name a few notes of each, and say what they seem to be for. Use " +
            "only the names given and never claim what a note says. Reply in the language of the user's message, " +
            "in four to seven sentences or a short list, and end by offering to answer a question about any of them. " +
            "Asked which notes matter most, name the ones marked with a priority, a status or a due date, and why."

    private const val NAMES_PER_FOLDER = 8
    private const val MAX_FOLDERS = 25
    private const val MAX_TAGS = 15
    private val MARKS = setOf("priority", "status", "due")

    fun messages(
        question: String,
        notes: Collection<NoteEntry>
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = "$INSTRUCTIONS\n\nVault:\n${mapOf(notes = notes)}"
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = question
        )
    )

    /** The vault as the model reads it: a line per folder, the root's notes, the tags. */
    fun mapOf(notes: Collection<NoteEntry>): String {
        val byFolder = notes.groupBy {
            it
                .file
                .path
                .parent
                ?.takeUnless { folder -> folder.isRoot }
                ?.value
        }

        val folders = byFolder
            .filterKeys {
                it != null
            }
            .entries
            .sortedByDescending { it.value.size }
            .take(n = MAX_FOLDERS)
            .map { (folder, inIt) -> "- $folder (${inIt.size}): ${namesOf(notes = inIt)}" }

        val root = byFolder[null]?.let { "- At the root (${it.size}): ${namesOf(notes = it)}" }

        val tags = notes
            .flatMap {
                it.metadata.tags + it.metadata.textTags
            }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(n = MAX_TAGS)
            .joinToString { "#${it.key}" }
            .takeIf { it.isNotEmpty() }
            ?.let { "Tags: $it" }

        // What marks a note as one that matters: "which are the most important?" found nothing marked "important".
        val marked = notes
            .mapNotNull { note ->
                val keys = note
                    .metadata
                    .properties
                    .filter {
                        it.key.lowercase() in MARKS
                    }

                if (keys.isEmpty()) return@mapNotNull null

                val said = NoteProperties.of(note = note).filter { line ->
                    keys.any { line.startsWith(prefix = "${it.key}:") }
                }

                "${note.file.path.nameWithoutExtension} (${said.joinToString()})"
            }
            .take(n = NAMES_PER_FOLDER)
            .joinToString(separator = "; ")
            .takeIf { it.isNotEmpty() }
            ?.let { "Marked: $it" }

        val lines = listOf("${notes.size} notes.") + folders + listOfNotNull(root, tags, marked)

        return lines.joinToString(separator = "\n")
    }

    private fun namesOf(notes: List<NoteEntry>): String {
        val names = notes
            .map { it.file.path.nameWithoutExtension }
            .sorted()

        val more = names.size - NAMES_PER_FOLDER

        val rest = if (more > 0) " and $more more" else ""

        return names.take(n = NAMES_PER_FOLDER).joinToString() + rest
    }
}
