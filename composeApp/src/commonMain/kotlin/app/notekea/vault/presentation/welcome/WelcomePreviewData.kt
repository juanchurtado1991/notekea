package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.vault.domain.RecentVaultStatus
import app.notekea.vault.domain.Vault

/** Sample values shared by the welcome flow's previews, so every preview shows the same believable vaults. */
internal object WelcomePreviewData {
    private const val ARCHIVE_MONTHS = 3
    private const val ARCHIVE_PATH = "/home/ana/Old/Archive"
    private const val DOCUMENTS_PATH = "/home/ana/Documents"
    private const val NEW_VAULT_NAME = "My Notes"
    private const val NOTES_PATH = "/home/ana/Notes"
    private const val RECENT_DAYS = 2
    private const val WORK_PATH = "/home/ana/Work/Obsidian"

    val platformConventions = PreviewPlatformConventions.linux

    val openedVault = Vault.fromPath(path = NOTES_PATH)

    val recentVaults = listOf(
        RecentVaultStatus(
            vault = Vault.fromPath(path = NOTES_PATH),
            lastOpened = ElapsedTime.Days(count = RECENT_DAYS),
            isMissing = false
        ),
        RecentVaultStatus(
            vault = Vault.fromPath(path = WORK_PATH),
            lastOpened = ElapsedTime.Weeks(count = 1),
            isMissing = false
        ),
        RecentVaultStatus(
            vault = Vault.fromPath(path = ARCHIVE_PATH),
            lastOpened = ElapsedTime.Months(count = ARCHIVE_MONTHS),
            isMissing = true
        )
    )

    val newVaultForm = NewVaultForm(name = NEW_VAULT_NAME, parentPath = DOCUMENTS_PATH)

    fun state(): WelcomeState = WelcomeState(platformConventions = platformConventions, isLoading = false)
}
