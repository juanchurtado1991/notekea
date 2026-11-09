package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class LambdaBodyLineRuleTest {
    private val assertThatCode = assertThatRule { LambdaBodyLineRule() }

    @Test
    fun `lambda of one call or assignment that fits goes on one line, with its parameter`() {
        val code =
            """
            fun load() {
                reduce {
                    it.copy(tree = intent.tree)
                }
                val names = rows.map { row ->
                    row.name.trim()
                }
                val label = Label(
                    onClick = {
                        isOpen = false
                    }
                )
            }
            """.trimIndent()
        val formattedCode =
            """
            fun load() {
                reduce { it.copy(tree = intent.tree) }
                val names = rows.map { row -> row.name.trim() }
                val label = Label(
                    onClick = { isOpen = false }
                )
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `lambda of one call that fits stays on its line, one that does not fit goes on several`() {
        val code =
            """
            val label = Label(onClick = { onReveal(path) })
            val other = Label(onClick = { onRevealTheFolderOfTheNoteInTheTree(path) })
            """.trimIndent()
        val formattedCode =
            """
            val label = Label(onClick = { onReveal(path) })
            val other = Label(onClick = {
                onRevealTheFolderOfTheNoteInTheTree(path)
            })
            """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to 60)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `lambda that compares, decides or holds two statements goes on several lines`() {
        val code =
            """
            val reason = SkipReason.entries.find { it.name == row.skipReason }
            val shown = notes.map { if (it.isOpen) it.name else null }
            val saved = notes.onEach { save(it); close(it) }
            """.trimIndent()
        val formattedCode =
            """
            val reason = SkipReason.entries.find {
                it.name == row.skipReason
            }
            val shown = notes.map {
                if (it.isOpen) it.name else null
            }
            val saved = notes.onEach {
                save(it); close(it)
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `lambda whose call another rule breaks into lines stays on several lines`() {
        val code =
            """
            val stale = sources.filter { (path, source) ->
                sourceOf(path = path) != source
            }
            val copies = notes.map {
                it.copy(name = name, path = path)
            }
            val names = notes.map {
                it.file.path.name.trim()
            }
            val rows = notes.map {
                row(it) { it.name }
            }
            val commented = notes.map {
                // The name as typed.
                it.trim()
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `lambda inside a condition or a string stays as it is`() {
        val code =
            """
            fun label(): String = if (notes.any { it.name == name }) "found" else "${'$'}{notes.count { it.isOpen }} open"
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `lambda of a name, a constant or a property path stays on the line`() {
        val code =
            """
            val paths = notes.map { it.file.path }
            val same = notes.map { it }
            val names = rows.map { row -> row.name }
            val none = notes.sumOf { 0 }
            val empty = notes.joinToString { "" }
            val nothing = {}
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
