package app.notekea.chat.domain.loop

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.chat.domain.tool.web.WebSearchTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.Countries

/**
 * The web's checks (`docs/agent-web.md`, "The web's checks"), from what the web's evaluation of 2026-11-16 saw the
 * 4B and the 9B do; each is told once, but a person's details and their own note searched are refused every time.
 */
internal object WebChecks {
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? = when {
        call.name == WebSearchTool.NAME -> detailsSearched(
            call = call,
            state = state
        ) ?: ownNoteSearched(state = state) ?: noteSearched(
            call = call,
            state = state
        ) ?: countryUnknown(
            call = call,
            state = state
        )

        call.name in WRITTEN_FROM_NOTES -> pageWritten(state = state)

        tool?.effect == ToolEffect.ENDS -> pagesUnread(state = state)
            ?: neverSearched(state = state)
            ?: notWritten(
                call = call,
                state = state
            )

        else -> null
    }

    // Refused every time, the note read or not: the 9B read "People/Laura Castro" and then searched its name with
    // "phone number" anyway, and the pages it found were approved and read (the web's evaluation, second run).
    private fun detailsSearched(
        call: ToolCall,
        state: TurnState
    ): String? {
        val query = folded(text = call.text(name = WebSearchTool.QUERY))
        if (!DETAILS.containsMatchIn(input = query)) return null

        val named = noteNamedIn(
            query = query,
            notes = state
                .turn
                .notes
                .map { it.file.path }
        ) ?: return null

        val note = named.bare()

        return "(Not searched: a person's phone, address or email is never looked up on the web. Answer from the " +
            "note $note, or say the notes do not hold it.)"
    }

    // Refused every time: "search the web for my weekly review" searches the person's own note (not "add … to my
    // Lisbon trip note"); the 4B read it, searched "weekly review template" and appended it (the web's third run).
    private fun ownNoteSearched(state: TurnState): String? {
        val owned = state
            .turn
            .notes
            .map { it.file.path }
            .filter {
                isOwnNoteSearched(
                    task = state.task,
                    titles = listOf(titleOf(path = it))
                )
            }

        if (owned.isEmpty()) return null

        val notes = owned.joinToString { it.bare() }

        return "(Not searched: the task means the person's own note, $notes, not a page of the web. Read it and " +
            "answer from it; when two notes share its name, ask which.)"
    }

    /** Whether [task] searches for the person's own note by its title: "search the web for my weekly review". */
    fun isOwnNoteSearched(
        task: String,
        titles: List<String>
    ): Boolean {
        val folded = folded(text = task)

        return titles
            .filter {
                it.length >= SPECIFIC_TITLE && ' ' in it
            }
            .any {
                val title = Regex.escape(folded(text = it))

                val searched =
                    Regex(pattern = "$SEARCHED_FOR(my|mi|mis)\\s+$title(?![\\p{L}\\p{N}])")

                searched.containsMatchIn(input = folded)
            }
    }

    // Held once: with no country in the request or the memory, "buy soil moisture sensor" brought shops in the
    // United States to a person living in Costa Rica (the web's third run); where they live is asked first.
    private fun countryUnknown(
        call: ToolCall,
        state: TurnState
    ): String? {
        val query = call.text(name = WebSearchTool.QUERY)
        val isUnknown = state.turn.country == null && WebSearchTool.isBuying(query = query)
        val isNamed = Countries.codeIn(text = query) != null
        if (!isUnknown || isNamed) return null

        val held =
            "(Not searched yet: where the person lives is not known, and shops depend on it. Ask them " +
                "which country with ${AskUserTool.NAME}, or search once more to look anywhere.)"

        return held.takeIf { state.told.add(element = COUNTRY_UNKNOWN) }
    }

    private fun noteSearched(
        call: ToolCall,
        state: TurnState
    ): String? {
        val query = folded(text = call.text(name = WebSearchTool.QUERY))

        val read = state
            .turn
            .reads
            .read
            .toSet()

        val named = noteNamedIn(
            query = query,
            notes = state
                .turn
                .notes
                .map { it.file.path }
                .filter {
                    it !in read
                }
        ) ?: return null

        val note = named.bare()

        val held =
            "(Not searched: \"${titleOf(path = named)}\" is a note of the vault, $note. Read it first: the web is " +
                "searched only for what the notes do not hold, and never for a person's phone, address or email.)"

        return held.takeIf { state.told.add(element = NOTE_SEARCHED) }
    }

    private fun pagesUnread(state: TurnState): String? {
        val pages = (WebAsked.pagesOf(task = state.task) + state.turn.web.readable).distinct()
        if (pages.isEmpty()) return null

        val isRead = state.made.any { it.startsWith(prefix = "${FetchTool.NAME}:") }
        if (isRead || isClipped(state = state)) return null

        val held =
            "(Not done: none of these pages was read: ${pages.joinToString()}. Read them with ${FetchTool.NAME} " +
                "and answer from what they say, or ${ChangeTools.clip.name} one the task asks to keep as a note.)"

        return held.takeIf { state.told.add(element = PAGES_UNREAD) }
    }

