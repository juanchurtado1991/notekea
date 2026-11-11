package app.notekea.core.preferences.domain

/**
 * Settings → General and Editor (5.2): the [language], whether the last vault opens at startup ([opensLastVault]),
 * the note's [textSize] (points) and [lineWidth], and whether the first-run welcome was seen ([hasSeenWelcome]).
 */
data class AppPreferences(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val opensLastVault: Boolean = true,
    val textSize: Int = DEFAULT_TEXT_SIZE,
    val lineWidth: LineWidth = LineWidth.WIDE,
    val hasSeenWelcome: Boolean = false
) {
    companion object {
        const val DEFAULT_TEXT_SIZE = 16
        val TEXT_SIZES = listOf(13, 14, 15, 16, 17, 18, 20, 22)
    }
}
