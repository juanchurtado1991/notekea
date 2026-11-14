package app.notekea.tree.presentation.viewmodel

import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.CreateNoteUseCase
import app.notekea.tree.domain.DuplicateNoteUseCase
import app.notekea.tree.domain.MoveEntryUseCase

/**
 * What the sidebar needs to make notes: an empty one ([createNote]), filled from a template once it is named
 * ([applyTemplate]), where the vault keeps its templates ([vaultSettings]), a copy of one ([duplicateNote]) and an
 * entry dragged into a folder ([moveEntry]), given together.
 */
class NewNoteTools(
    val createNote: CreateNoteUseCase,
    val applyTemplate: ApplyTemplateUseCase,
    val vaultSettings: VaultSettingsReader,
    val duplicateNote: DuplicateNoteUseCase,
    val moveEntry: MoveEntryUseCase
)
