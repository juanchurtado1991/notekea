package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.NameCompoundExpressionRule.Companion.BRANCH_MESSAGE
import app.notekea.ktlint.rules.NameCompoundExpressionRule.Companion.CONDITION_MESSAGE
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class NameCompoundExpressionRuleTest {
    private val assertThatCode = assertThatRule { NameCompoundExpressionRule() }

    @Test
    fun `a condition of two calls is a violation`() {
        val code = """
            fun isInAngles(text: String): Boolean {
                if (text.startsWith(prefix = "<") && text.endsWith(suffix = ">")) {
                    return true
                }
                return false
            }
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 2,
            col = 9,
            detail = CONDITION_MESSAGE
        )
    }

    @Test
    fun `a condition of one call and a value has no violations`() {
        val code = """
            fun trimmed(text: String, isOn: Boolean): String {
                while (isOn && text.startsWith(prefix = "<")) {
                    return text
                }
                return if (isOn || text.length > 2) text else ""
            }
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `a long condition is a violation even when it calls nothing`() {
        val code = """
            fun change() {
                if (state.value.workspace != before && state.value.isRestored) saveTabs()
            }
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 2,
            col = 9,
            detail = CONDITION_MESSAGE
        )
    }

    @Test
    fun `a long condition that checks a type or null stays where the compiler needs it`() {
        val code = """
            fun change() {
                if (knownFolders != null && attachments[file.path] == file) return
                if (created is Outcome.Failure && created.error !is NotesError.NameTaken) return
            }
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `calls inside a lambda of the condition do not count`() {
        val code = """
            fun hasShort(words: List<String>, isOn: Boolean): Boolean {
                if (isOn && words.any { it.trim() == "" }) {
                    return true
                }
                return false
            }
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `a named condition has no violations`() {
        val code = """
            fun isInAngles(text: String): Boolean {
                val isInAngles = text.startsWith(prefix = "<") && text.endsWith(suffix = ">")
                if (isInAngles) {
                    return true
                }
                return false
            }
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `an if that calls something as a named argument is a violation`() {
        val code = """
            val label = Label(text = if (isOn) text.trim() else "Off")
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 26,
            detail = BRANCH_MESSAGE
        )
    }

    @Test
    fun `a when and a chain on a parenthesized if as named arguments are violations`() {
        val code = """
            val embed = Embed(
                kind = when (width) {
                    null -> Kind.TEXT
                    else -> Kind.IMAGE
                },
                label = (if (width != null) alt else text).ifBlank { null }
            )
        """.trimIndent()

        assertThatCode(code).hasLintViolationsWithoutAutoCorrect(
            LintViolation(line = 2, col = 12, detail = BRANCH_MESSAGE),
            LintViolation(line = 6, col = 13, detail = BRANCH_MESSAGE)
        )
    }

    @Test
    fun `a one-line if between values has no violations`() {
        val code = """
            val border = Border(
                color = if (isSelected && isFocused) colors.primary else colors.outline,
                width = if (isSelected) 2.dp else 1.dp
            )
            val shown = show(if (isOn) text.trim() else "Off")
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `chains as named arguments have no violations`() {
        val code = """
            val names = Names(sorted = notes.map { it.name }.sortedBy { it.length }, first = notes.first().name)
            val box = Box(modifier = Modifier.fillMaxWidth().padding(all = 8.dp))
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
