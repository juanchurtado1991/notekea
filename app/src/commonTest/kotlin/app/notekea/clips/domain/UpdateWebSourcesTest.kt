package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Updating the web sources (the developer's request of 2026-11-13): a changed page gets its new text under
 * what was written above it, a section's new page becomes a note, and what did not change is left alone.
 */
class UpdateWebSourcesTest {
    private val engine = FakeLlmEngine().apply {
        readiness = Outcome.Failure(error = LlmError.NoModel)
    }
    private val contents = FakeNoteContentStore()
    private val files = FakeAttachmentStore()

    private val articles = mutableMapOf(
        "https://x.com/docs" to "Intro, rewritten this week.",
        "https://x.com/docs/axiom" to "Send traces to Axiom.",
        "https://x.com/docs/datadog" to "Send traces to Datadog.",
        "https://y.com/sleep" to "Sleep sorts what was learned."
    )

    // "/docs/latest" answers as "/docs/axiom", as a site that redirects does.
    private val redirects = mutableMapOf<String, String>()
    private var onFetch: (String) -> Unit = {}

    private val fetcher = PageFetcher { asked ->
        val url = redirects[asked] ?: asked
        onFetch(url)

        if (url in articles) {
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = links
                )
            )
        } else {
            Outcome.Failure(error = ClipError.Refused(status = 404))
        }
    }

    private var links = "<a href=/docs/axiom>Axiom</a> <a href=/docs/datadog>Datadog</a>"

    private val clipWebPage = ClipWebPageUseCase(
        fetcher = fetcher,
        extractor = { page ->
            Outcome.Success(
                value = Article(
                    title = page
                        .url
                        .substringAfterLast(delimiter = '/')
                        .replaceFirstChar { it.uppercase() },
                    paragraphs = listOf(articles.getValue(page.url))
                )
            )
        },
        llmEngine = engine,
        supportCheck = SupportCheck(llmEngine = engine)
    )

    private val update = UpdateWebSourcesUseCase(
        clipWebPage = clipWebPage,
        findSection = FindSectionUseCase(fetcher = fetcher),
        attachmentStore = files,
        noteContentStore = contents,
        noteFileOperations = FakeNoteFileOperations()
    )

    private fun note(
        path: String,
        source: String,
        original: String,
        above: String = ""
    ) {
        val text = "---\nsource: \"$source\"\nclipped: 2026-10-01\n---\n\n# Title\n\n$above" +
            "## Original text\n\n$original\n"

        contents.texts[RelativePath(value = path)] = text
        files.contents[RelativePath(value = path)] = byteArrayOf()
    }

    private fun textOf(path: String): String = contents.texts.getValue(RelativePath(value = path))

    @Test
    fun `a changed page keeps what was written above it, a new page joins its index, the rest stay`() =
        runTest {
            note(
                path = "Web sources/Docs.md",
                source = "https://x.com/docs",
                original = "Intro, as it was.",
                above = "My own line.\n\n## Pages\n\n- [[Docs/Axiom|Axiom]]\n\n"
            )

            note(
                path = "Web sources/Docs/Axiom.md",
                source = "https://x.com/docs/axiom",
                original = "Send traces to Axiom."
            )

            note(
                path = "Web sources/Sleep.md",
                source = "https://y.com/sleep",
                original = "Sleep sorts what was learned."
            )

            note(
                path = "Recipes/Arepas.md",
                source = "https://x.com/docs",
                original = "Not a web source."
            )

            val steps = update(
                vaultPath = "/vault",
                target = RelativePath(value = "Web sources"),
                today = "2026-11-13"
            ).toList()

            val index = contents.texts.getValue(RelativePath(value = "Web sources/Docs.md"))

            assertEquals(
                expected = listOf<Any?>(
                    UpdateStep.Done(
                        updated = listOf(RelativePath(value = "Web sources/Docs.md")),
                        added = listOf(RelativePath(value = "Web sources/Docs/Datadog.md")),
                        unchanged = 2,
                        failed = 0
                    ),
                    true,
                    true,
                    true,
                    "Send traces to Datadog.",
                    "Not a web source."
                ),
                actual = listOf(
                    steps.last(),
                    "My own line." in index && "clipped: 2026-11-13" in index,
                    index.trimEnd().endsWith(suffix = "$ORIGINAL\n\nIntro, rewritten this week."),
                    "- [[Docs/Axiom|Axiom]]\n- [[Docs/Datadog|Datadog]]" in index,
                    WebSourceNote.originalOf(
                        text = textOf(path = "Web sources/Docs/Datadog.md")
                    ),
                    WebSourceNote.originalOf(text = textOf(path = "Recipes/Arepas.md"))
                )
            )
        }

    @Test
    fun `only the note asked is read again, alone as it was clipped, and a page that fails is left`() =
        runTest {
            note(
                path = "Web sources/Sleep.md",
                source = "https://y.com/sleep",
                original = "Old text."
            )

            note(
                path = "Web sources/Gone.md",
                source = "https://y.com/gone",
                original = "Kept as it is."
            )

            val sleep = update(
                vaultPath = "/vault",
                target = RelativePath(value = "Web sources/Sleep.md"),
                today = "2026-11-13"
            ).toList()

            val gone = update(
                vaultPath = "/vault",
                target = RelativePath(value = "Web sources/Gone.md"),
                today = "2026-11-13"
            ).toList()

            assertEquals(
                expected = listOf<Any?>(
                    listOf(
                        UpdateStep.Reading(
                            done = 0,
                            total = 1,
                            url = "https://y.com/sleep"
                        )
                    ),
                    UpdateStep.Done(updated = listOf(RelativePath(value = "Web sources/Sleep.md"))),
                    UpdateStep.Done(failed = 1),
                    "Kept as it is."
                ),
                actual = listOf(
                    sleep.filterIsInstance<UpdateStep.Reading>(),
                    sleep.last(),
                    gone.last(),
                    WebSourceNote.originalOf(text = textOf(path = "Web sources/Gone.md"))
                )
            )
        }

    // The code review of 2026-11-13: the notes were read when the update began and written minutes later.
    @Test
    fun `what the person writes in a note while the update runs is kept`() = runTest {
        note(
            path = "Web sources/Sleep.md",
            source = "https://y.com/sleep",
            original = "Old text."
        )

        val path = RelativePath(value = "Web sources/Sleep.md")

        onFetch = {
            contents.texts[path] = contents
                .texts
                .getValue(path)
                .replace("# Title", "# Title\n\nWritten meanwhile.")
        }

        update(
            vaultPath = "/vault",
            target = path,
            today = "2026-11-13"
        ).toList()

        val text = contents.texts.getValue(path)

        assertEquals(
            expected = listOf(true, true),
            actual = listOf("Written meanwhile." in text, "Sleep sorts what was learned." in text)
        )
    }

    @Test
    fun `a new page never takes the name of a note the person keeps in the section's folder`() =
        runTest {
            note(
                path = "Web sources/Docs.md",
                source = "https://x.com/docs",
                original = "Intro, rewritten this week.",
                above = "## Pages\n\n- [[Docs/Axiom|Axiom]]\n\n"
            )

            note(
                path = "Web sources/Docs/Axiom.md",
                source = "https://x.com/docs/axiom",
                original = "Send traces to Axiom."
            )

            val mine = RelativePath(value = "Web sources/Docs/Datadog.md")
            contents.texts[mine] = "My own note on Datadog."
            files.contents[mine] = byteArrayOf()

            val done = update(
                vaultPath = "/vault",
                target = RelativePath(value = "Web sources"),
                today = "2026-11-13"
            ).toList().last() as UpdateStep.Done

            assertEquals(
                expected = listOf<Any>("My own note on Datadog.", listOf(RelativePath(value = "Web sources/Docs/Datadog 2.md"))),
                actual = listOf(contents.texts.getValue(mine), done.added)
            )
        }

    @Test
    fun `a link that redirects to a page a note has is not a new page, however often it is updated`() =
        runTest {
            links = "<a href=/docs/latest>Axiom</a>"
            redirects["https://x.com/docs/latest"] = "https://x.com/docs/axiom"

            note(
                path = "Web sources/Docs.md",
                source = "https://x.com/docs",
                original = "Intro, rewritten this week.",
                above = "## Pages\n\n- [[Docs/Axiom|Axiom]]\n\n"
            )

            note(
                path = "Web sources/Docs/Axiom.md",
                source = "https://x.com/docs/axiom",
                original = "Send traces to Axiom."
            )

            val runs = (1..2).map {
                update(
                    vaultPath = "/vault",
                    target = RelativePath(value = "Web sources"),
                    today = "2026-11-13"
                ).toList().last()
            }

            assertEquals(
                expected = List(size = 2) { UpdateStep.Done(unchanged = 2) },
                actual = runs
            )
        }

    @Test
    fun `a note with Windows line ends is the same note, a heading of the person's is not the article's, a wall is no update`() {
        val windows =
            "---\r\nsource: \"https://y.com/a\"\r\nclipped: 2026-10-01\r\n---\r\n\r\n# A\r\n\r\n" +
                "### Original text\r\n\r\nMine.\r\n\r\n## Original text\r\n\r\nLine one.\r\n\r\nLine two.\r\n"

        val rewritten = WebSourceNote.updated(
            text = windows,
            article = "New one.\n\nNew two.",
            today = "2026-11-13"
        )

        val long = "word ".repeat(n = 200)

        assertEquals(
            expected = listOf<Any>(true, true, true, false, true),
            actual = listOf(
                WebSourceNote.isSame(
                    text = windows,
                    article = "Line one.\n\nLine two."
                ),
                "### Original text\r\n\r\nMine.\r\n\r\n## Original text\r\n\r\nNew one.\r\n\r\nNew two.\r\n" in rewritten,
                "clipped: 2026-11-13\r\n" in rewritten,
                "\n" in rewritten.replace("\r\n", ""),
                WebSourceNote.looksGone(
                    text = "## Original text\n\n$long",
                    article = "Accept our cookies to go on."
                )
            )
        )
    }

    @Test
    fun `new pages join the list above the original text, never a list inside the article`() {
        val index =
            "# Docs\n\n## Pages\n\n- [[Docs/Axiom|Axiom]]\n\n## Original text\n\n- [[not a page]]\n"

        assertEquals(
            expected = "# Docs\n\n## Pages\n\n- [[Docs/Axiom|Axiom]]\n- [[Docs/Datadog|Datadog]]\n\n" +
                "## Original text\n\n- [[not a page]]\n",
            actual = WebSourceNote.withPages(
                text = index,
                links = listOf("Docs/Datadog|Datadog")
            )
        )
    }

    private companion object {
        const val ORIGINAL = "## Original text"
    }
}
