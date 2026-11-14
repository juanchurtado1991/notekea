package app.notekea.core.preferences.domain

/**
 * Settings → General and Editor (5.2): the [language], the last vault at startup, the note's [textSize] and
 * [lineWidth], whether the welcome was seen, how often the web sources are updated ([webUpdate]), and whether agent
 * mode works with the one loop ([chatLoop], a preview until it replaces today's agent: `docs/agent-loop.md`).
 */
data class AppPreferences(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val opensLastVault: Boolean = true,
    val textSize: Int = DEFAULT_TEXT_SIZE,
    val lineWidth: LineWidth = LineWidth.WIDE,
    val hasSeenWelcome: Boolean = false,
    val webUpdate: WebUpdateEvery = WebUpdateEvery.NEVER,
    val chatLoop: Boolean = false
) {
    companion object {
        const val DEFAULT_TEXT_SIZE = 16
        val TEXT_SIZES = listOf(13, 14, 15, 16, 17, 18, 20, 22)
    }
}
