package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.MaxFileLengthRule.Companion.MAX_FILE_LINES
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MaxFileLengthRuleTest {
    private val assertThatCode = assertThatRule { MaxFileLengthRule() }

    @Test
    fun `a file at the limit has no violations`() {
        assertThatCode(fileOf(lines = MAX_FILE_LINES)).hasNoLintViolations()
    }

    @Test
    fun `a file over the limit is a violation, reported at its start`() {
        assertThatCode(fileOf(lines = MAX_FILE_LINES + 1)).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = "File has ${MAX_FILE_LINES + 1} lines, the maximum is $MAX_FILE_LINES. Split it by sub-feature"
        )
    }

    /** A file of [lines] lines: a package line, then one constant per line. */
    private fun fileOf(
        lines: Int
    ): String = (listOf("package sample") + (2..lines).map { "val v$it = $it" }).joinToString(separator = "\n")
}
