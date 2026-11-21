package app.notekea.core.preferences.domain

/**
 * Settings (5.2): the [language], the last vault at startup, the note's [textSize] and [lineWidth], the welcome
 * seen, the web sources' updates ([webUpdate]), agent mode on the one loop ([chatLoop], `docs/agent-loop.md`), and
 * the chat's memory between conversations ([chatMemory], `docs/agent-memory.md`).
 */
data class AppPreferences(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val opensLastVault: Boolean = true,
    val textSize: Int = DEFAULT_TEXT_SIZE,
    val lineWidth: LineWidth = LineWidth.WIDE,
    val hasSeenWelcome: Boolean = false,
    val webUpdate: WebUpdateEvery = WebUpdateEvery.NEVER,
    val chatLoop: Boolean = false,
    val chatMemory: Boolean = true
) {
    companion object {
        const val DEFAULT_TEXT_SIZE = 16
        val TEXT_SIZES = listOf(13, 14, 15, 16, 17, 18, 20, 22)
    }
}
