package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A web page clipped: notes kept only where the article backs them, the article alone without a model. */
class ClipWebPageUseCaseTest {
    private val engine = FakeLlmEngine()
    private var fetched: Outcome<FetchedPage, ClipError> = Outcome.Success(
        value = FetchedPage(
            url = URL,
            html = "<p/>"
        )
    )

    private val clip = ClipWebPageUseCase(
        fetcher = { fetched },
        extractor = { Outcome.Success(value = ARTICLE) },
        llmEngine = engine,
        supportCheck = SupportCheck(llmEngine = engine)
    )

    private suspend fun last() = clip(url = URL).toList().last()

    @Test
    fun `a sentence the article does not back is dropped, with a heading left empty`() = runTest {
        engine.reply = {
            "Sleep sorts what was learned.\n\n## What it does\n- Sleep moves memories to the cortex.\n\n" +
                "## Advice\n- Coffee cures everything."
        }

        engine.scoreOf = { query, _ ->
            if ("Coffee" in query) -9f else 2f
        }

        val done = last() as ClipStep.Done

        assertEquals(
            expected = "Sleep sorts what was learned.\n\n## What it does\n- Sleep moves memories to the cortex.",
            actual = done.clip.notes
        )
    }

    @Test
    fun `without a model the article is clipped alone, and a page that cannot be reached writes nothing`() =
        runTest {
            engine.readiness = Outcome.Failure(error = LlmError.NoModel)
            val alone = last() as ClipStep.Done

            fetched = Outcome.Failure(error = ClipError.Offline)

            assertEquals(
                expected = listOf<Any?>("", ARTICLE, ClipStep.Failed(error = ClipError.Offline)),
                actual = listOf(alone.clip.notes, alone.clip.article, last())
            )
        }

    @Test
    fun `the note keeps the source, the date, the notes and the original text, under a safe name`() {
        val clip = WebClip(
            url = URL,
            title = "Sleep: why? / A guide",
            notes = "- Point.",
            article = ARTICLE
        )

        assertEquals(
            expected = "Web sources/Sleep why A guide.md" to
                "---\nsource: \"$URL\"\nclipped: 2026-11-11\n---\n\n# Sleep: why? / A guide\n\n- Point.\n\n" +
                "## Original text\n\nSleep sorts what was learned.\n\nIt moves memories to the cortex.\n",
            actual = WebClipNote.pathOf(title = clip.title).value to WebClipNote.textOf(
                clip = clip,
                today = "2026-11-11"
            )
        )
    }

    private companion object {
        const val URL = "https://example.com/sleep"
        val ARTICLE = Article(
            title = "Sleep",
            paragraphs = listOf("Sleep sorts what was learned.", "It moves memories to the cortex.")
        )
    }
}
