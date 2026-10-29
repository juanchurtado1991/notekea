package com.testbuddy

import androidx.compose.runtime.Composable
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** The builder of [testBuddy]: each call adds to the suite, and every case ID may be used once. */
@TestBuddyDsl
public class SuiteBuilder internal constructor(
    private val appName: String,
    private val version: String
) {
    private val automated = mutableListOf<AutomatedCase>()
    private val manual = mutableListOf<ManualCase>()
    private val dataSources = mutableListOf<Path>()
    private var window = WindowSize(width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT)
    private var backUp: () -> List<Path> = { emptyList() }
    private var installedApp: (HostSystem) -> Path? = { null }
    private var content: (@Composable (CaseFolders) -> Unit)? = null

    /** Shows the app on a case's [CaseFolders]: its data folder must be [CaseFolders.appData], never the real one. */
    public fun launch(content: @Composable (folders: CaseFolders) -> Unit) {
        this.content = content
    }

    /** A folder whose contents every case gets a fresh copy of, in [CaseFolders.data]. */
    public fun data(from: Path) {
        dataSources.add(from)
    }

    /** The size of the window automated cases show the app in. */
    public fun window(
        width: Int,
        height: Int
    ) {
        window = WindowSize(width = width, height = height)
    }

    /** What Finish and clean up puts back as it was: the installed app's own data, backed up before manual cases. */
    public fun backUp(paths: () -> List<Path>) {
        backUp = paths
    }

    /**
     * Where the installed app's program is on each system (`/Applications/Notes.app/Contents/MacOS/Notes`,
     * `C:\Program Files\Notes\Notes.exe`): the manual cases get an "Open <app>" button that starts it, its output
     * kept in the Report folder. No path, or one that does not exist, hides the button.
     */
    public fun installedApp(path: (HostSystem) -> Path?) {
        installedApp = path
    }

    public fun automated(
        id: String,
        title: String,
        timeout: Duration = DEFAULT_TIMEOUT,
        window: WindowSize? = null,
        body: CaseScope.() -> Unit
    ) {
        automated.add(
            AutomatedCase(id = checkedId(id = id), title = title, timeout = timeout, window = window, body = body)
        )
    }

    public fun manual(
        id: String,
        reason: ManualReason,
        build: ManualCaseBuilder.() -> Unit
    ) {
        manual.add(ManualCaseBuilder(id = checkedId(id = id), reason = reason).apply(build).build())
    }

    internal fun build(): Suite = Suite(
        appName = appName,
        version = version,
        automated = automated.toList(),
        manual = manual.toList(),
        window = window,
        dataSources = dataSources.toList(),
        backUp = backUp,
        installedApp = installedApp,
        content = requireNotNull(content) { "The suite of $appName does not say how to launch it" }
    )

    private fun checkedId(id: String): String {
        require(id.isNotBlank() && id.none(Char::isWhitespace)) { "A case ID has no spaces: \"$id\"" }
        val isTaken = automated.any { it.id == id } || manual.any { it.id == id }
        require(!isTaken) { "Two cases use the ID $id" }
        return id
    }

    private companion object {
        const val DEFAULT_WIDTH = 1280
        const val DEFAULT_HEIGHT = 800
        val DEFAULT_TIMEOUT = 2.minutes
    }
}
