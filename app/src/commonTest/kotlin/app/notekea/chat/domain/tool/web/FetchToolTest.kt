package app.notekea.chat.domain.tool.web

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A page read only from an address the request writes (`GUIDELINES.md` §13): one the model cut at its query (seen on
 * the 9B, 2026-11-16, a shop's `&pid=`) is read whole, and a piece of one cut anywhere else is still refused.
 */
class FetchToolTest {
    private val read = mutableListOf<String>()

    private val tool = FetchTool { url ->
        read += url
        Outcome.Success(value = "Drip kit, 119,577 colones")
    }

    private val written = "https://www.pacifiko.cr/compras/landtouch-drip-kit&pid=ZWIyYzM0Yz"

    private suspend fun fetched(url: String): String = tool
        .run(
            call = ToolCall(
                name = FetchTool.NAME,
                arguments = mapOf(FetchTool.URL to url)
            ),
            context = ToolContext(
                vaultPath = VAULT,
                notes = emptyList(),
                task = "where can I buy a drip irrigation kit?\n\n$written",
                openNote = null
            )
        ).forModel

    @Test
    fun `an address cut at its query is read whole`() = runTest {
        val result = fetched(url = "https://www.pacifiko.cr/compras/landtouch-drip-kit")

        assertEquals(
            expected = listOf(listOf(written), true),
            actual = listOf(read, "119,577" in result)
        )
    }

    @Test
    fun `a piece of an address cut elsewhere is still refused`() = runTest {
        val result = fetched(url = "https://www.pacifiko.cr/compras")

        assertEquals(
            expected = listOf(emptyList<String>(), true),
            actual = listOf(read, "refused" in result)
        )
    }
}
