package app.notekea.editor.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.editor.domain.RenameAndUpdateLinksUseCase
import app.notekea.editor.presentation.model.EditorEffect

/**
 * The title renames the note. It saves first ([saveNow]), so the file that moves holds every edit; the links to the
 * note follow the new name, and the watch of the file on disk ([EditorDisk]) moves to the new path.
 */
internal class EditorRenaming(
    private val host: EditorHost,
    private val vaultPath: String,
    private val renameAndUpdateLinks: RenameAndUpdateLinksUseCase,
    private val disk: EditorDisk,
    private val saveNow: suspend () -> Unit
) {
    suspend fun rename(name: String) {
        val from = host.state.value.notePath ?: return
        if (name.trim() == from.nameWithoutExtension) return
        saveNow()

        when (
            val outcome = renameAndUpdateLinks(
                vaultPath = vaultPath,
                path = from,
                newName = name
            )
        ) {
            is Outcome.Failure -> host.reduce { it.copy(titleError = outcome.error) }

            is Outcome.Success -> {
                val renamed = outcome.value
                disk.stop()

                host.reduce {
                    it.copy(
                        notePath = renamed.path,
                        titleError = null
                    )
                }

                if (renamed.path in renamed.links?.updated.orEmpty()) {
                    disk.followRewrite(path = renamed.path)
                }

                disk.watch(path = renamed.path)

                host.emit(
                    effect = EditorEffect.Renamed(
                        from = from,
                        to = renamed.path,
                        links = renamed.links
                    )
                )
            }
        }
    }
}
