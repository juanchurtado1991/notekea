package app.notekea.chat.domain

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A page pasted in the chat to clip (the developer's request of 2026-11-11): when it links pages under its path and the
 * request says neither "only" nor "with its links", the chat asks; each answer clips what it says.
 */
class ClipSectionChatTest {
    private val clipper = object : WebPageClipper {
        override suspend fun clip(url: String) = Outcome.Success(value = draft(name = "Overview"))

        override suspend fun sectionOf(
            url: String,
            language: String?
        ) = Outcome.Success(
            value = SectionAsked(
                url = url,
                prefix = "https://x.com/docs/",
                count = 2,
                more = 0
            )
        )

        override suspend fun clipSection(url: String) = Outcome.Success(
            value = listOf(draft(name = "Overview"), draft(name = "Overview/Axiom"), draft(name = "Overview/Datadog"))
        )
    }

    private val compose = ComposeNotesUseCase(
        llmEngine = FakeLlmEngine(),
        noteTextCache = NoteTextCache(
            contentStore = FakeNoteContentStore(),
            textFolder = TextFolder()
        ),
        planReader = GhostActionPlanReader(),
        clipper = clipper
    )

    private suspend fun clipped(request: String) = compose(
        step = ActionStep(
            kind = ActionKind.CLIP_URL,
            url = URL
        ),
        request = request,
        vaultPath = "/vault",
        notes = emptyList()
    )

    @Test
    fun `a page with links under its path is asked about, and each answer clips what it says`() =
        runTest {
            val asked = clipped(request = "clip $URL") as Outcome.Failure
            val only = clipped(request = "clip only $URL") as Outcome.Success
            val whole = clipped(request = "clip $URL with its linked pages") as Outcome.Success
            val spanish = clipped(request = "recorta $URL con sus enlaces") as Outcome.Success

            assertEquals(
                expected = listOf<Any?>(
                    ActionProblem.SectionAsked(
                        SectionAsked(
                            url = URL,
                            prefix = "https://x.com/docs/",
                            count = 2,
                            more = 0
                        )
                    ),
                    1,
                    3,
                    3
                ),
                actual = listOf(
                    asked.error,
                    only.value.content.size,
                    whole.value.content.size,
                    spanish.value.content.size
                )
            )
        }

    private fun draft(name: String) = NoteDraft(
        path = RelativePath(value = "Web sources/$name.md"),
        text = name
    )

    private companion object {
        const val URL = "https://x.com/docs/overview"
    }
}
