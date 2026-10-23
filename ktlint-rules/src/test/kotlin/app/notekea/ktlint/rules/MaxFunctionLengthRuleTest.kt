package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.MaxFunctionLengthRule.Companion.MAX_FUNCTION_BODY_LINES
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class MaxFunctionLengthRuleTest {
    private val assertThatCode = assertThatRule { MaxFunctionLengthRule() }

    @Test
    fun `block body at the limit has no violations`() {
        val code = blockBodyFunction(statementLines = MAX_FUNCTION_BODY_LINES - BLOCK_BRACE_LINES)

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `block body over the limit is a violation`() {
        val code = blockBodyFunction(statementLines = MAX_FUNCTION_BODY_LINES - BLOCK_BRACE_LINES + 1)

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = expectedMessage(bodyLines = MAX_FUNCTION_BODY_LINES + 1)
        )
    }

    @Test
    fun `expression body over the limit is a violation`() {
        val code = expressionBodyFunction(argumentLines = MAX_FUNCTION_BODY_LINES - EXPRESSION_BODY_EXTRA_LINES)

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = expectedMessage(bodyLines = MAX_FUNCTION_BODY_LINES + 1)
        )
    }

    @Test
    fun `long signature does not count towards the limit`() {
        val parameters = (1..MAX_FUNCTION_BODY_LINES).joinToString(separator = ",\n") { "    p$it: Int" }
        val code = "fun sum(\n$parameters\n): Int {\n    return 0\n}"

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `violation in a nested function is reported on that function only`() {
        val nested = blockBodyFunction(statementLines = MAX_FUNCTION_BODY_LINES)
            .lines()
            .joinToString(separator = "\n") { "    $it" }
        val code = "class Vault {\n$nested\n}"

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 2,
            col = 5,
            detail = expectedMessage(bodyLines = MAX_FUNCTION_BODY_LINES + BLOCK_BRACE_LINES)
        )
    }

    private fun blockBodyFunction(
        statementLines: Int
    ): String {
        val statements = (1..statementLines).joinToString(separator = "\n") { "    println($it)" }
        return "fun long() {\n$statements\n}"
    }

    private fun expectedMessage(
        bodyLines: Int
    ): String = "Function body has $bodyLines lines, the maximum is $MAX_FUNCTION_BODY_LINES. " +
        "Extract granular, self-descriptive functions"

    private fun expressionBodyFunction(
        argumentLines: Int
    ): String {
        val arguments = (1..argumentLines).joinToString(separator = ",\n") { "    $it" }
        return "fun long() = listOf(\n$arguments\n)"
    }

    companion object {
        private const val BLOCK_BRACE_LINES = 2
        private const val EXPRESSION_BODY_EXTRA_LINES = 1
    }
}
