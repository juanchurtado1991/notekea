package app.notekea.chat.domain

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.ClipWords
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.PageProgress
import app.notekea.chat.domain.plan.SectionAsked
import app.notekea.chat.domain.plan.WebPageClipper
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
    private val depths = mutableListOf<Int>()
    private var progressShown: () -> Unit = {}

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

        override suspend fun clipSection(
            url: String,
            isEvery: Boolean,
            depth: Int,
            onProgress: suspend (PageProgress) -> Unit
        ): Outcome<List<NoteDraft>, PageProblem> {
            depths += depth
            onProgress(PageProgress(found = FOUND))
            progressShown()

            onProgress(
                PageProgress(
                    done = 1,
                    total = FOUND,
                    url = "https://x.com/docs/axiom"
                )
            )

            return clips()
        }

        private fun clips() = Outcome.Success(
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

    @Test
    fun `all of them is asked in English or Spanish, the closest thirty otherwise`() {
        assertEquals(
            expected = listOf(true, true, false, false),
            actual = listOf(
                ClipWords.isEvery(request = "clip $URL with all its linked pages"),
                ClipWords.isEvery(request = "recorta $URL con todas sus páginas enlazadas"),
                ClipWords.isEvery(request = "clip $URL with its linked pages"),
                ClipWords.isEvery(request = "clip all of $URL")
            )
        )
    }

    private fun draft(name: String) = NoteDraft(
        path = RelativePath(value = "Web sources/$name.md"),
        text = name
    )

    // The developer's request of 2026-11-13: the Android docs gave only the pages their first page links.
    @Test
    fun `how deep the links are followed is said in levels, in English or Spanish, and told to the clipper`() =
        runTest {
            val progress = mutableListOf<PageProgress>()

            compose(
                step = ActionStep(
                    kind = ActionKind.CLIP_URL,
                    url = URL
                ),
                request = "clip $URL with its linked pages, 2 levels deep",
                vaultPath = "/vault",
                notes = emptyList(),
                onProgress = { progress += it }
            )

            clipped(request = "recorta $URL a 3 niveles")
            clipped(request = "clip $URL with its linked pages")
            clipped(request = "clip $URL 9 levels deep")

            assertEquals(
                expected = listOf<Any?>(listOf(2, 3, 1, 3), FOUND, 1 to FOUND),
                actual = listOf(
                    depths,
                    progress.first().found,
                    progress.last().done to progress.last().total
                )
            )
        }

    private companion object {
        const val URL = "https://x.com/docs/overview"
        const val FOUND = 12
    }
}
