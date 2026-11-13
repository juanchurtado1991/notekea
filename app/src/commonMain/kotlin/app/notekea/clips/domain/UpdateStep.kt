package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/** Where an update of the web sources stands. */
sealed interface UpdateStep {
    /** Page [done] + 1 of [total] is being read again. */
    data class Reading(
        val done: Int,
        val total: Int,
        val url: String
    ) : UpdateStep

    /**
     * It ended: the notes whose page changed ([updated]), the notes of pages that are new ([added]), how many were
     * the same and how many pages could not be read.
     */
    data class Done(
        val updated: List<RelativePath> = emptyList(),
        val added: List<RelativePath> = emptyList(),
        val unchanged: Int = 0,
        val failed: Int = 0
    ) : UpdateStep
}
