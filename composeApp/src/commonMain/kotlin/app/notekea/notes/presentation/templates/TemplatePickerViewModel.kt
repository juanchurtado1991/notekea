package app.notekea.notes.presentation.templates

import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.CreateTemplatesFolderUseCase
import app.notekea.notes.domain.FuzzyMatcher
import app.notekea.notes.domain.GetTemplateSettingsUseCase
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TemplateListing
import app.notekea.notes.domain.TemplateSettings
import app.notekea.notes.domain.Templates
import app.notekea.notes.domain.VaultTree
import co.touchlab.kermit.Logger

/**
 * The template picker (`ROADMAP.md` 1.12), opened with `Mod+T`, the toolbar's Template button or a folder's "New note
 * from template": the notes of the vault's templates folder, found by name as in the quick switcher. The settings
 * (`.obsidian/templates.json`) are read again at every opening, since Obsidian may change them; the notes come from the
 * vault tree ([TemplatePickerIntent.OnTreeChange]). Without a templates folder it offers to create one with an example.
 */
class TemplatePickerViewModel(
    private val vaultPath: String,
    private val getTemplateSettings: GetTemplateSettingsUseCase,
    private val createTemplatesFolder: CreateTemplatesFolderUseCase,
    private val fuzzyMatcher: FuzzyMatcher
) : BaseViewModel<TemplatePickerState, TemplatePickerIntent, TemplatePickerEffect>(
    initialState = TemplatePickerState()
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val templates = Templates()
    private var tree = VaultTree.EMPTY
    private var settings = TemplateSettings.DEFAULT

    override fun onIntent(
        intent: TemplatePickerIntent
    ) {
        when (intent) {
            is TemplatePickerIntent.OnOpen -> open(target = intent.target)
            TemplatePickerIntent.OnClose -> close(effect = TemplatePickerEffect.Dismissed)
            TemplatePickerIntent.OnConfirm -> choose(index = state.value.selectedIndex)
            is TemplatePickerIntent.OnQueryChange -> reduce { rowsFor(state = it.copy(query = intent.query)) }
            is TemplatePickerIntent.OnRowClick -> choose(index = intent.index)
            TemplatePickerIntent.OnSelectNext -> reduce { moved(state = it, step = 1) }
            TemplatePickerIntent.OnSelectPrevious -> reduce { moved(state = it, step = -1) }
            is TemplatePickerIntent.OnCreateFolderClick -> createFolder(intent = intent)
            is TemplatePickerIntent.OnTreeChange -> changeTree(tree = intent.tree)
        }
    }

    private fun open(
        target: TemplateTarget
    ) {
        reduce { rowsFor(state = TemplatePickerState(isOpen = true, target = target, folder = settings.folder)) }
        launchGuarded(onError = ::showCreateFailure) {
            settings = getTemplateSettings(vaultPath = vaultPath)
            reduce { if (it.isOpen) rowsFor(state = it.copy(folder = settings.folder)) else it }
        }
    }

    private fun changeTree(
        tree: VaultTree
    ) {
        this.tree = tree
        if (state.value.isOpen) reduce { rowsFor(state = it) }
    }

    /** Makes the folder with its example, which is listed at once, before the vault watcher reports it. */
    private fun createFolder(
        intent: TemplatePickerIntent.OnCreateFolderClick
    ) {
        if (state.value.isCreating) return
        reduce { it.copy(isCreating = true, error = null) }
        launchGuarded(onError = ::showCreateFailure) {
            val outcome = createTemplatesFolder(
                vaultPath = vaultPath,
                exampleName = intent.exampleName,
                exampleText = intent.exampleText
            )
            when (outcome) {
                is Outcome.Failure -> reduce { it.copy(isCreating = false, error = outcome.error) }

                is Outcome.Success -> {
                    tree = tree.withNote(note = createdNote(path = outcome.value))
                    reduce { rowsFor(state = it.copy(isCreating = false)) }
                }
            }
        }
    }

    /** An unexpected failure while reading the settings or making the folder: reported as a failed operation. */
    private fun showCreateFailure(
        throwable: Throwable
    ) {
        logger.w(throwable = throwable) { UNEXPECTED_MESSAGE }
        reduce { it.copy(isCreating = false, error = NotesError.OperationFailed(path = settings.folder)) }
    }

    private fun choose(
        index: Int
    ) {
        val row = state.value.rows.getOrNull(index = index) ?: return
        close(effect = TemplatePickerEffect.TemplateChosen(template = row.path, target = state.value.target))
    }

    private fun close(
        effect: TemplatePickerEffect
    ) {
        reduce { TemplatePickerState(folder = settings.folder) }
        emit(effect = effect)
    }

    /** The rows for the state's query, best match first (all of them, in natural order, without a query). */
    private fun rowsFor(
        state: TemplatePickerState
    ): TemplatePickerState {
        val listing = templates.listing(tree = tree, settings = settings)
        val found = (listing as? TemplateListing.Found)?.templates.orEmpty()
            .map { TemplatePickerRow(path = it, name = templates.nameOf(template = it, settings = settings)) }
        val rows = found.mapNotNull { row ->
            fuzzyMatcher.match(query = state.query, text = row.name)?.let { row to it }
        }
            .sortedByDescending { (_, match) -> match.score }
            .map { (row, _) -> row }
        return state.copy(
            rows = rows,
            selectedIndex = 0,
            hasFolder = listing != TemplateListing.NoFolder,
            hasTemplates = found.isNotEmpty()
        )
    }

    /** Moves the selection by [step], wrapping around at both ends. */
    private fun moved(
        state: TemplatePickerState,
        step: Int
    ): TemplatePickerState {
        if (state.rows.isEmpty()) return state
        return state.copy(selectedIndex = (state.selectedIndex + step).mod(other = state.rows.size))
    }

    private fun createdNote(
        path: RelativePath
    ): NoteEntry = NoteEntry(file = NoteFile(path = path, size = null, modifiedAt = null), metadata = NoteMetadata.NONE)

    companion object {
        private const val LOG_TAG = "TemplatePicker"
        private const val UNEXPECTED_MESSAGE = "The template picker failed unexpectedly"
    }
}
