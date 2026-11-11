package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Writes the text a step will add before its card is shown (3.9), so the person confirms the real text: a new note's
 * body from the request, a note written from other notes (long notes read as digests first, so 30 notes fit), or a
 * pasted text turned into linked notes with an index. Steps that write nothing come back as they were.
 */
class ComposeNotesUseCase(
    private val llmEngine: LlmEngine,
    private val noteTextCache: NoteTextCache,
    private val planReader: ActionPlanReader,
    private val clipper: WebPageClipper? = null,
    private val cardMaker: CardMaker? = null
) {
    suspend operator fun invoke(
        step: ActionStep,
        request: String,
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): Outcome<ActionStep, ActionProblem> = when (step.kind) {
        ActionKind.CLIP_URL -> clipped(
            step = step,
            request = request
        )

        ActionKind.MAKE_CARDS -> withCards(
            step = step,
            vaultPath = vaultPath
        )

        else -> composed(
            step = step,
            request = request,
            vaultPath = vaultPath,
            notes = notes
        )
    }

    private suspend fun composed(
        step: ActionStep,
        request: String,
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): Outcome<ActionStep, ActionProblem> = try {
        val content = when (step.kind) {
            ActionKind.CREATE_NOTE -> newNote(
                step = step,
                request = request
            )

            ActionKind.WRITE_FROM_NOTES -> fromNotes(
                step = step,
                request = request,
                vaultPath = vaultPath,
                notes = notes
            )

            ActionKind.NOTES_FROM_TEXT -> fromText(
                step = step,
                request = request
            )

            else -> emptyList()
        }

        val known = (notes.map { it.file.path } + content.map { it.path })
            .mapTo(destination = HashSet()) { it.nameWithoutExtension.lowercase() }

        Outcome.Success(
            value = step.copy(
                content = content.map {
                    it.copy(
                        text = cleaned(
                            text = it.text,
                            known = known
                        )
                    )
                }
            )
        )
    } catch (exception: LlmException) {
        Outcome.Failure(error = ActionProblem.Failed(ChatError.Model(exception.error)))
    } catch (problem: ComposeProblem) {
        Outcome.Failure(error = problem.problem)
    }

    private fun problem(problem: ActionProblem): Outcome<ActionStep, ActionProblem> =
        Outcome.Failure(error = problem)

    /** The study cards of the step's note, to add at its end (4.6). */
    private suspend fun withCards(
        step: ActionStep,
        vaultPath: String
    ): Outcome<ActionStep, ActionProblem> {
        val note =
            step.notes.firstOrNull() ?: return problem(problem = ActionProblem.UnknownNote(name = ""))

        val maker = cardMaker ?: return Outcome.Failure(error = ActionProblem.NoCards)

        return when (val made = maker.cardsFor(vaultPath = vaultPath, note = note)) {
            is Outcome.Success -> Outcome.Success(value = step.copy(text = made.value))
            is Outcome.Failure -> made
        }
    }

    /** A web page clipped as it is written (4.4): its note is the page's, never cleaned like the model's notes. */
    // The page alone, or with the pages it links under its path when the request says so; asked when it says neither.
    private suspend fun clipped(
        step: ActionStep,
        request: String
    ): Outcome<ActionStep, ActionProblem> {
        val clipping =
            clipper ?: return problem(ActionProblem.PageFailed(reason = PageProblem.REFUSED))

        val url = step.url.orEmpty()

        if (ClipWords.isSection(request)) {
            return when (val clips = clipping.clipSection(url = url)) {
                is Outcome.Success -> Outcome.Success(value = step.copy(content = clips.value))
                is Outcome.Failure -> problem(ActionProblem.PageFailed(reason = clips.error))
            }
        }

        if (!ClipWords.isOnly(request)) {
            val section = clipping.sectionOf(url = url)
            val isAsked = section is Outcome.Success && section.value.count > 0
            if (isAsked) return problem(ActionProblem.SectionAsked(section = section.value))
        }

        return when (val clip = clipping.clip(url = url)) {
            is Outcome.Success -> Outcome.Success(value = step.copy(content = listOf(clip.value)))
            is Outcome.Failure -> problem(problem = ActionProblem.PageFailed(reason = clip.error))
        }
    }

    private suspend fun newNote(
        step: ActionStep,
        request: String
    ): List<NoteDraft> {
        val title = step.title.orEmpty()
        val what = step.text
        if (step.template != null || what == null) return emptyList()

        val body = written(
            messages = WritePrompt.fromRequest(
                request = request,
                what = what
            ),
            maxTokens = FREE_TOKENS
        )

        return listOf(
            NoteDraft(
                path = pathOf(
                    folder = step.folder,
                    title = title
                ),
                text = "# $title\n\n$body\n"
            )
        )
    }

    private suspend fun fromNotes(
        step: ActionStep,
        request: String,
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): List<NoteDraft> {
        val wanted = step.notes.toSet()

        val read = noteTextCache.textsOf(
            vaultPath = vaultPath,
            notes = notes.filter {
                it.file.path in wanted
            }
        )

        val sources = step.notes.mapNotNull { path ->
            read[path]?.let {
                path.nameWithoutExtension to it.body.trim()
            }
        }

        val noNotes = ActionProblem.Missing(step.kind, ActionField.NOTES)
        if (sources.isEmpty()) throw ComposeProblem(noNotes)

        val isTooLong = sources.sumOf { it.second.length } > READ_AT_ONCE_CHARS

        val given = if (isTooLong) {
            digested(
                request = request,
                sources = sources
            )
        } else {
            sources
        }

        val title = step.title.orEmpty()

        val body = written(
            messages = WritePrompt.fromNotes(
                request = request,
                what = step.text.orEmpty(),
                sources = given
            ),
            maxTokens = WritePrompt.wordsFor(notes = sources.size) * TOKENS_PER_WORD
        )

        // The note goes to the folder asked for, else beside the notes it comes from when they share one.
        val folder = step.folder ?: step
            .notes
            .map { it.parent }
            .distinct()
            .singleOrNull()

        return listOf(
            NoteDraft(
                path = pathOf(
                    folder = folder,
                    title = title
                ),
                text = "# $title\n\n$body\n"
            )
        )
    }

    /** Each source read alone into a few sentences, several at once, when together they are too long to read. */
    private suspend fun digested(
        request: String,
        sources: List<Pair<String, String>>
    ): List<Pair<String, String>> = coroutineScope {
        sources.map { (name, text) ->
            async {
                val messages = WritePrompt.digest(
                    request = request,
                    name = name,
                    text = text.take(n = DIGEST_CHARS)
                )

                name to written(
                    messages = messages,
                    maxTokens = DIGEST_TOKENS
                )
            }
        }.awaitAll()
    }

    private suspend fun fromText(
        step: ActionStep,
        request: String
    ): List<NoteDraft> {
        val noFolder = ActionProblem.Missing(step.kind, ActionField.FOLDER)
        val folder = step.folder ?: throw ComposeProblem(noFolder)

        val parts = partsOf(text = request)

        if (parts.size > MAX_PARTS) {
            throw ComposeProblem(ActionProblem.TextTooLong(request.split(WHITESPACE).size))
        }

        val made = mutableListOf<Pair<String, String>>()

        parts.forEach { part ->
            val json = written(
                messages = TextNotesPrompt.messages(
                    request = request,
                    part = part,
                    usedTitles = made.map { it.first }
                ),
                maxTokens = PART_TOKENS,
                jsonSchema = TextNotesPrompt.SCHEMA
            )

            made += planReader.readNotes(json = json).orEmpty()
        }

        if (made.isEmpty()) throw ComposeProblem(ActionProblem.Missing(step.kind, ActionField.TEXT))

        val notes = made
            .map { (title, text) ->
                fileName(title = title) to text
            }
            .distinctBy { it.first.lowercase() }
            .take(n = MAX_TEXT_NOTES)

        val index = "# ${folder.name}\n\n" +
            notes.joinToString(separator = "\n") { "- [[${it.first}]]" } + "\n"

        return listOf(
            NoteDraft(
                path = folder.child(name = folder.name + MARKDOWN),
                text = index
            )
        ) +
            notes.map { (title, text) ->
                NoteDraft(
                    path = folder.child(name = title + MARKDOWN),
                    text = "# $title\n\n$text\n"
                )
            }
    }

    /**
     * [text] as a note: its own title only (models add one of theirs, or stray `# A # B` lines), and links only to
     * notes that exist or are written with it ([known]); a link to a note that is not there reads as its words.
     */
    private fun cleaned(
        text: String,
        known: Set<String>
    ): String {
        val lines = text.lines()
        val body = lines.drop(n = 1).filterNot { TITLE_LINE.matches(input = it) }
        val joined = (listOf(lines.first()) + body).joinToString(separator = "\n")

        return WIKI_LINK
            .replace(input = joined) { link ->
                val target = link
                    .groupValues[1]
                    .substringBefore(delimiter = '|')
                    .substringBefore(delimiter = '#')

                val isKnown = target.substringAfterLast(delimiter = '/').lowercase() in known

                if (isKnown) link.value else link.groupValues[1].substringAfter(delimiter = '|')
            }
    }

    /** [text] in parts the model can read at once, cut between paragraphs. */
    private fun partsOf(text: String): List<String> {
        val parts = mutableListOf<StringBuilder>()

        text.split(PARAGRAPH).forEach { paragraph ->
            val last = parts.lastOrNull()
            val fits = last != null && last.length + paragraph.length <= PART_CHARS

            if (fits) {
                last.append("\n\n").append(paragraph)
            } else {
                parts += StringBuilder(paragraph.take(n = PART_CHARS))
            }
        }

        return parts.map { it.toString() }.filter { it.isNotBlank() }
    }

    private suspend fun written(
        messages: List<ChatMessage>,
        maxTokens: Int,
        jsonSchema: String? = null
    ): String {
        val text = StringBuilder()

        llmEngine
            .generate(
                messages = messages,
                options = GenerationOptions(
                    maxTokens = maxTokens,
                    temperature = TEMPERATURE,
                    jsonSchema = jsonSchema
                )
            )
            .collect { text.append(it) }

        return text.toString().trim()
    }

    private fun pathOf(
        title: String,
        folder: RelativePath?
    ): RelativePath = (folder ?: RelativePath.ROOT).child(name = fileName(title = title) + MARKDOWN)

    /** [title] as a file's name: the characters no system allows in one are left out. */
    private fun fileName(title: String): String = title
        .replace(FORBIDDEN, "")
        .trim()
        .ifEmpty { UNTITLED }

    private class ComposeProblem(val problem: ActionProblem) : RuntimeException()

    private companion object {
        const val MARKDOWN = ".md"
        const val UNTITLED = "Untitled"
        const val TEMPERATURE = 0.3f
        const val TOKENS_PER_WORD = 2
        const val FREE_TOKENS = 600
        const val DIGEST_TOKENS = 120
        const val PART_TOKENS = 1_400

        // About 2,500 tokens of notes, which leaves room for the instructions and the answer in a 4,096 context.
        const val READ_AT_ONCE_CHARS = 9_000
        const val DIGEST_CHARS = 6_000
        const val PART_CHARS = 5_000
        const val MAX_PARTS = 5
        const val MAX_TEXT_NOTES = 15
        val TITLE_LINE = Regex(pattern = "^#\\s.*")
        val WIKI_LINK = Regex(pattern = "\\[\\[([^\\]]+)]]")
        val PARAGRAPH = Regex(pattern = "\\n\\s*\\n")
        val WHITESPACE = Regex(pattern = "\\s+")
        val FORBIDDEN = Regex(pattern = "[\\\\/:*?\"<>|#^\\[\\]]")
    }
}
