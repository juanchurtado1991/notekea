package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ExpressionOnAssignmentLineRuleTest {
    private val assertThatCode = assertThatRule { ExpressionOnAssignmentLineRule() }

    @Test
    fun `comment after the assignment is left untouched`() {
        val code =
            """
            val answer =
                // The answer to everything
                42
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `expression already on the assignment line has no violations`() {
        val code =
            """
            fun answer(): Int = 42
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `expression that does not fit stays on the next line`() {
        val code =
            """
            fun answer(): Int =
                1234567890
            """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to SHORT_MAX_LINE_LENGTH)
            .hasNoLintViolations()
    }

    @Test
    fun `function expression body is joined to the assignment line`() {
        val code =
            """
            fun answer(): Int =
                42
            """.trimIndent()
        val formattedCode =
            """
            fun answer(): Int = 42
            """.trimIndent()

        assertThatCode(code)
            .hasLintViolation(
                line = 1,
                col = 19,
                detail = ERROR_MESSAGE
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `getter expression body is joined to the assignment line`() {
        val code =
            """
            val size: Int
                get() =
                    42
            """.trimIndent()
        val formattedCode =
            """
            val size: Int
                get() = 42
            """.trimIndent()

        assertThatCode(code)
            .hasLintViolation(
                line = 2,
                col = 11,
                detail = ERROR_MESSAGE
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `multiline raw string stays on its own line`() {
        val code =
            "val text =\n" +
                "    \"\"\"\n" +
                "    first line\n" +
                "    second line\n" +
                "    \"\"\".trimIndent()"

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `property initializer is joined to the assignment line`() {
        val code =
            """
            val name: String =
                "notekea"
            """.trimIndent()
        val formattedCode =
            """
            val name: String = "notekea"
            """.trimIndent()

        assertThatCode(code)
            .hasLintViolation(
                line = 1,
                col = 18,
                detail = ERROR_MESSAGE
            ).isFormattedAs(formattedCode)
    }

    companion object {
        private const val ERROR_MESSAGE = "Expression should start on the same line as '='"
        private const val SHORT_MAX_LINE_LENGTH = 20
    }
}
