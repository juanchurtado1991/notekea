package app.notekea.chat.domain.agent

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.AttachedTexts
import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.chat.domain.plan.VaultNames
import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath

/**
 * What a read-only move of agent mode finds, as the text the model reads next: passages, a note's text, notes by
 * their properties, tasks, or a web page's article. A page is read only when the task writes its address.
 */
internal class AgentLookUp(
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val noteTextCache: NoteTextCache,
    private val actionRunner: ActionRunner,
    private val pageReader: PageReader,
    private val vaultPath: String,
    private val notes: Collection<NoteEntry>,
    private val openNote: RelativePath?
) {
    private val attachedTexts = AttachedTexts(noteTextCache = noteTextCache)
    private val readNotes = mutableListOf<RelativePath>()
    private val empty = mutableListOf<String>()

    /** The notes read so far that held text: what a note the task writes is written from. */
    val read: List<RelativePath> get() = readNotes.distinct()

    /** The notes read so far that held no text to work from, by name. */
    val unreadable: List<String> get() = empty.distinct()

    suspend fun result(
        move: AgentTaskMove,
        task: String
    ): String = when (move.move) {
        AgentTaskPrompt.SEARCH -> searched(query = move.query.orEmpty())

        AgentTaskPrompt.READ -> read(name = move.note.orEmpty())

        AgentTaskPrompt.FIND -> found(
            step = ActionStep(
                kind = ActionKind.FIND_NOTES,
                request = move.query
            )
        )

        AgentTaskPrompt.TASKS -> found(step = ActionStep(kind = ActionKind.SHOW_TASKS))

        AgentTaskPrompt.FETCH -> fetched(
            url = move.url.orEmpty(),
            task = task
        )

        else -> "(unknown move)"
    }

    private suspend fun searched(query: String): String {
        val passages =
            (answerFromNotes.excerpts(vaultPath, notes, query) as? Outcome.Success)?.value.orEmpty()

        val lines = passages.take(n = PASSAGES).map {
            "- ${it.note.nameWithoutExtension}: ${it.text.take(n = PASSAGE_CHARS).replace('\n', ' ')}"
        } + propertiesMatching(query = query)

        if (lines.isEmpty()) return "search \"$query\": nothing found."

        return "search \"$query\":\n" + lines.joinToString(separator = "\n")
    }

    // The index reads a note's text, not its frontmatter: notes whose property values hold a word of the query.
    private fun propertiesMatching(query: String): List<String> {
        val words = query
            .lowercase()
            .split(' ', ',', '.', '?')
            .filter {
                it.length >= MIN_WORD
            }

        return notes
            .filter { note ->
                val values = NoteProperties
                    .of(note = note)
                    .joinToString(separator = "\n")
                    .lowercase()

                words.any {
                    it in values
                }
            }
            .take(n = PROPERTY_NOTES)
            .map { note ->
                val written = NoteProperties.of(note = note).joinToString(separator = "; ")

                "- ${note.file.path.nameWithoutExtension} (properties: $written)"
            }
    }

    /** The vault's path of the note [name] names, without its extension, or null when no note has that name. */
    fun pathOf(name: String): String? = noteNamed(name = name)?.value?.removeSuffix(suffix = ".md")

    private fun noteNamed(name: String): RelativePath? = runCatching {
        VaultNames(
            notes = notes,
            openNote = openNote
        ).noteOf(name = name)
    }.getOrNull()

    private suspend fun read(name: String): String {
        val path = noteNamed(name = name) ?: return "read \"$name\": no note of that name."

        // The open tab is no note when it is a folder or a view: it reads as a name nothing matches.
        val note = notes.firstOrNull {
            it.file.path == path
        } ?: return "read \"$name\": no note of that name."

        val read = attachedTexts.of(
            vaultPath = vaultPath,
            note = note,
            notes = notes
        )

        val shown = path.nameWithoutExtension

        val unread = read
            .unread
            .takeIf { it.isNotEmpty() }
            ?.let { " It attaches ${it.joinToString()}, which holds no text that can be read." }
            .orEmpty()

        if (read.text.isBlank()) {
            empty += shown

            return "read \"$shown\": $NO_TEXT$unread"
        }

        readNotes += path
        val properties = NoteProperties.of(note = note).joinToString(separator = "") { "$it\n" }

        // Counted here, not by the model: "how many words does Arepas have?" got 109, 128 and 63 (the chat probe).
        val words = WORDS.findAll(input = read.text).count()

        val more = if (read.text.length > NOTE_CHARS) {
            "\n(Its first $NOTE_CHARS of ${read.text.length} characters. A write_from_notes step that names " +
                "this note reads all of it.)"
        } else {
            ""
        }

        val with = read
            .attached
            .takeIf { it.isNotEmpty() }
            ?.let { ", with its attached ${it.joinToString()}" }
            .orEmpty()

        return "read \"$shown\" ($words words$with):\n$properties${read.text.take(n = NOTE_CHARS)}$more$unread"
    }

    private suspend fun found(step: ActionStep): String {
        val found = when (
            val result = actionRunner.find(vaultPath = vaultPath, step = step, notes = notes)
        ) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> return "${step.kind.written}: nothing found."
        }

        val rows = found.rows

        // The query it ran, so the next move can try other words when a tag or a property was guessed wrong.
        if (rows.isEmpty()) {
            val query = found
                .query
                ?.let { " with the query:\n$it" }
                .orEmpty()

            return "${step.kind.written} ${step.request.orEmpty()}: nothing found$query."
        }

        return "${step.kind.written} ${step.request.orEmpty()}:\n" + rows
            .take(n = ROWS)
            .joinToString(separator = "\n") {
                "- ${it.note.nameWithoutExtension} ${it.cells.joinToString(separator = " · ")}" +
                    it
                        .due
                        ?.let { due -> " ($due)" }
                        .orEmpty()
            }
    }

    private suspend fun fetched(
        url: String,
        task: String
    ): String {
        // Only an address the task writes whole: not a piece of one, such as its site's root.
        if (url !in UrlsNamed.writtenIn(text = task)) return "fetch \"$url\": refused, the task does not write this address."

        return when (val page = pageReader.read(url = url)) {
            is Outcome.Success -> "fetch \"$url\":\n${page.value.take(n = PAGE_CHARS)}"
            is Outcome.Failure -> "fetch \"$url\": could not be read (${page.error.name.lowercase()})."
        }
    }

    private companion object {
        val WORDS = Regex(pattern = "[\\p{L}\\p{N}]+(['’-][\\p{L}\\p{N}]+)*")
        const val PASSAGES = 4
        const val MIN_WORD = 3
        const val NO_TEXT = "nothing found: this note holds no text to work from."
        const val PROPERTY_NOTES = 8
        const val PASSAGE_CHARS = 400
        const val NOTE_CHARS = 2_500
        const val ROWS = 30
        const val PAGE_CHARS = 3_000
    }
}
