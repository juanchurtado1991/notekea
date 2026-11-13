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

    private val fetcher = PageFetcher { url ->
        if (url in articles) {
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = "<a href=/docs/axiom>Axiom</a> <a href=/docs/datadog>Datadog</a>"
                )
            )
        } else {
            Outcome.Failure(error = ClipError.Refused(status = 404))
        }
    }

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

    private companion object {
        const val ORIGINAL = "## Original text"
    }
}
