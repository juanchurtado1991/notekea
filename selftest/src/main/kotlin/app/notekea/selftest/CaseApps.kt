package app.notekea.selftest

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/** The [CaseApp] each running case shows, by its data folder, so the case's verbs can reach its pickers. */
object CaseApps {
    private val apps = ConcurrentHashMap<Path, CaseApp>()

    fun register(app: CaseApp) {
        apps[app.vaults] = app
    }

    fun unregister(app: CaseApp) {
        apps.remove(app.vaults, app)
    }

    fun of(vaults: Path): CaseApp = requireNotNull(apps[vaults]) { "No Notekea is running on $vaults" }
}
