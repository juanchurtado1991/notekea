package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase

/** What the sidebar needs to update the links after a rename (`ROADMAP.md` 2.5): to plan them, and to write them. */
class LinkUpdateTools(
    val planLinkUpdates: PlanLinkUpdatesUseCase,
    val applyLinkUpdates: ApplyLinkUpdatesUseCase
)
