package app.notekea.chat.domain.tool.web

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolOrigin
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.web.domain.WebResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A web search as a tool of the loop (`docs/agent-web.md`, #144). */
class WebSearchToolTest {
    private val sent = mutableListOf<Pair<String, String?>>()

    private val tool = WebSearchTool { query, country ->
        sent += query to country

        Outcome.Success(
            value = listOf(
                WebResult(
                    title = "Tomato under glass",
                    url = "https://www.fao.org/tomato",
                    snippet = "Water at the base."
                )
            )
        )
    }

    // #144: the query sent is the one shown, with the turn's country and nothing else; what comes back is untrusted.
    @Test
    fun `the query shown is the query sent, in the turn's country, and the pages come back as untrusted text`() =
        runTest {
            val result = tool.run(
                call = ToolCall(
                    name = WebSearchTool.NAME,
                    arguments = mapOf(WebSearchTool.QUERY to "tomato greenhouse watering")
                ),
                context = ToolContext(
                    vaultPath = VAULT,
                    notes = emptyList(),
                    task = "look up how to water tomatoes in a greenhouse",
                    openNote = null,
                    country = "CO"
                )
            )

            assertEquals(
                expected = listOf(
                    listOf("tomato greenhouse watering" to "CO"),
                    ToolOrigin.UNTRUSTED,
                    true
                ),
                actual = listOf(
                    sent,
                    result.origin,
                    "fao.org" in result.forModel
                )
            )
        }

    // The web's evaluation, 2026-11-16: "buy soil moisture sensor" in Costa Rica's region brought shops in the
    // United States. A search for where to buy names the country; a fact, or a query that names one, is sent as is.
    @Test
    fun `a search for where to buy names the turn's country, and other searches are sent as written`() =
        runTest {
            val shown = listOf(
                "buy soil moisture sensor",
                "boiling point of water at 1200 m",
                "comprar bicicleta eléctrica en Colombia"
            ).map { query ->
                val call = ToolCall(
                    name = WebSearchTool.NAME,
                    arguments = mapOf(WebSearchTool.QUERY to query)
                )

                val context = ToolContext(
                    vaultPath = VAULT,
                    notes = emptyList(),
                    task = query,
                    openNote = null,
                    country = "CR"
                )

                tool.run(call, context)
                tool.targetOf(call, context)
            }

            assertEquals(
                expected = listOf(
                    "buy soil moisture sensor Costa Rica",
                    "boiling point of water at 1200 m",
                    "comprar bicicleta eléctrica en Colombia"
                ),
                actual = sent.map { it.first }
            )

            assertEquals(
                expected = sent.map { it.first },
                actual = shown
            )
        }
}
