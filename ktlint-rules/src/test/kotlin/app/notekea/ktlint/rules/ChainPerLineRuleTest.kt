package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ChainPerLineRuleTest {
    private val assertThatCode = assertThatRule { ChainPerLineRule() }

    @Test
    fun `call with arguments and a trailing lambda of several lines goes under its receiver`() {
        val code =
            """
            val present = notes.mapTo(destination = HashSet()) {
                it.path == path
            }
            """.trimIndent()
        // The standard indent rule then moves the lambda's lines in.
        val formattedCode =
            """
            val present = notes
                .mapTo(destination = HashSet()) {
                it.path == path
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `call with arguments and a trailing lambda on its line stays on the line`() {
        val code =
            """
            fun log(throwable: Throwable) {
                logger.e(throwable = throwable) { MESSAGE }
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `call with only a lambda or only arguments stays on the line`() {
        val code =
            """
            val held = indexed.associateBy { it.path }
            val rows = queries.selectStates(vault = vaultPath)
            val context = withContext(context = io) { load() }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `chain inside a condition or a string stays on the line`() {
        val code =
            """
            fun label(): String = if (state.value.tabs.isEmpty()) "none" else "${'$'}{state.value.tabs.first()} is open"
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `chain of three calls goes one segment per line`() {
        val code =
            """
            val names = notes.filter { it.isOpen }.map { it.name }.sorted()
            """.trimIndent()
        val formattedCode =
            """
            val names = notes
                .filter { it.isOpen }
                .map { it.name }
                .sorted()
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `chain that is already partly wrapped is wrapped whole`() {
        val code =
            """
            val names = notes.filter {
                it.isOpen
            }.map { it.name }
                .sorted()
            """.trimIndent()
        val formattedCode =
            """
            val names = notes
                .filter {
                it.isOpen
            }
                .map { it.name }
                .sorted()
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `chain of two segments stays on the line`() {
        val code =
            """
            val names = notes.filter { it.isOpen }.sortedBy { it.name }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `import and package names are not chains`() {
        val code =
            """
            package app.notekea.core.notes

            import app.notekea.core.notes.domain.NoteFile
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `path of properties without a call stays on the line`() {
        val code =
            """
            val first = state.value.tabs.size
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `properties of a chain that calls something go on their own lines`() {
        val code =
            """
            val first = held?.keys!!.sorted()[0].value
            """.trimIndent()
        val formattedCode =
            """
            val first = held
                ?.keys!!
                .sorted()[0]
                .value
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `qualified name is one receiver`() {
        val code =
            """
            val now = Clock.System.now().toEpochMilliseconds()
            val later = Clock.System.now().plus(duration = wait).toEpochMilliseconds()
            """.trimIndent()
        val formattedCode =
            """
            val now = Clock.System.now().toEpochMilliseconds()
            val later = Clock.System
                .now()
                .plus(duration = wait)
                .toEpochMilliseconds()
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }
}
