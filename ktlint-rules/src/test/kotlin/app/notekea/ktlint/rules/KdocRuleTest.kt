package app.notekea.ktlint.rules

import app.notekea.ktlint.rules.KdocRule.Companion.EMPTY_MESSAGE
import app.notekea.ktlint.rules.KdocRule.Companion.MAX_LINES
import app.notekea.ktlint.rules.KdocRule.Companion.NAME_MESSAGE
import app.notekea.ktlint.rules.KdocRule.Companion.PAST_MESSAGE
import app.notekea.ktlint.rules.KdocRule.Companion.TAG_MESSAGE
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class KdocRuleTest {
    private val assertThatCode = assertThatRule { KdocRule() }

    @Test
    fun `a KDoc at the limit has no violations`() {
        val code = """
            /**
             * Reads the notes of a vault in path order.
             *
             * A note that cannot be read is skipped,
             * and the scan goes on with the next one.
             */
            class NoteScanner
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `a KDoc over the limit is a violation`() {
        val code = """
            /**
             * Reads the notes of a vault in path order.
             * A note that cannot be read is skipped,
             * and the scan goes on with the next one,
             * until the last note.
             */
            class NoteScanner
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = KdocRule.longMessage(lines = MAX_LINES + 1)
        )
    }

    @Test
    fun `an empty KDoc is a violation`() {
        val code = """
            /**
             *
             */
            class NoteScanner
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = EMPTY_MESSAGE
        )
    }

    @Test
    fun `a KDoc that only repeats the name is a violation`() {
        val code = """
            class Notes {
                /** The note scanner. */
                val noteScanner = NoteScanner()
            }
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 2,
            col = 5,
            detail = NAME_MESSAGE
        )
    }

    @Test
    fun `a tag that only repeats the name is a violation`() {
        val code = """
            /**
             * Reads the notes of a vault in path order.
             * @param vaultPath the vault path
             */
            fun scan(vaultPath: String) = Unit
        """.trimIndent()

        assertThatCode(code).hasLintViolationWithoutAutoCorrect(
            line = 1,
            col = 1,
            detail = TAG_MESSAGE
        )
    }

    @Test
    fun `a tag that says more than the name has no violations`() {
        val code = """
            /**
             * Reads the notes of a vault in path order.
             * @param vaultPath absolute, as the folder picker gave it
             * @return the notes that could be read
             */
            fun scan(vaultPath: String) = emptyList<String>()
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `a KDoc that cites the roadmap, a smoke run or an issue is a violation`() {
        val code = """
            /** The quick switcher (`ROADMAP.md` 1.6). */
            class Switcher

            /** Keeps the cursor after the text (1.17 smoke: the cursor started before it). */
            class Cursor

            /** Shown at any scale (issue #101). */
            class Buttons
        """.trimIndent()

        assertThatCode(code).hasLintViolationsWithoutAutoCorrect(
            LintViolation(line = 1, col = 1, detail = PAST_MESSAGE),
            LintViolation(line = 4, col = 1, detail = PAST_MESSAGE),
            LintViolation(line = 7, col = 1, detail = PAST_MESSAGE)
        )
    }

    @Test
    fun `a KDoc that names a guide of the repository has no violations`() {
        val code = """
            /** The cases of 1.6, the quick switcher and search (`MVP_SMOKE_TEST.md`). */
            class SwitcherCases
        """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