    private fun neverSearched(state: TurnState): String? {
        val canSearch = state
            .tools
            .tools
            .any {
                it.name == WebSearchTool.NAME
            }

        val isAsked = canSearch && WebAsked.isResearch(task = state.task)
        if (!isAsked) return null

        val isSearched = state.made.any {
            it.startsWith(prefix = "${WebSearchTool.NAME}:") || it.startsWith(prefix = "${FetchTool.NAME}:")
        }

        if (isSearched) return null

        val held =
            "(Not done: the task asks to look it up on the web and nothing was searched. Call " +
                "${WebSearchTool.NAME} first; write only what the pages say, each with its link.)"

        return held.takeIf { state.told.add(element = NEVER_SEARCHED) }
    }

    // "Make detailed notes of <address>" was written by write_note from the vault's statistics notes, or became an
    // empty note: a page is kept as a note by clipping it.
    private fun pageWritten(state: TurnState): String? {
        val pages = WebAsked.pagesOf(task = state.task)
        val isKept = pages.isEmpty() || isClipped(state = state)
        if (isKept) return null

        val held = "(Not added: ${pages.joinToString()} is a web page, not a note of the vault: " +
            "${ChangeTools.writeNote.name} writes only from the notes, and ${ChangeTools.createNote.name} " +
            "makes an empty note. Keep the page as a note with ${ChangeTools.clip.name}, its address as the " +
            "task wrote it.)"

        return held.takeIf { state.told.add(element = PAGE_WRITTEN) }
    }

    // "Research … and make me a note" answered, and "add it to my trip note" answered with "I have added these ideas
    // to your note": an answer writes nothing.
    private fun notWritten(
        call: ToolCall,
        state: TurnState
    ): String? {
        val canClip = state
            .tools
            .tools
            .any {
                it.name == ChangeTools.clip.name
            }

        val isWritten = call.name != AnswerTool.NAME || state.turn.work.changes.isNotEmpty()

        val isHeld =
            canClip &&
                !isWritten &&
                WebAsked.isNoteAsked(task = state.task)

        if (!isHeld) return null

        val held =
            "(Not done: the task asks for a note, and an answer writes nothing. ${ChangeTools.clip.name} a page " +
                "the task wrote, or ${ChangeTools.append.name} what the pages say, each with its link, to the note " +
                "the task names; then ${ProposeTool.NAME}.)"

        return held.takeIf { state.told.add(element = NOT_WRITTEN) }
    }

    private fun isClipped(state: TurnState): Boolean = state
        .turn
        .work
        .changes
        .any {
            it.kind == ActionKind.CLIP_URL
        }

    // The note a search names by its title: two words or more, eight letters at least.
    private fun noteNamedIn(
        query: String,
        notes: List<RelativePath>
    ): RelativePath? = notes.firstOrNull { path ->
        val title = folded(text = titleOf(path = path))
        val isSpecific = title.length >= SPECIFIC_TITLE && ' ' in title

        // Not `\b`, ASCII on the JVM: a title that starts with "Á" was never found (the bug hunt of 2026-11-17, L6).
        val named = Regex(pattern = "(?<![\\p{L}\\p{N}])${Regex.escape(title)}(?![\\p{L}\\p{N}])")

        isSpecific && named.containsMatchIn(input = query)
    }

    private fun titleOf(path: RelativePath): String = path
        .value
        .substringAfterLast(delimiter = '/')
        .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

    private fun folded(text: String): String = text
        .lowercase()
        .trim()

    // A title of two words or more, eight letters at least ("Laura Castro", "Weekly review"): one word alone
    // ("Greenhouse", "Home") is too often only a word of what is searched.
    private const val SPECIFIC_TITLE = 8

    private const val NOTE_SEARCHED = "note searched"
    const val COUNTRY_UNKNOWN = "country unknown"

    // "search the web for", "look up online", "busca en la web": what follows is what is searched.
    private const val SEARCHED_FOR =
        "\\b(search|look up|find|busca|buscar|encuentra)( the web| on the web| online| en la web| en internet)?" +
            "( for| por)?\\s+"
    private const val PAGES_UNREAD = "pages unread"
    private const val NEVER_SEARCHED = "never searched"
    private val WRITTEN_FROM_NOTES = setOf(ChangeTools.writeNote.name, ChangeTools.createNote.name)

    private const val PAGE_WRITTEN = "page written"
    private const val NOT_WRITTEN = "not written"

    private val DETAILS = Regex(
        pattern = "\\b(phone|telephone|cell|mobile|number|address|email|e-mail|whatsapp|contact|teléfono|telefono|" +
            "celular|número|numero|dirección|direccion|correo|contacto)\\b"
    )
}
