package app.notekea.chat.domain

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

        if (passages.isEmpty()) return "search \"$query\": nothing found."

        return "search \"$query\":\n" + passages
            .take(n = PASSAGES)
            .joinToString(separator = "\n") {
                "- ${it.note.nameWithoutExtension}: ${it.text.take(n = PASSAGE_CHARS).replace('\n', ' ')}"
            }
    }

    private suspend fun read(name: String): String {
        val path = runCatching {
            VaultNames(
                notes = notes,
                openNote = openNote
            ).noteOf(name = name)
        }.getOrNull()
            ?: return "read \"$name\": no note of that name."

        val note = notes.first {
            it.file.path == path
        }

        val body = noteTextCache
            .textsOf(
                vaultPath = vaultPath,
                notes = listOf(note)
            )[path]
            ?.body
            .orEmpty()

        return "read \"${path.nameWithoutExtension}\":\n${body.take(n = NOTE_CHARS)}"
    }

    private suspend fun found(step: ActionStep): String {
        val rows = when (
            val found = actionRunner.find(vaultPath = vaultPath, step = step, notes = notes)
        ) {
            is Outcome.Success -> found.value.rows
            is Outcome.Failure -> return "${step.kind.written}: nothing found."
        }

        return "${step.kind.written} ${step.request.orEmpty()}:\n" + rows
            .take(n = ROWS)
            .joinToString(separator = "\n") {
                "- ${it.note.nameWithoutExtension} ${it.cells.joinToString(separator = " · ")}"
            }
    }

    private suspend fun fetched(
        url: String,
        task: String
    ): String {
        if (url.isBlank() || url !in task) return "fetch \"$url\": refused, the task does not write this address."

        return when (val page = pageReader.read(url = url)) {
            is Outcome.Success -> "fetch \"$url\":\n${page.value.take(n = PAGE_CHARS)}"
            is Outcome.Failure -> "fetch \"$url\": could not be read (${page.error.name.lowercase()})."
        }
    }

    private companion object {
        const val PASSAGES = 4
        const val PASSAGE_CHARS = 400
        const val NOTE_CHARS = 2_500
        const val ROWS = 30
        const val PAGE_CHARS = 3_000
    }
}
