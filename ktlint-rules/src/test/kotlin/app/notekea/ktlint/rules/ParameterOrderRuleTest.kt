package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

private const val ERROR_MESSAGE = "Parameters must be ordered: mandatory, then nullable, then with a default value"

class ParameterOrderRuleTest {
    private val assertThatCode = assertThatRule { ParameterOrderRule() }

    @Test
    fun `constructor with a default value before a mandatory parameter is a violation`() {
        val code =
            """
            class Note(
                val title: String = "",
                val path: String
            )
            """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 3,
            col = 5,
            detail = ERROR_MESSAGE
        )
    }

    @Test
    fun `default value before a mandatory parameter is a violation`() {
        val code =
            """
            fun open(
                readOnly: Boolean = false,
                path: String
            ) {}
            """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 3,
            col = 5,
            detail = ERROR_MESSAGE
        )
    }

    @Test
    fun `nullable parameter after a default value is a violation`() {
        val code =
            """
            fun open(
                path: String,
                readOnly: Boolean = false,
                tag: String?
            ) {}
            """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 4,
            col = 5,
            detail = ERROR_MESSAGE
        )
    }

    @Test
    fun `ordered parameters have no violations`() {
        val code =
            """
            fun open(
                path: String,
                tag: String?,
                readOnly: Boolean = false
            ) {}
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `override functions are skipped`() {
        val code =
            """
            class LocalVault : Vault {
                override fun open(
                    readOnly: Boolean = false,
                    path: String
                ) {}
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `trailing lambda after a default value is allowed`() {
        val code =
            """
            fun watch(
                path: String,
                recursive: Boolean = true,
                onChange: (String) -> Unit
            ) {}
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
