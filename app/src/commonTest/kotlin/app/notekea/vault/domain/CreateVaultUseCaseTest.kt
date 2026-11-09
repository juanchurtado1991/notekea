package app.notekea.vault.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateVaultUseCaseTest {
    private val fixture = VaultFixture()

    @Test
    fun `a new vault is created with the trimmed name and opened`() = runTest {
        val outcome = fixture.createVault(parentPath = FakeVaultFileSystem.DEFAULT_PARENT, name = "  My Notes ")

        val expectedPath = FakeVaultFileSystem.DEFAULT_PARENT + FakeVaultFileSystem.SEPARATOR + NAME
        assertEquals(
            expected = Outcome.Success(value = Vault.fromPath(path = expectedPath)),
            actual = outcome
        )
        assertEquals(
            expected = listOf(expectedPath),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `names that cannot be a folder everywhere are rejected before touching the disk`() = runTest {
        listOf("   ", "a/b").forEach { name ->
            assertEquals(
                expected = Outcome.Failure(error = VaultError.InvalidName),
                actual = fixture.createVault(parentPath = FakeVaultFileSystem.DEFAULT_PARENT, name = name),
                message = name
            )
        }
        assertEquals(
            expected = emptyMap(),
            actual = fixture.fileSystem.folders
        )
    }

    @Test
    fun `a folder error is returned as is`() = runTest {
        val error = VaultError.FolderAlreadyExists(path = VaultFixture.NOTES)
        fixture.fileSystem.createFailure = error

        assertEquals(
            expected = Outcome.Failure(error = error),
            actual = fixture.createVault(parentPath = FakeVaultFileSystem.DEFAULT_PARENT, name = NAME)
        )
    }

    companion object {
        private const val NAME = "My Notes"
    }
}
