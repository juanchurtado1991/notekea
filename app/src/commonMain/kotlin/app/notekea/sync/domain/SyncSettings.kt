package app.notekea.sync.domain

/**
 * How a vault syncs (4.1): the HTTPS address of its Git remote, whether it syncs by itself ([isAuto]: a commit 10 s
 * after the last change, a pull when it opens and every [intervalMinutes]) or only with Sync now.
 */
data class SyncSettings(
    val remoteUrl: String,
    val isAuto: Boolean = true,
    val intervalMinutes: Int = DEFAULT_INTERVAL_MINUTES
) {
    companion object {
        const val DEFAULT_INTERVAL_MINUTES = 5
        val INTERVALS = listOf(1, 5, 15, 30)
    }
}
