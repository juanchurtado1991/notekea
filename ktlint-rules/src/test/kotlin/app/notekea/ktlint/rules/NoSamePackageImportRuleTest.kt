package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NoSamePackageImportRuleTest {
    private val assertThatCode = assertThatRule { NoSamePackageImportRule() }

    @Test
    fun `alias of a declaration of the same package is kept`() {
        val code =
            """
            package app.notekea.notes

            import app.notekea.notes.Note as Entry
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `import of a declaration of the same package is removed`() {
        val code =
            """
            package app.notekea.notes

            import app.notekea.core.Path
            import app.notekea.notes.Note
            import kotlin.time.Instant

            val note = Note()
            """.trimIndent()
        val formattedCode =
            """
            package app.notekea.notes

            import app.notekea.core.Path
            import kotlin.time.Instant

            val note = Note()
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `import of a nested declaration or of a subpackage is kept`() {
        val code =
            """
            package app.notekea.notes

            import app.notekea.notes.Note.Companion.EMPTY
            import app.notekea.notes.domain.NoteFile
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `import of another package is kept`() {
        val code =
            """
            package app.notekea.notes

            import app.notekea.core.Path
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }
}
