package app.notekea.core.presentation.platform

/** The window's title: what the active tab shows, the open vault, then the app; each only when there is one. */
object WindowTitle {
    private const val APP_NAME = "Notekea"
    private const val SEPARATOR = " — "

    fun of(
        vaultName: String?,
        activeTabName: String?
    ): String = listOfNotNull(activeTabName, vaultName, APP_NAME).joinToString(separator = SEPARATOR)
}
