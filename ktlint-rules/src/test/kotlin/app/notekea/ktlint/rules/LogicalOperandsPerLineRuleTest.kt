package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class LogicalOperandsPerLineRuleTest {
    private val assertThatCode = assertThatRule { LogicalOperandsPerLineRule() }

    @Test
    fun `expression inside a string stays on the line`() {
        val code =
            """
            val text = "${'$'}{a && b && c}"
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `operand after a comment stays where it is`() {
        val code =
            """
            val isSame = first && // the first one
                second &&
                third
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `three operands go one per line`() {
        val code =
            """
            val isSame = current.size != null && current.date != null && current.size == indexed.size
            """.trimIndent()
        val formattedCode =
            """
            val isSame = current.size != null &&
                current.date != null &&
                current.size == indexed.size
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `three operands of different operators go one per line`() {
        val code =
            """
            fun check() {
                if (note == null || note.version != version && isChanged) {
                    read()
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun check() {
                if (note == null ||
                    note.version != version &&
                    isChanged) {
                    read()
                }
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `two operands stay on the line`() {
        val code =
            """
            val isSame = current.size != null && current.date != null
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `operands in parentheses count as one`() {
        val code =
            """
            val isSame = first && (second || third)
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
