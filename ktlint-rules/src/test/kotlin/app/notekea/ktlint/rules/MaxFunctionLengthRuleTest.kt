package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.MaxFunctionLengthRule.Companion.MAX_FUNCTION_BODY_LINES
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MaxFunctionLengthRuleTest {
    private val assertThatCode = assertThatRule { MaxFunctionLengthRule() }

    @Test
    fun `block body at the limit has no violations`() {
        val code = blockBodyFunction(statementLines = MAX_FUNCTION_BODY_LINES)

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `block body over the limit is a violation`() {
        val code = blockBodyFunction(statementLines = MAX_FUNCTION_BODY_LINES + 1)

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = MaxFunctionLengthRule.errorMessage(bodyLines = MAX_FUNCTION_BODY_LINES + 1)
        )
    }

    @Test
    fun `expression body over the limit is a violation`() {
        val code = expressionBodyFunction(argumentLines = MAX_FUNCTION_BODY_LINES)

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = MaxFunctionLengthRule.errorMessage(bodyLines = MAX_FUNCTION_BODY_LINES + 1)
        )
    }

    @Test
    fun `blank lines and lines of brackets alone do not count`() {
        val statements = (1..MAX_FUNCTION_BODY_LINES).joinToString(separator = "\n\n") {
            "    (\n        println($it)\n    )"
        }

        assertThatCode("fun long() {\n$statements\n}").hasNoLintViolations()
    }

    @Test
    fun `long signature does not count towards the limit`() {
        val parameters = (1..MAX_FUNCTION_BODY_LINES + 1).joinToString(separator = ",\n") {
            "    p$it: Int"
        }
        val code = "fun sum(\n$parameters\n): Int {\n    return 0\n}"

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `violation in a nested function is reported on that function only`() {
        val nested = blockBodyFunction(statementLines = MAX_FUNCTION_BODY_LINES + 1)
            .lines()
            .joinToString(separator = "\n") { "    $it" }
        val code = "class Vault {\n$nested\n}"

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 2,
            col = 5,
            detail = MaxFunctionLengthRule.errorMessage(bodyLines = MAX_FUNCTION_BODY_LINES + 1)
        )
    }

    private fun blockBodyFunction(statementLines: Int): String {
        val statements = (1..statementLines).joinToString(separator = "\n") { "    println($it)" }

        return "fun long() {\n$statements\n}"
    }

    /** `= listOf(` is a line of code too, so the body has one line more than its arguments. */
    private fun expressionBodyFunction(argumentLines: Int): String {
        val arguments = (1..argumentLines).joinToString(separator = ",\n") { "    $it" }

        return "fun long() = listOf(\n$arguments\n)"
    }
}
