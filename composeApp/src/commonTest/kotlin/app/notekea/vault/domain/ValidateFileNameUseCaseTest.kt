package app.notekea.vault.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateFileNameUseCaseTest {
    private val validateFileName = ValidateFileNameUseCase()

    @Test
    fun `names that cannot be a file or folder on every system are rejected`() {
        val invalid = listOf("", ".", "..", "a/b", "a\\b", "a:b", "a*b", "a?b", "a\"b", "a<b", "a>b", "a|b")

        assertEquals(
            expected = emptyList(),
            actual = invalid.filter { validateFileName(name = it) }
        )
    }

    @Test
    fun `ordinary names are accepted, including spaces, dots and accents`() {
        val valid = listOf("My Notes", "Café con leche", "2026-10-24", ".hidden", "v1.2", "a..b")

        assertEquals(
            expected = valid,
            actual = valid.filter { validateFileName(name = it) }
        )
    }
}
