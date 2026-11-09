package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NamedArgumentsPerLineRuleTest {
    private val assertThatCode = assertThatRule { NamedArgumentsPerLineRule() }

    @Test
    fun `annotation stays on the line`() {
        val code =
            """
            @Suppress(names = "a", other = "b")
            val note = 1
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `call inside a condition or a string stays on the line`() {
        val code =
            """
            fun label(): String = if (isSame(indexed = old, current = new)) "same" else "${'$'}{name(first = a, last = b)}"
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `call with one named argument stays on the line`() {
        val code =
            """
            val note = guarded(fallback = Unit) { load() }
            val pair = Pair(first, second)
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `call with two named arguments goes one argument per line`() {
        val code =
            """
            fun delete() {
                queries.deleteState(vault = vaultPath, path = path.value)
            }
            """.trimIndent()
        val formattedCode =
            """
            fun delete() {
                queries.deleteState(
                    vault = vaultPath,
                    path = path.value
                )
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `positional argument of a call with two named ones goes on its own line too`() {
        val code =
            """
            val text = format(template, first = a, last = b) { it.trim() }
            """.trimIndent()
        val formattedCode =
            """
            val text = format(
                template,
                first = a,
                last = b
            ) { it.trim() }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `super type call with two named arguments goes one argument per line`() {
        val code =
            """
            class Rule : Base(id = ID, about = ABOUT)
            """.trimIndent()
        val formattedCode =
            """
            class Rule : Base(
                id = ID,
                about = ABOUT
            )
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }
}
