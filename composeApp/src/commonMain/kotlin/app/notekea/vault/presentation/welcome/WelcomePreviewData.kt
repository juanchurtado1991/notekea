package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier
import app.notekea.vault.domain.OpenedVault
import app.notekea.vault.domain.RecentVaultStatus
import app.notekea.vault.domain.Vault

/** Sample values shared by the welcome flow's previews, so every preview shows the same believable vaults. */
internal object WelcomePreviewData {
    private const val APP_DATA = "/home/ana/.local/share/notekea"
    private const val ARCHIVE_MONTHS = 3
    private const val ARCHIVE_PATH = "/home/ana/Old/Archive"
    private const val DOCUMENTS_PATH = "/home/ana/Documents"
    private const val HOME = "/home/ana"
    private const val NEW_VAULT_NAME = "My Notes"
    private const val NOTES_COUNT = 128
    private const val NOTES_PATH = "/home/ana/Notes"
    private const val RECENT_DAYS = 2
    private const val WORK_PATH = "/home/ana/Work/Obsidian"

    val platformConventions = PlatformConventions(
        operatingSystem = OperatingSystem.LINUX,
        primaryModifier = ShortcutModifier.CONTROL,
        openFolderShortcut = Shortcut(key = ShortcutKey.O, modifiers = setOf(ShortcutModifier.CONTROL)),
        quitShortcut = Shortcut(key = ShortcutKey.Q, modifiers = setOf(ShortcutModifier.CONTROL)),
        newNoteShortcut = Shortcut(key = ShortcutKey.N, modifiers = setOf(ShortcutModifier.CONTROL)),
        renameShortcut = Shortcut(key = ShortcutKey.F2, modifiers = emptySet()),
        deleteShortcut = Shortcut(key = ShortcutKey.DELETE, modifiers = emptySet()),
        toggleSidebarShortcut = Shortcut(key = ShortcutKey.BACKSLASH, modifiers = setOf(ShortcutModifier.CONTROL)),
        dialogButtonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
        appDataDirectory = APP_DATA,
        homeDirectory = HOME
    )

    val openedVault = OpenedVault(vault = Vault.fromPath(path = NOTES_PATH), noteCount = NOTES_COUNT)

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
