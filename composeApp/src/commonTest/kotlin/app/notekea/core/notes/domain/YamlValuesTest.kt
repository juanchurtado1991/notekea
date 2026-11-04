package app.notekea.core.notes.domain

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

    @Test
    fun `a comma inside quotes does not split a list, a quote in the middle of an item opens nothing`() {
        assertEquals(
            expected = listOf(
                listOf("Smith, John", "JS"),
                listOf("it's, ok", "b"),
                listOf("say \", hi", "b"),
                listOf("it's", "ok"),
                listOf("Smith, John", "JS")
            ),
            actual = listOf(
                YamlValues.inlineItems(text = "[\"Smith, John\", JS]"),
                YamlValues.inlineItems(text = "['it''s, ok', b]"),
                YamlValues.inlineItems(text = "[\"say \\\", hi\", b]"),
                YamlValues.inlineItems(text = "[it's, ok, ]"),
                YamlValues.scalarItems(text = "'Smith, John', JS")
            )
        )
    }

    @Test
    fun `the items of an inline list are found as written, quotes included`() {
        val block = FrontmatterBlocks.Block(lines = listOf("authors: [\"Smith, John\", JS]"), key = "authors")

        assertEquals(
            expected = mapOf("Smith, John" to "\"Smith, John\"", "JS" to "JS"),
            actual = YamlValues.writtenItems(block = block)
        )
    }
}
