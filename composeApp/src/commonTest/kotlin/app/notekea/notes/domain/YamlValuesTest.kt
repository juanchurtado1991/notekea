package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class YamlValuesTest {
    @Test
    fun `a text ending in a colon is quoted, since YAML reads it as a key`() {
        assertEquals(expected = "\"Note:\"", actual = YamlValues.written(text = "Note:"))
    }

    @Test
    fun `double quotes undo their escapes, in one pass`() {
        assertEquals(
            expected = listOf("a\nb", "tab\there", "say \"hi\"", "C:\\new", "\u00E9", "\uD83D\uDE00", "\\q"),
            actual = listOf(
                "\"a\\nb\"",
                "\"tab\\there\"",
                "\"say \\\"hi\\\"\"",
                "\"C:\\\\new\"",
                "\"\\xE9\"",
                "\"\\U0001F600\"",
                "\"\\q\""
            ).map { YamlValues.unquoted(text = it) }
        )
    }

    @Test
    fun `single quotes hold no escapes`() {
        assertEquals(expected = "a\\nb's", actual = YamlValues.unquoted(text = "'a\\nb''s'"))
    }

    @Test
    fun `what is read from double quotes is written back the same`() {
        val written = listOf("\"a\\nb\"", "\"tab\\there\"", "\"C:\\\\new\"", "\"bell\\x07\"")

        assertEquals(
            expected = written,
            actual = written.map { YamlValues.written(text = YamlValues.unquoted(text = it), quote = '"') }
        )
    }

    @Test
    fun `a line break needs double quotes, even in a property that had single ones`() {
        assertEquals(
            expected = listOf("\"a\\nb\"", "\"a\\nb\""),
            actual = listOf(YamlValues.written(text = "a\nb"), YamlValues.written(text = "a\nb", quote = '\''))
        )
    }
}
