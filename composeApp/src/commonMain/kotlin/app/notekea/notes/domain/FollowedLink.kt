package app.notekea.notes.domain

import app.notekea.core.notes.domain.RelativePath

/** The note a clicked link leads to; [isNew] when it did not exist and was created by the click. */
data class FollowedLink(
    val path: RelativePath,
    val isNew: Boolean
)
