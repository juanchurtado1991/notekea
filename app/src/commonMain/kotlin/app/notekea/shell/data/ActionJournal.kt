package app.notekea.shell.data

import app.notekea.chat.domain.plan.DonePlan
import app.notekea.core.notes.domain.RelativePath

/** What one plan did, and how to take it back, newest last. */
internal class ActionJournal {
    val created = mutableListOf<RelativePath>()
    val createdFolders = mutableListOf<RelativePath>()
    val changed = mutableListOf<RelativePath>()
    val moved = mutableListOf<Pair<RelativePath, RelativePath>>()
    val trashed = mutableListOf<RelativePath>()
    val undoSteps = mutableListOf<suspend (vaultPath: String) -> Unit>()

    fun plan(id: Long): DonePlan = DonePlan(
        id = id,
        created = created.toList(),
        changed = changed.distinct(),
        moved = moved.toList(),
        trashed = trashed.toList(),
        opened = created.firstOrNull() ?: moved
            .lastOrNull()
            ?.second
            ?.takeIf { it.isMarkdown }
    )
}
