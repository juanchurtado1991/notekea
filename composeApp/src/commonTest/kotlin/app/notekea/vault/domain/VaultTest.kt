package app.notekea.vault.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class VaultTest {
    @Test
    fun `the name is the last segment of unix and windows paths`() {
        val names = mapOf(
            "/home/ana/Notes" to "Notes",
            "/home/ana/Notes/" to "Notes",
            "C:\\Users\\ana\\Work Notes" to "Work Notes",
            "C:\\Users\\ana\\Work Notes\\" to "Work Notes",
            "Notes" to "Notes"
        )

        names.forEach { (path, name) ->
            assertEquals(
                expected = Vault(path = path, name = name),
                actual = Vault.fromPath(path = path)
            )
        }
    }

    @Test
    fun `a root folder is named after its full path`() {
        assertEquals(
            expected = ROOT,
            actual = Vault.fromPath(path = ROOT).name
        )
    }

    companion object {
        private const val ROOT = "/"
    }
}
