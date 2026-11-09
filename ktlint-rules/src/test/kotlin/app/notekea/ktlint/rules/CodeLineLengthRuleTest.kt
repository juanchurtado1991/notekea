package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class CodeLineLengthRuleTest {
    private val assertThatCode = assertThatRule { CodeLineLengthRule() }

    @Test
    fun `a line of code over the limit is a violation, reported where it passes it`() {
        val code = "val total = " + (1..12).joinToString(separator = " + ") { "number$it" }

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to LIMIT)
            .hasLintViolationWithoutAutoCorrect(
                line = 1,
                col = LIMIT + 1,
                detail = CodeLineLengthRule.errorMessage(limit = LIMIT)
            )
    }

    @Test
    fun `a string, a test's name and an import may pass the limit`() {
        val long = "a".repeat(n = LIMIT)
        val short = "a".repeat(n = LIMIT - 7)

        val code = """
            import sample.$long

            val text = "$long"

            fun `$long`() = Unit

            fun `$short`() =
                Unit

            val s = "${short}aa" +
                "b"
        """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to LIMIT)
            .hasNoLintViolations()
    }

    @Test
    fun `a comment has its own, longer limit`() {
        val fits = "// " + "a".repeat(n = CodeLineLengthRule.MAX_COMMENT_LENGTH - 3)
        val kdoc = "/**\n * " + "a".repeat(n = LIMIT) + "\n */"
        val code = "$fits\n${fits}a\n$kdoc\nval one = 1"

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to LIMIT)
            .hasLintViolationWithoutAutoCorrect(
                line = 2,
                col = CodeLineLengthRule.MAX_COMMENT_LENGTH + 1,
                detail = CodeLineLengthRule.errorMessage(limit = CodeLineLengthRule.MAX_COMMENT_LENGTH)
            )
    }

    companion object {
        private const val LIMIT = 100
    }
}
