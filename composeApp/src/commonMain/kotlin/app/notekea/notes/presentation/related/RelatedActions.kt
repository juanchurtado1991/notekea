package app.notekea.notes.presentation.related

import app.notekea.notes.domain.RelativePath

/** What a related note's card can do (`ROADMAP.md` 2.4): open it, open it beside the note, or link to it there. */
class RelatedActions(
    val onOpen: (RelativePath) -> Unit,
    val onOpenBeside: (RelativePath) -> Unit,
    val onInsertLink: (RelativePath) -> Unit
)
