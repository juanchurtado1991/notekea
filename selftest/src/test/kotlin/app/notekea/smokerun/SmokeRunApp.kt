package app.notekea.smokerun

import androidx.compose.runtime.Composable
import app.notekea.selftest.CaseApp
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * The real app for an automated smoke run (`ROADMAP.md` 1.16): a [CaseApp] on copies of the smoke vaults named
 * [vaults] (and what [prepare] writes into their folder), with its data in a temporary folder instead of the user's.
 */
@OptIn(ExperimentalPathApi::class)
internal class SmokeRunApp(
    vaults: List<String>,
    prepare: (vaults: File) -> Unit = {}
) : AutoCloseable {
    private val root: Path = Files.createTempDirectory(TEMP_PREFIX)
    private val app: CaseApp

    init {
        vaults.forEach { name ->
            File(SMOKE_VAULTS, name).copyRecursively(target = root.resolve(VAULTS).resolve(name).toFile())
        }
        prepare(root.resolve(VAULTS).toFile())
        app = CaseApp(appData = root.resolve(APP_DATA), vaults = root.resolve(VAULTS))
    }

    val folderPicker get() = app.folderPicker
    val windowFocus get() = app.windowFocus

    /** Where the copy of the vault [name] is. */
    fun vaultPath(name: String): String = app.vaultPath(name = name)

    @Composable
    fun Content() = app.Content()

    /**
     * Stops the app. Its folder is deleted when the test process ends, not now: work the app started in the background
     * (the note cache's database) may still be finishing, and would fail on a folder already gone.
     */
    override fun close() {
        app.close()
        Runtime.getRuntime().addShutdownHook(Thread { root.deleteRecursively() })
    }

    companion object {
        private const val APP_DATA = "app-data"
        private const val SMOKE_VAULTS = "../smoke-data/vaults"
        private const val TEMP_PREFIX = "notekea-smoke-run"
        private const val VAULTS = "vaults"
    }
}
