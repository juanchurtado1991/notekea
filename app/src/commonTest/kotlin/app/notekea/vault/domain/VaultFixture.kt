package app.notekea.vault.domain

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.DescribeElapsedTimeUseCase
import kotlin.time.Instant

/** Real vault use cases wired to the fakes, shared by the domain and ViewModel tests. */
class VaultFixture {
    val clock = FakeClock(currentTime = Instant.fromEpochMilliseconds(epochMilliseconds = NOW_MILLIS))
    val fileSystem = FakeVaultFileSystem()
    val folderPicker = FakeFolderPicker()
    val store = FakeRecentVaultsStore()

    val openVault = OpenVaultUseCase(vaultFileSystem = fileSystem, recentVaultsStore = store, clock = clock)
    val validateFileName = ValidateFileNameUseCase()
    val createVault = CreateVaultUseCase(
        vaultFileSystem = fileSystem,
        validateFileName = validateFileName,
        openVault = openVault
    )
    val getRecentVaults = GetRecentVaultsUseCase(
        recentVaultsStore = store,
        vaultFileSystem = fileSystem,
        describeElapsedTime = DescribeElapsedTimeUseCase(clock = clock)
    )
    val removeRecentVault = RemoveRecentVaultUseCase(recentVaultsStore = store)
    val locateVault = LocateVaultUseCase(openVault = openVault, removeRecentVault = removeRecentVault)
    val restoreLastVault = RestoreLastVaultUseCase(recentVaultsStore = store, openVault = openVault)

    fun addFolder(
        path: String,
        access: FolderAccess = FolderAccess.AVAILABLE
    ) {
        fileSystem.folders[path] = access
    }

    fun recent(
        path: String
    ): RecentVault = RecentVault(path = path, lastOpenedAt = clock.currentTime)

    companion object {
        const val ARCHIVE = "/home/ana/Old/Archive"
        const val NOTES = "/home/ana/Notes"
        const val NOW_MILLIS = 1_800_000_000_000L
        const val WORK = "/home/ana/Work"
    }
}
