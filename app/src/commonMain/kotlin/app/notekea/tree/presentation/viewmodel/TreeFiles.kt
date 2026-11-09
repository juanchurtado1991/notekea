package app.notekea.tree.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.FileRevealer

/**
 * The sidebar's entries outside Notekea: a file that is not a note opens with its app and an entry
 * is shown in the file manager. Both take the selected entry when no path is given, and show a failure as the
 * sidebar's action error.
 */
internal class TreeFiles(
    private val host: TreeHost,
    private val vaultPath: String,
    private val fileOpener: FileOpener,
    private val fileRevealer: FileRevealer
) {
    fun openWithApp(path: RelativePath?) {
        val target = path ?: host.state.value.selectedPath ?: return

        host.launch {
            val outcome = fileOpener.open(
                vaultPath = vaultPath,
                path = target
            )

            if (outcome is Outcome.Failure) {
                host.reduce { it.copy(actionError = outcome.error) }
            }
        }
    }

    fun showInFileManager(path: RelativePath?) {
        val target = path ?: host.state.value.selectedPath ?: return

        host.launch {
            val outcome = fileRevealer.reveal(
                vaultPath = vaultPath,
                path = target
            )

            if (outcome is Outcome.Failure) {
                host.reduce { it.copy(actionError = outcome.error) }
            }
        }
    }
}
