package app.notekea.sync.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * What a commit of the whole vault did: whether there was anything to commit ([isMade]), and the files it left out
 * of the repository ([leftOut]) because no Git host takes a file that large. They stay in the vault, no longer synced.
 */
data class VaultCommit(
    val isMade: Boolean,
    val leftOut: List<RelativePath> = emptyList()
)
