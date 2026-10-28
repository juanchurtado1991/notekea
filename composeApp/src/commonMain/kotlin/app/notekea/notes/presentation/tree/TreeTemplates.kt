package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.RelativePath

/**
 * New notes from a template in the sidebar (`ROADMAP.md` 1.12): the template picker opens for the selected folder, the
 * note is made with its name ready to type and opened, and the template fills it once its name is set ([apply]).
 */
internal class TreeTemplates(
    private val host: TreeHost,
    private val vaultPath: String,
    private val newNotes: NewNoteTools
) {
    // A note made from a template, and the template it is filled from once its name is set.
    private var pending: Pair<RelativePath, RelativePath>? = null

    /** The template picker opens for a new note in the selected folder. */
    fun choose() {
        host.emit(effect = VaultTreeEffect.ChooseTemplate(folder = targetFolder(state = host.state.value)))
    }

    /** Makes a note in the intent's folder with its name ready to type, opens it, and keeps its template for later. */
    fun create(
        intent: VaultTreeIntent.OnNoteFromTemplate
    ) {
        if (host.state.value.isSidebarCollapsed) host.updateTree { it.copy(isSidebarCollapsed = false) }
        host.launch {
            val outcome = newNotes.createNote(vaultPath = vaultPath, folder = intent.folder, baseName = intent.baseName)
            host.updateTree { reduceCreated(state = it, outcome = outcome, isFolder = false) }
            val path = (outcome as? Outcome.Success)?.value ?: return@launch
            pending = path to intent.template
            host.open(path = path)
        }
    }

    /** Fills the note made from a template, now [named], when [note] is that note; a failure shows in the banner. */
    suspend fun apply(
        note: RelativePath,
        named: RelativePath
    ) {
        val (path, template) = pending ?: return
        if (path != note) return
        pending = null
        val outcome = newNotes.applyTemplate(vaultPath = vaultPath, note = named, template = template)
        if (outcome is Outcome.Failure) host.reduce { it.copy(actionError = outcome.error) }
    }
}
