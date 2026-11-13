package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.RelativePath

/** What a step names, found in the vault. */
data class StepTargets(
    val notes: List<RelativePath> = emptyList(),
    val folder: RelativePath? = null,
    val target: RelativePath? = null,
    val template: RelativePath? = null
)
