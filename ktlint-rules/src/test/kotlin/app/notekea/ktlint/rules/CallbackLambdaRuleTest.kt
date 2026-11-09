package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class CallbackLambdaRuleTest {
    private val assertThatCode = assertThatRule { CallbackLambdaRule() }

    @Test
    fun `a callback of several statements is a violation that is not corrected`() {
        val code =
            """
            val label = Label(
                onClick = {
                    save()
                    close()
                }
            )
            """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 2,
            col = 15,
            detail = CallbackLambdaRule.ERROR_MESSAGE
        )
    }

    @Test
    fun `a callback of one statement, and a lambda that is not a callback passed by name, have no violations`() {
        val code =
            """
            val field = Field(
                label = {
                    Icon(icon)
                    Text(text)
                },
                onClick = { close() },
                onClose = {
                    onIntent(
                        Close(
                            pane = pane,
                            tab = tab
                        )
                    )
                }
            )

            val names = notes.onEach {
                save(it)
                close(it)
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
