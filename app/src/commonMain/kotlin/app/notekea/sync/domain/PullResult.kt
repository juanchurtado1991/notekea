package app.notekea.sync.domain

import app.notekea.core.notes.domain.RelativePath

/** What a pull brought: how many files it [changed], and the notes changed on both sides ([conflicts]). */
data class PullResult(
    val changed: Int,
    val conflicts: List<RelativePath>
)
