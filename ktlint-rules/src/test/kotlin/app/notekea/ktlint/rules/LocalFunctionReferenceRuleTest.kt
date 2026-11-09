package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class LocalFunctionReferenceRuleTest {
    private val assertThatCode = assertThatRule { LocalFunctionReferenceRule() }

    @Test
    fun `a reference to a function declared inside another one is a violation that is not corrected`() {
        val code =
            """
            fun Tab(onSelect: () -> Unit) {
                fun startDrag() {
                    onSelect()
                    drag()
                }

                Row(onStart = ::startDrag)
            }
            """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 7,
            col = 19,
            detail = LocalFunctionReferenceRule.ERROR_MESSAGE
        )
    }

    @Test
    fun `a reference to a function declared in an enclosing lambda is a violation`() {
        val code =
            """
            val item = Item(
                submenu = { onChosen ->
                    fun pick() {
                        onChosen()
                        format()
                    }

                    Picker(onPick = ::pick)
                }
            )
            """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 8,
            col = 25,
            detail = LocalFunctionReferenceRule.ERROR_MESSAGE
        )
    }

    @Test
    fun `a member, a top-level function, a bound reference and a call from a lambda have no violations`() {
        val code =
            """
            fun close() = Unit

            class Screen(private val viewModel: ViewModel) {
                fun save() = Unit

                fun show() {
                    fun leave() {
                        save()
                        close()
                    }

                    Row(
                        onSave = ::save,
                        onClose = ::close,
                        onIntent = viewModel::onIntent,
                        onCopy = DesktopActions::copy,
                        onLeave = { leave() }
                    )
                }
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
