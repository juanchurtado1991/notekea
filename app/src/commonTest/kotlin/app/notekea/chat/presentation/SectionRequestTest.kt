package app.notekea.chat.presentation

import app.notekea.chat.domain.plan.ClipWords
import app.notekea.chat.domain.plan.SectionAsked
import app.notekea.chat.presentation.viewmodel.SectionRequest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The four ways to clip a page's links (the developer's request of 2026-11-13), as the requests the chat acts on. */
class SectionRequestTest {
    private val section = SectionAsked(
        url = URL,
        prefix = URL,
        count = 23,
        more = 0
    )

    private fun requestOf(
        asked: SectionAsked,
        isWhole: Boolean,
        depth: Int
    ): String = SectionRequest.of(
        section = asked,
        isWhole = isWhole,
        isEvery = false,
        depth = depth
    )

    @Test
    fun `a deep choice and the whole documentation are counted first, and clipped once confirmed`() {
        val counted = section.copy(
            depth = ClipWords.FULL_DEPTH,
            isCounted = true
        )

        val requests = listOf(
            requestOf(
                asked = section,
                isWhole = false,
                depth = 1
            ),
            requestOf(
                asked = section,
                isWhole = true,
                depth = 1
            ),
            requestOf(
                asked = section,
                isWhole = true,
                depth = 2
            ),
            requestOf(
                asked = section,
                isWhole = true,
                depth = ClipWords.FULL_DEPTH
            ),
            requestOf(
                asked = counted,
                isWhole = true,
                depth = ClipWords.FULL_DEPTH
            )
        )

        assertEquals(
            expected = listOf(
                "clip only $URL",
                "clip $URL with its linked pages",
                "clip $URL with its linked pages, 2 levels deep, count first",
                "clip $URL with its linked pages, the whole documentation, count first",
                "clip $URL with its linked pages, the whole documentation"
            ),
            actual = requests
        )
    }

    @Test
    fun `the requests are read back as what they ask, how deep, whether whole and whether to count first`() {
        val full = "clip $URL with its linked pages, the whole documentation, count first"
        val deep = "clip $URL with its linked pages, 2 levels deep"

        assertEquals(
            expected = listOf<Any>(ClipWords.FULL_DEPTH, true, true, 2, false, false, true),
            actual = listOf(
                ClipWords.depthOf(request = full),
                ClipWords.isFull(request = full),
                ClipWords.isCounted(request = full),
                ClipWords.depthOf(request = deep),
                ClipWords.isFull(request = deep),
                ClipWords.isCounted(request = deep),
                ClipWords.isFull(request = "recorta $URL, la documentación completa")
            )
        )
    }

    private companion object {
        const val URL = "https://x.com/docs/"
    }
}
