package app.notekea.sync.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * A vault left in the middle of a merge or a rebase (the app killed, the power gone): its notes may hold Git's own
 * conflict marks, and nothing can be synced until it is repaired. [changedHere] are the files changed on this
 * computer and not on the remote, which going back to the remote would replace.
 */
data class UnfinishedSync(val changedHere: List<RelativePath>)
