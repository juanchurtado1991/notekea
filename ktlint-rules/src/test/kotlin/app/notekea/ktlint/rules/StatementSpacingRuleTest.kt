package app.notekea.ktlint.rules

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class StatementSpacingRuleTest {
    private val assertThatCode = assertThatRule { StatementSpacingRule() }

    @Test
    fun `blank line goes around a statement of several lines`() {
        val code =
            """
            fun plan() {
                val held = load()
                val read = notes.filter {
                    it.isOpen
                }
                val present = names()
                val other = paths()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun plan() {
                val held = load()

                val read = notes.filter {
                    it.isOpen
                }

                val present = names()
                val other = paths()
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `blank line goes before a return`() {
        val code =
            """
            fun plan(): Int {
                val held = load()
                return held.size
            }
            """.trimIndent()
        val formattedCode =
            """
            fun plan(): Int {
                val held = load()

                return held.size
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `blank line goes before the comment written above the statement`() {
        val code =
            """
            fun plan(): Int {
                val held = load() // every note
                // Only the open ones
                return held.size
            }
            """.trimIndent()
        val formattedCode =
            """
            fun plan(): Int {
                val held = load() // every note

                // Only the open ones
                return held.size
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }

    @Test
    fun `lone return and statements of one line need no blank line`() {
        val code =
            """
            fun size(): Int {
                return 1
            }

            fun plan() {
                val held = load()
                val present = names()
                save()
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `separated statements have no violations`() {
        val code =
            """
            fun plan(): Int {
                val held = load()

                // Only the open ones
                val read = notes.filter {
                    it.isOpen
                }

                return held.size
            }
            """.trimIndent()

        assertThatCode(code).hasNoLintViolations()
    }

    @Test
    fun `statements of a lambda are separated too`() {
        val code =
            """
            val plan = run {
                val read = notes.filter {
                    it.isOpen
                }
                read.size
            }
            """.trimIndent()
        val formattedCode =
            """
            val plan = run {
                val read = notes.filter {
                    it.isOpen
                }

                read.size
            }
            """.trimIndent()

        assertThatCode(code).isFormattedAs(formattedCode)
    }
}
