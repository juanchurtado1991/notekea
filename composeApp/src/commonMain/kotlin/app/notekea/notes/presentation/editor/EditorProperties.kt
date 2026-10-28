package app.notekea.notes.presentation.editor

import app.notekea.core.domain.StringPatterns
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * The note's properties above its text (`ROADMAP.md` 1.14): their fold, remembered for every note and run
 * ([isExpandedChosen]), the source view, and each edit written into the frontmatter at once; YAML typed in the source
 * is written once typing pauses, like the text, and any save before that writes it first ([writePendingSource]).
 */
internal class EditorProperties(
    private val host: EditorHost,
    private val saving: EditorSaving,
    private val noteInfo: EditorNoteInfo,
    private val preferences: EditorPreferencesStore
) {
    var isExpandedChosen = true
        private set

    private var sourceJob: Job? = null
    private var pendingSource: PropertyIntent.OnSourceEdit? = null

    /** Reads the fold the user last chose. */
    suspend fun loadFold() {
        isExpandedChosen = preferences.isPropertiesExpanded()
    }

    fun onIntent(
        intent: PropertyIntent
    ) {
        when (intent) {
            PropertyIntent.OnFoldToggle -> toggleFold()

            PropertyIntent.OnSourceToggle -> host.reduce { it.copy(isSourceShown = !it.isSourceShown) }

            is PropertyIntent.OnSourceEdit -> editSourceLater(intent = intent)

            else -> {
                cancelSourceEdit()
                saving.runSave { edit(intent = intent) }
            }
        }
    }

    /** Writes the YAML typed in the source and not written yet, before any other save. */
    suspend fun writePendingSource() {
        sourceJob?.cancel()
        sourceJob = null
        val intent = pendingSource ?: return
        pendingSource = null
        edit(intent = intent)
    }

    private fun toggleFold() {
        isExpandedChosen = !isExpandedChosen
        host.reduce { it.copy(isPropertiesExpanded = isExpandedChosen) }
        host.launch { preferences.setPropertiesExpanded(isExpanded = isExpandedChosen) }
    }

    /** Typed YAML is written once typing pauses, like the note's text; the next keystroke waits again. */
    private fun editSourceLater(
        intent: PropertyIntent.OnSourceEdit
    ) {
        sourceJob?.cancel()
        pendingSource = intent
        sourceJob = host.launch {
            delay(duration = EditorViewModel.AUTOSAVE_DELAY)
            sourceJob = null
            saving.alone { writePendingSource() }
        }
    }

    private fun cancelSourceEdit() {
        sourceJob?.cancel()
        sourceJob = null
        pendingSource = null
    }

    /**
     * Writes the frontmatter after a property edit at once, with the body as it is, so it is saved like any save (never
     * over an outside change); nothing when the edit changes nothing or is not one to make.
     */
    private suspend fun edit(
        intent: PropertyIntent
    ) {
        val state = host.state.value
        val path = state.notePath ?: return
        val document = saving.opened ?: return
        if (state.isLoading || state.hasConflict || state.isDeleted) return
        if (sourcePathOf(intent = intent).let { it != null && it != path }) return
        val lineBreak = if (document.usesCrLf) StringPatterns.WINDOWS_NEW_LINE else StringPatterns.NEW_LINE
        val header = saving.propertyEditing.headerAfter(
            intent,
            document.header,
            state.properties,
            noteInfo.today(),
            lineBreak
        )
        if (header == null || header == document.header) return
        saving.write(
            path = path,
            document = document.copy(header = header, body = host.buffer.text.toString()),
            expectedOnDisk = saving.onDisk
        )
    }

    /** The note a source view edit was typed in; null for the form's edits, which always concern the open note. */
    private fun sourcePathOf(
        intent: PropertyIntent
    ): RelativePath? = when (intent) {
        is PropertyIntent.OnSourceEdit -> intent.path
        is PropertyIntent.OnSourceCommit -> intent.path
        else -> null
    }
}
