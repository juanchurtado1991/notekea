package app.notekea.tree.presentation.viewmodel

import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.CreateNoteUseCase

/**
 * What the sidebar needs to make notes: an empty one ([createNote]), filled from a template once it is named
 * ([applyTemplate], `ROADMAP.md` 1.12), given together so its constructor stays short.
 */
class NewNoteTools(
    val createNote: CreateNoteUseCase,
    val applyTemplate: ApplyTemplateUseCase
)
