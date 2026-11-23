package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.web.WebSearchTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.WebResult
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The web's checks (`docs/agent-web.md`), each from what the web's evaluation of 2026-11-16 saw: a note of the vault
 * searched for online, pages the request writes left unread, and research written with no search; and the web's
 * tools given to a note written from the web.
 */
class WebChecksTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)

    private val searched = mutableListOf<String>()
    private val fetched = mutableListOf<String>()

    private val webSearch = WebSearchTool { query, _ ->
        searched += query

        Outcome.Success(
            value = listOf(
                WebResult(
                    title = "Compost at home",
                    url = "https://www.fao.org/compost",
                    snippet = "Greens and browns."
                )
            )
        )
    }

    private val fetch = FetchTool { url ->
        fetched += url
        Outcome.Success(value = "HTTP is a client-server protocol.")
    }

    private val toolsets = Toolsets(
        byKind = mapOf(
            WorkKind.WEB to listOf(
                webSearch,
                fetch,
                ReadTool(noteTextCache = cache),
                ChangeTools.writeNote,
                ChangeTools.clip,
                ChangeTools.append,
                ProposeTool(),
                AnswerTool()
            ),
            WorkKind.WRITE to listOf(
                ReadTool(noteTextCache = cache),
                ChangeTools.writeNote,
                AnswerTool()
            )
        )
    )

    private val runTurn = loopTurn(
        engine = engine,
        cache = cache,
        toolsets = toolsets
    )

    // The model makes [first] until it is told [told]; then [then]; then it answers.
    private fun script(
        first: String,
        told: String,
        then: String
    ) {
        engine.reply = { messages ->
            val asked = messages.first().text

            val isTold = messages.any {
                told in it.text
            }

            val isDone = messages.any {
                DONE in it.text
            }

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"web","skill":"none"}"""

                asked.startsWith(prefix = CALLS) &&
                    isTold &&
                    isDone -> """{"tool":"answer","text":"$DONE"}"""

                asked.startsWith(prefix = CALLS) && isTold -> then

                asked.startsWith(prefix = CALLS) -> first

                else -> DONE
            }
        }
    }

    private suspend fun run(
        task: String,
        paths: List<String> = emptyList()
    ): List<AgentStep> = runTurn(
        task = task,
        vaultPath = VAULT,
        notes = paths.map { entry(path = it) },
        context = ActionContext(
            today = "2026-11-16",
            notes = paths.map { it.removeSuffix(suffix = ".md") }
        ),
        openNote = null
    ).toList()

    @Test
    fun `a search for a note of the vault is held back, read first, and never sent`() = runTest {
        store.texts[RelativePath(value = "Personal/Weekly review.md")] = "# Weekly review\n\nDone: 4.\n\n$DONE"

        script(
            first = """{"tool":"web_search","query":"my weekly review"}""",
            told = "the person's own note",
            then = """{"tool":"read","note":"Personal/Weekly review"}"""
        )

        val steps = run(
            task = "search the web for my weekly review",
            paths = listOf("Personal/Weekly review.md")
        )

        assertEquals(
            expected = listOf(true, emptyList<String>()),
            actual = listOf(steps.last() is AgentStep.Finished, searched)
        )
    }

    // The web's third run: the 4B read its weekly review, then searched "weekly review template" and appended it.
    @Test
    fun `the person's own note searched for is refused even once it is read`() = runTest {
        store.texts[RelativePath(value = "Personal/Weekly review.md")] = "# Weekly review\n\nDone: 4."

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"web","skill":"none"}"""
                !asked.startsWith(prefix = CALLS) -> DONE
                messages.any { "the person's own note" in it.text } -> """{"tool":"answer","text":"$DONE"}"""
                messages.any { "Done: 4." in it.text } -> """{"tool":"web_search","query":"weekly review template"}"""
                else -> """{"tool":"read","note":"Personal/Weekly review"}"""
            }
        }

        val steps = run(
            task = "search the web for my weekly review",
            paths = listOf("Personal/Weekly review.md")
        )

        assertEquals(
            expected = listOf(true, emptyList<String>()),
            actual = listOf(steps.last() is AgentStep.Finished, searched)
        )
    }

    @Test
    fun `a search for the web added to the person's note is not refused as the note searched`() {
        assertEquals(
            expected = listOf(true, false),
            actual = listOf(
                "search the web for my weekly review",
                "look up things to do and add them to my weekly review"
            ).map {
                WebChecks.isOwnNoteSearched(
                    task = it,
                    titles = listOf("Weekly review")
                )
            }
        )
    }

    // The web's third run: "buy soil moisture sensor" with no country known brought shops in the United States.
    @Test
    fun `a search for where to buy with no country known is held once, to ask where the person lives`() =
        runTest {
            val buy = """{"tool":"web_search","query":"buy soil moisture sensor"}"""

            engine.reply = { messages ->
                val asked = messages.first().text

                when {
                    asked.startsWith(prefix = "You say how a request") -> """{"work":"web","skill":"none"}"""
                    !asked.startsWith(prefix = CALLS) -> DONE
                    else -> buy
                }
            }

            val steps = run(task = "where can I buy a soil moisture sensor?")

            val told = engine
                .generated
                .any { prompt ->
                    prompt.any {
                        "where the person lives is not known" in it.text
                    }
                }

            assertEquals(
                expected = listOf(true, true, listOf("buy soil moisture sensor")),
                actual = listOf(steps.last() is AgentStep.SourcesFound, told, searched)
            )
        }

    @Test
    fun `a person's phone is never searched for, even once the note is read`() = runTest {
        store.texts[RelativePath(value = "People/Laura Castro.md")] = "# Laura Castro\n\nphone: 8701-9032"

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"web","skill":"none"}"""
                !asked.startsWith(prefix = CALLS) -> DONE
                messages.any { "is never looked up" in it.text } -> """{"tool":"answer","text":"$DONE"}"""
                messages.any { "8701-9032" in it.text } -> """{"tool":"web_search","query":"Laura Castro phone"}"""
                else -> """{"tool":"read","note":"People/Laura Castro"}"""
            }
        }

        val steps = run(
            task = "search the web for the phone number of Laura Castro",
            paths = listOf("People/Laura Castro.md")
        )

        assertEquals(
            expected = listOf(true, emptyList<String>()),
            actual = listOf(steps.last() is AgentStep.Finished, searched)
        )
    }

    @Test
    fun `a page made into notes by write_note is held back, and clipped`() = runTest {
        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"web","skill":"none"}"""
                !asked.startsWith(prefix = CALLS) -> DONE
                messages.any { "clip: added" in it.text } -> """{"tool":"propose","summary":"$DONE"}"""
                messages.any { "is a web page, not a note" in it.text } -> """{"tool":"clip","url":"$PAGE"}"""
                else -> """{"tool":"write_note","title":"HTTP","notes":["People/Laura Castro"],"text":"Notes."}"""
            }
        }

        val steps = run(task = "make detailed notes of $PAGE")

        // The clip then reads the page, which this test's fetcher has not: only the moves made are asked for.
        assertEquals(
            expected = listOf("clip"),
            actual = steps
                .filterIsInstance<AgentStep.Moving>()
                .map { it.move }
        )
    }

    @Test
    fun `a note asked for and only answered is held back, and appended`() = runTest {
        store.texts[RelativePath(value = "Travel/Lisbon trip.md")] = "# Lisbon trip"

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"web","skill":"none"}"""

                !asked.startsWith(prefix = CALLS) -> DONE

                messages.any { "append: added" in it.text } -> """{"tool":"propose","summary":"$DONE"}"""

                messages.any { "an answer writes nothing" in it.text } ->
                    """{"tool":"append","note":"Travel/Lisbon trip","text":"HTTP ($PAGE)"}"""

                messages.any { "client-server" in it.text } ->
                    """{"tool":"answer","text":"I have added it to your note."}"""

                else -> """{"tool":"fetch","url":"$PAGE"}"""
            }
        }

        val steps = run(
            task = "add what $PAGE says to my Lisbon trip note",
            paths = listOf("Travel/Lisbon trip.md")
        )

        assertEquals(
            expected = listOf(listOf(ActionKind.APPEND_TEXT)),
            actual = steps
                .filterIsInstance<AgentStep.Proposed>()
                .map { proposed ->
                    proposed.steps.map { it.kind }
                }
        )
    }

    @Test
    fun `a note is asked for by writing, making or adding one, and not by naming one`() {
        assertEquals(
            expected = listOf(true, true, true, true, false, false),
            actual = listOf(
                "investiga cómo hacer compost en casa y hazme una nota con las fuentes",
                "make detailed notes of $PAGE",
                "look up what to see in Lisbon in 3 days and add it to my Lisbon trip note",
                "find three good beginner books on statistics and add them to my reading list",
                "busca en internet lo que dice mi nota de sueño y memoria sobre el sueño REM",
                "check online whether the hotels in my Lisbon trip note have good reviews"
            ).map { WebAsked.isNoteAsked(task = it) }
        )
    }

    @Test
    fun `an answer that never read the page the request writes is held back until the page is read`() =
        runTest {
            script(
                first = """{"tool":"answer","text":"HTTP is a protocol."}""",
                told = "none of these pages was read",
                then = """{"tool":"fetch","url":"$PAGE"}"""
            )

            engine.replyAfter(seen = "client-server")

            val steps = run(task = "summarize $PAGE")

            assertEquals(
                expected = listOf(true, listOf(PAGE)),
                actual = listOf(steps.last() is AgentStep.Finished, fetched)
            )
        }

    @Test
    fun `research ended with no search is held back until the web is searched`() = runTest {
        script(
            first = """{"tool":"answer","text":"Compost needs greens and browns."}""",
            told = "nothing was searched",
            then = """{"tool":"web_search","query":"compost at home"}"""
        )

        engine.replyAfter(seen = "fao.org")

        val steps = run(task = "research how to make compost at home")

        assertEquals(
            expected = listOf(true, listOf("compost at home")),
            actual = listOf(steps.last() !is AgentStep.Stopped, searched)
        )
    }

    @Test
    fun `a note written from the web is given the web's tools, and one written from the notes is not`() {
        fun namesOf(task: String) = toolsets
            .of(
                kind = WorkKind.WRITE,
                task = task
            )
            .tools
            .map { it.name }

        assertEquals(
            expected = listOf(
                listOf("read", "write_note", "web_search", "fetch", "clip", "answer"),
                listOf("read", "write_note", "web_search", "fetch", "clip", "answer"),
                listOf("read", "write_note", "answer")
            ),
            actual = listOf(
                namesOf(task = "research compost and write me a note with the sources"),
                namesOf(task = "make a note of $PAGE"),
                namesOf(task = "write a summary of my meetings")
            )
        )
    }

    // After [seen] comes back from a page or a search, the model has what it needs: it answers.
    private fun FakeLlmEngine.replyAfter(seen: String) {
        val before = reply

        reply = { messages ->
            if (messages.any { seen in it.text }) {
                """{"tool":"answer","text":"$DONE"}"""
            } else {
                before(messages)
            }
        }
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val DONE = "Done from what was found."
        const val PAGE = "https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview"
    }
}
