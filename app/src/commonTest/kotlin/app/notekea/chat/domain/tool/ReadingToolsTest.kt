package app.notekea.chat.domain.tool

import app.notekea.chat.domain.plan.TextParts
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.reading.DigestTool
import app.notekea.chat.domain.tool.reading.LinksTool
import app.notekea.chat.domain.tool.reading.ListTool
import app.notekea.chat.domain.tool.reading.OutlineTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteFixtures.tags
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import app.notekea.core.notes.domain.tasks.NoteTask
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The tools that read no more than is needed (`docs/agent-loop.md`, step 4): a long note seen by its outline and
 * read by its section, a folder listed, a note's links, and long documents digested outside the turn's context.
 */
class ReadingToolsTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private val lease = RelativePath(value = "Home/Lease.md")
    private val arepas = RelativePath(value = "Recipes/Arepas.md")
    private val soup = RelativePath(value = "Recipes/Soup.md")

    private val context = ToolContext(
        vaultPath = VAULT,
        notes = listOf(
            entry(
                path = lease.value,
                modifiedOffsetMillis = 3_000,
                metadata = NoteMetadata.NONE.copy(
                    tasks = listOf(true, false, false).map {
                        NoteTask(
                            stateIndex = 0,
                            endIndex = 0,
                            text = "Call Ana",
                            isDone = it
                        )
                    }
                )
            ),
            entry(
                path = arepas.value,
                modifiedOffsetMillis = 2_000,
                metadata = tags("latin")
            ),
            entry(
                path = soup.value,
                modifiedOffsetMillis = 1_000,
                metadata = NoteMetadata.NONE.copy(
                    properties = listOf(
                        NoteProperty(
                            key = "serves",
                            value = PropertyValue.Text(text = "4")
                        )
                    )
                )
            )
        ),
        task = "what does the lease say about ending it?",
        openNote = null
    )

    private suspend fun said(
        tool: ChatTool,
        vararg arguments: Pair<String, String>
    ): String = tool.run(
        call = ToolCall(
            name = tool.name,
            arguments = arguments.toMap()
        ),
        context = context
    ).forModel

    // The loop's run of 2026-11-18: Kenji's note led to his student, hers to her friends, until no step was left.
    @Test
    fun `a note on the topic of one read is asked for, and the topic of that note is not`() = runTest {
        val kenji = RelativePath(value = "People/Kenji.md")
        val ines = RelativePath(value = "People/Ines.md")
        val rita = RelativePath(value = "People/Rita.md")
        store.texts[kenji] = "My teacher. He prefers email."
        store.texts[ines] = "A former student, friend. Phone: 916 778 340."
        store.texts[rita] = "A friend from the book club. Phone: 912 000 111."

        val people = ToolContext(
            vaultPath = VAULT,
            notes = listOf(
                entry(path = kenji.value, metadata = tags("people")),
                entry(path = ines.value, metadata = tags("people", "friend")),
                entry(path = rita.value, metadata = tags("friend"))
            ),
            task = "what is the phone of my teacher?",
            openNote = null
        )

        val read = ReadTool(noteTextCache = cache)

        val said = listOf("Kenji", "Ines").map {
            read.run(
                call = ToolCall(
                    name = read.name,
                    arguments = mapOf("note" to it)
                ),
                context = people
            ).forModel
        }

        assertEquals(
            expected = listOf(true, false),
            actual = said.map { "Read them too" in it }
        )
    }

    @Test
    fun `an outline gives a note's headings, nested, with the words each part holds`() = runTest {
        store.texts[lease] = LEASE
        val outline = said(OutlineTool(noteTextCache = cache), "note" to "Lease")

        assertEquals(
            expected = listOf(
                "outline \"Lease\" (30 words):",
                "- (before the first heading)",
                "- Lease",
                "  - Rent (8 words)",
                "  - Ending it",
                "- Contacts"
            ),
            actual = outline.lines().mapIndexed { index, line ->
                val isKept = index == 0 || "Rent" in line
                if (isKept) line else line.substringBeforeLast(delimiter = " (")
            }
        )
    }

    @Test
    fun `a note is read by a section, its subsections with it, and a section it lacks gets its headings`() =
        runTest {
            store.texts[lease] = LEASE
            val read = ReadTool(noteTextCache = cache)
            val rent = said(read, "note" to "Lease", "section" to "rent")
            val whole = said(read, "note" to "Lease", "section" to "## Lease")
            val none = said(read, "note" to "Lease", "section" to "Deposit")

            assertEquals(
                expected = listOf(
                    true,
                    false,
                    true,
                    "read \"Lease\": no section \"Deposit\". Its headings: Lease, Rent, Ending it, Contacts.",
                    listOf(lease)
                ),
                actual = listOf(
                    rent.startsWith(prefix = "read \"Lease\", section \"Rent\" (8 words):") && "950 euros" in rent,
                    "60 days" in rent,
                    "950 euros" in whole &&
                        "60 days" in whole &&
                        "Ana" !in whole,
                    none,
                    context.reads.read
                )
            )
        }

    @Test
    fun `a folder or a tag is listed by name with its properties, and with neither the last changed first`() =
        runTest {
            val list = ListTool()

            assertEquals(
                expected = listOf(
                    "list, 2 notes in \"recipes\":\nRecipes/Arepas (#latin)\nRecipes/Soup (serves)",
                    "list, 1 notes tagged latin:\nRecipes/Arepas (#latin)",
                    "list, 3 notes changed last:\nHome/Lease (2 open tasks)\nRecipes/Arepas (#latin)\n" +
                        "Recipes/Soup (serves)",
                    "list: no note in \"Trips\".",
                    // A tag asked as a folder, as the 9B listed "Tags/idea" three times (the loop's fifth run).
                    "list, 1 notes tagged latin:\nRecipes/Arepas (#latin)"
                ),
                actual = listOf(
                    said(list, "folder" to "recipes"),
                    said(list, "tag" to "latin"),
                    said(list),
                    said(list, "folder" to "Trips"),
                    said(list, "folder" to "Tags/latin")
                )
            )
        }

    @Test
    fun `a note's links are the notes it links to and the ones that link to it`() = runTest {
        store.texts[lease] = "See [[Arepas]] for the dinner."
        store.texts[arepas] = "Corn flour."
        store.texts[soup] = "Goes with [[Lease]]? No: with [[Arepas]]."

        val links = LinksTool(
            noteTextCache = cache,
            linkFinder = JetBrainsMarkdownLinkFinder()
        )

        assertEquals(
            expected = listOf(
                "links \"Arepas\":\nIt links to: no note\nLinked from: Home/Lease, Recipes/Soup",
                "links \"Lease\":\nIt links to: Recipes/Arepas\nLinked from: Recipes/Soup",
                "links \"Tacos\": no note of that name."
            ),
            actual = listOf(
                said(links, "note" to "Arepas"),
                said(links, "note" to "Lease"),
                said(links, "note" to "Tacos")
            )
        )
    }

    @Test
    fun `a long note is digested part by part in calls of their own, and counts as read`() =
        runTest {
            val long = (1..6)
                .joinToString(separator = "\n\n") { "A fact about the lease. ".repeat(n = 100) }

            store.texts[lease] = long
            engine.reply = { "It can be ended with 60 days of notice." }

            val digest = said(
                DigestTool(
                    llmEngine = engine,
                    noteTextCache = cache
                ),
                "notes" to "Lease\nTacos",
                "question" to "how is it ended?"
            )

            val parts = TextParts.of(text = long).size

            assertEquals(
                expected = listOf(true, true, parts, true, listOf(lease)),
                actual = listOf(
                    digest.startsWith(prefix = "digest \"Lease\" ($parts parts read):\nIt can be ended"),
                    digest.endsWith(suffix = "digest \"Tacos\": no note of that name."),
                    engine.generated.size,
                    engine.generated.all { messages ->
                        "how is it ended?" in messages.last().text
                    },
                    context.reads.read
                )
            )
        }

    private companion object {
        const val LEASE =
            "Intro line.\n\n# Lease\nSigned in May.\n\n## Rent\nThe rent is 950 euros a month.\n\n" +
                "## Ending it\nEither side may end it with 60 days of notice.\n\n# Contacts\nAna, the landlord."
    }
}
