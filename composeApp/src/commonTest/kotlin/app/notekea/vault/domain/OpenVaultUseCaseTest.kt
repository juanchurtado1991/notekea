package app.notekea.vault.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenVaultUseCaseTest {
    private val fixture = VaultFixture()

    @Test
    fun `an available folder opens with its note count and becomes the most recent vault`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.WORK))

        val outcome = fixture.openVault(path = VaultFixture.NOTES)

        assertEquals(
            expected = Outcome.Success(
                value = Vault.fromPath(path = VaultFixture.NOTES)
            ),
            actual = outcome
        )
        assertEquals(
            expected = listOf(VaultFixture.NOTES, VaultFixture.WORK),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `reopening a vault moves it to the top without duplicating it`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults =
            listOf(fixture.recent(path = VaultFixture.WORK), fixture.recent(path = VaultFixture.NOTES))

        fixture.openVault(path = VaultFixture.NOTES)

        assertEquals(
            expected = listOf(VaultFixture.NOTES, VaultFixture.WORK),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `the recent list keeps only the most recent vaults`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults =
            (1..OpenVaultUseCase.MAX_RECENT_VAULTS).map { fixture.recent(path = OLD_PREFIX + it) }

        fixture.openVault(path = VaultFixture.NOTES)

        assertEquals(
            expected = OpenVaultUseCase.MAX_RECENT_VAULTS,
            actual = fixture.store.recentVaults.size
        )
        assertEquals(
            expected = VaultFixture.NOTES,
            actual = fixture.store.recentVaults
                .first()
                .path
        )
    }

    @Test
    fun `a missing folder fails and is not remembered`() = runTest {
        val outcome = fixture.openVault(path = VaultFixture.ARCHIVE)

        assertEquals(
            expected = Outcome.Failure(error = VaultError.FolderMissing(path = VaultFixture.ARCHIVE)),
            actual = outcome
        )
        assertEquals(
            expected = emptyList(),
            actual = fixture.store.recentVaults
        )
    }

    @Test
    fun `an unreadable folder fails with a permission error`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES, access = FolderAccess.NOT_READABLE)

        assertEquals(
            expected = Outcome.Failure(error = VaultError.PermissionDenied(path = VaultFixture.NOTES)),
            actual = fixture.openVault(path = VaultFixture.NOTES)
        )
    }

    companion object {
        private const val OLD_PREFIX = "/old/"
    }
}
