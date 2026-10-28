package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.DailyNoteSettings
import app.notekea.notes.domain.DailyNotes

/**
 * Daily notes in the sidebar (`ROADMAP.md` 1.10): "Today" opens today's note, creating it first if needed, and a
 * selected daily note knows the daily notes before and after it, to step through them.
 */
internal class TreeDays(
    private val host: TreeHost,
    private val vaultPath: String,
    private val days: DailyNoteTools
) {
    private val dailyNotes = DailyNotes()
    private var settings = DailyNoteSettings.DEFAULT

    // What the day neighbors were last found for (the notes by identity), so moving through the tree does not read
    // every note name again.
    private var inputs: Triple<Any?, Any?, DailyNoteSettings>? = null

    suspend fun loadSettings() {
        settings = days.getDailyNoteSettings(vaultPath = vaultPath)
    }

    fun onIntent(
        intent: VaultTreeIntent
    ) {
        if (intent is VaultTreeIntent.OnDayStep) step(step = intent.step) else openToday()
    }

    /** Finds the selected note's day neighbors when the selection, the notes or the settings changed. */
    fun update() {
        val current = host.state.value
        val last = inputs
        val isSame = last != null && last.first == current.selectedPath && last.second === current.tree.notes &&
            last.third == settings
        if (isSame) return
        inputs = Triple(current.selectedPath, current.tree.notes, settings)
        val neighbors = current.selectedPath?.let {
            dailyNotes.neighborsOf(path = it, notes = current.tree.notes.keys, settings = settings)
        }
        if (neighbors != current.dayNeighbors) host.reduce { it.copy(dayNeighbors = neighbors) }
    }

    /** Today's daily note, with the settings read again: Obsidian may have changed them since the vault opened. */
    private fun openToday() {
        host.launch {
            loadSettings()
            val outcome = days.openDailyNote(vaultPath = vaultPath)
            host.updateTree { reduceFollowedLink(state = it, outcome = outcome) }
            host.open(path = (outcome as? Outcome.Success)?.value?.path)
        }
    }

    private fun step(
        step: Int
    ) {
        val neighbors = host.state.value.dayNeighbors ?: return
        val target = (if (step < 0) neighbors.previous else neighbors.next) ?: return
        host.reveal(path = target)
    }
}
