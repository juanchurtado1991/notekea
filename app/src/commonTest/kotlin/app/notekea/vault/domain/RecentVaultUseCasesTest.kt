package app.notekea.vault.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.ElapsedTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days

/** Covers the use cases that read or edit the recent list: get, remove, locate, restore, pick and suggest. */
class RecentVaultUseCasesTest {
    private val fixture = VaultFixture()

    @Test
    fun `recent vaults are described and missing folders are flagged`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults = listOf(
            RecentVault(path = VaultFixture.NOTES, lastOpenedAt = fixture.clock.currentTime - 2.days),
            fixture.recent(path = VaultFixture.ARCHIVE)
        )

        assertEquals(
            expected = listOf(
                RecentVaultStatus(
                    vault = Vault.fromPath(path = VaultFixture.NOTES),
                    lastOpened = ElapsedTime.Days(count = 2),
                    isMissing = false
                ),
                RecentVaultStatus(
                    vault = Vault.fromPath(path = VaultFixture.ARCHIVE),
                    lastOpened = ElapsedTime.JustNow,
                    isMissing = true
                )
            ),
            actual = fixture.getRecentVaults()
        )
    }

    @Test
    fun `removing a recent vault keeps the others in order`() = runTest {
        fixture.store.recentVaults = listOf(VaultFixture.NOTES, VaultFixture.ARCHIVE, VaultFixture.WORK).map {
            fixture.recent(path = it)
        }

        fixture.removeRecentVault(path = VaultFixture.ARCHIVE)

        assertEquals(
            expected = listOf(VaultFixture.NOTES, VaultFixture.WORK),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `locating a missing vault opens the new folder and forgets the old one`() = runTest {
        fixture.addFolder(path = VaultFixture.WORK)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.ARCHIVE))

        val outcome = fixture.locateVault(missingPath = VaultFixture.ARCHIVE, newPath = VaultFixture.WORK)

        assertEquals(
            expected = true,
            actual = outcome is Outcome.Success
        )
        assertEquals(
            expected = listOf(VaultFixture.WORK),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `a failed locate keeps the old entry`() = runTest {
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.ARCHIVE))

        fixture.locateVault(missingPath = VaultFixture.ARCHIVE, newPath = VaultFixture.WORK)

        assertEquals(
            expected = listOf(VaultFixture.ARCHIVE),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `locating a vault at the same path keeps it`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.NOTES))

        fixture.locateVault(missingPath = VaultFixture.NOTES, newPath = VaultFixture.NOTES)

        assertEquals(
            expected = listOf(VaultFixture.NOTES),
            actual = fixture.store.recentVaults.map { it.path }
        )
    }

    @Test
    fun `the last vault is restored when it can be opened`() = runTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.NOTES))

        assertEquals(
            expected = Vault.fromPath(path = VaultFixture.NOTES),
            actual = fixture.restoreLastVault()
        )
    }

    @Test
    fun `nothing is restored without recents or when the last folder is gone`() = runTest {
        assertNull(actual = fixture.restoreLastVault())

        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.ARCHIVE))

        assertNull(actual = fixture.restoreLastVault())
    }
}
