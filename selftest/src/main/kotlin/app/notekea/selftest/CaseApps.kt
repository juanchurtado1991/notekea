package app.notekea.selftest

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/** The [CaseApp] each running case shows, by its data folder, so the case's verbs can reach it and quit it. */
object CaseApps {
    private val apps = ConcurrentHashMap<Path, CaseApp>()
    private val quits = ConcurrentHashMap<CaseApp, QuitRequest>()

    fun register(app: CaseApp): QuitRequest {
        apps[app.vaults] = app
        return QuitRequest().also { quits[app] = it }
    }

    fun unregister(app: CaseApp) {
        apps.remove(app.vaults, app)
        quits.remove(app)
    }

    fun of(vaults: Path): CaseApp = requireNotNull(apps[vaults]) { "No Notekea is running on $vaults" }

    /** Asks the Notekea on [vaults] to quit, run [whileClosed] and start again, and returns the one that quits. */
    fun quit(
        vaults: Path,
        whileClosed: () -> Unit
    ): CaseApp {
        val app = of(vaults = vaults)
        requireNotNull(quits[app]).request(whileClosed = whileClosed)
        return app
    }
}
