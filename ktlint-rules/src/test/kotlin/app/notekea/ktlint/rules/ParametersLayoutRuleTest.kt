package app.notekea.ktlint.rules

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ParametersLayoutRuleTest {
    private val assertThatCode = assertThatRule { ParametersLayoutRule() }

    @Test
    fun `lambda and function type parameters are left alone`() {
        val code =
            """
            val join: (first: String, second: String, third: String) -> String = { first, second, third -> first }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `list with a comment is left alone`() {
        val code =
            """
            fun load(
                // The vault's folder
                path: String
            ) = Unit
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `single parameter joins the line when the signature fits`() {
        val code =
            """
            class Store(
                private val queries: Queries
            ) : StateStore {
                override fun load(
                    path: String
                ): List<Note> = queries.select(path = path)
            }
            """.trimIndent()
        val formattedCode =
            """
            class Store(private val queries: Queries) : StateStore {
                override fun load(path: String): List<Note> = queries.select(path = path)
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `single parameter stays on its own line when the signature does not fit`() {
        val code =
            """
            fun loadEveryNote(
                path: String
            ): List<Note> = emptyList()
            """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to SHORT_MAX_LINE_LENGTH)
            .hasNoLintViolations()
    }

    @Test
    fun `single parameter whose signature does not fit goes on its own line`() {
        val code =
            """
            fun loadEveryNote(path: String): List<Note> = emptyList()
            """.trimIndent()
        val formattedCode =
            """
            fun loadEveryNote(
                path: String
            ): List<Note> = emptyList()
            """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to SHORT_MAX_LINE_LENGTH)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `signature is measured up to the assignment of an expression body`() {
        val code =
            """
            fun load(
                path: String
            ): List<Note> = queries.select(path = path).executeAsList()
            """.trimIndent()
        val formattedCode =
            """
            fun load(path: String): List<Note> = queries.select(path = path).executeAsList()
            """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to SHORT_MAX_LINE_LENGTH)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `three parameters go one per line`() {
        val code =
            """
            fun plan(notes: List<Note>, indexed: List<Note>, version: String) = Unit
            """.trimIndent()
        val formattedCode =
            """
            fun plan(
                notes: List<Note>,
                indexed: List<Note>,
                version: String
            ) = Unit
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `two parameters go either way`() {
        val code =
            """
            data class Kept(val note: String, val copy: String?) : Notice

            fun isSame(
                indexed: Note,
                current: Note
            ): Boolean = indexed == current
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `two parameters on different lines without one per line are spread`() {
        val code =
            """
            fun isSame(indexed: Note,
                current: Note): Boolean = indexed == current
            """.trimIndent()
        val formattedCode =
            """
            fun isSame(
                indexed: Note,
                current: Note
            ): Boolean = indexed == current
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `two parameters that do not fit go one per line`() {
        val code =
            """
            fun isSameNote(indexed: Note, current: Note): Boolean = true
            """.trimIndent()
        val formattedCode =
            """
            fun isSameNote(
                indexed: Note,
                current: Note
            ): Boolean = true
            """.trimIndent()

        assertThatCode(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to SHORT_MAX_LINE_LENGTH)
            .isFormattedAs(formattedCode)
    }

    companion object {
        private const val SHORT_MAX_LINE_LENGTH = 40
    }
}
