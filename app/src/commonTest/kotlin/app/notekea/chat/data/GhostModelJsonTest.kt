package app.notekea.chat.data

import app.notekea.chat.domain.tool.ToolCall
import kotlin.test.Test
import kotlin.test.assertEquals

/** The loop's replies as the model writes them under a schema, and the texts a schema names. */
class GhostModelJsonTest {
    private val json = GhostModelJson()

    @Test
    fun `a call is read from the model's JSON, lists a line an item, escapes undone, and what is no call is none`() {
        assertEquals(
            expected = listOf(
                ToolCall(
                    name = "plan",
                    arguments = mapOf("steps" to "Read \"A\"\nWrite it", "n" to "3")
                ),
                ToolCall(
                    name = "answer",
                    arguments = mapOf("text" to "Línea uno\nlínea dos é")
                ),
                null,
                null
            ),
            actual = listOf(
                """{"tool": "plan", "steps": ["Read \"A\"", "Write it"], "n": 3}""",
                """{"tool":"answer","text":"Línea uno\nlínea dos é"}""",
                """{"text":"no tool named"}""",
                "not json"
            ).map { reply ->
                json
                    .fields(json = reply)
                    ?.let { ToolCall.of(fields = it) }
            }
        )
    }

    @Test
    fun `a value that is no text is kept as written, and a list's too`() {
        assertEquals(
            expected = mapOf("n" to "2.5", "done" to "true", "none" to "null", "mixed" to "a\n7\nfalse", "empty" to ""),
            actual = json.fields(
                json = """{"n": 2.5, "done": true, "none": null, "mixed": ["a", 7, false], "empty": []}"""
            )
        )
    }

    @Test
    fun `escapes are undone, a unicode one as well`() {
        assertEquals(
            expected = mapOf("text" to "a\tb \\ / é \"q\""),
            actual = json.fields(json = """{"text": "a\tb \\ \/ é \"q\""}""")
        )
    }

    @Test
    fun `what is written around the object is left out`() {
        assertEquals(
            expected = listOf(mapOf("pick" to "N1"), mapOf("pick" to "N1")),
            actual = listOf("Here it is:\n{\"pick\": \"N1\"}", "{\"pick\": \"N1\"}\n\nDone.").map {
                json.fields(json = it)
            }
        )
    }

    @Test
    fun `a reply cut short is no reply`() {
        assertEquals(
            expected = listOf(null, null, null, null),
            actual = listOf(
                """{"tool": "answer", "text": "cut in the mid""",
                """{"tool": "answer", "text": "whole" """,
                """{"tool": "plan", "steps": ["one", "tw""",
                """{"tool": """
            ).map { json.fields(json = it) }
        )
    }

    @Test
    fun `a text is quoted with its escapes`() {
        assertEquals(
            expected = listOf("\"plain\"", "\"Sam's \\\"best\\\" way\\\\\"", "\"two\\nlines\""),
            actual = listOf("plain", "Sam's \"best\" way\\", "two\nlines").map {
                json.quoted(text = it)
            }
        )
    }
}
