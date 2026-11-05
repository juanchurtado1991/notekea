package app.notekea.search.presentation.switcher

import app.notekea.core.notes.domain.RelativePath

/**
 * One result of the palette, ready to show: the note's [name] with its matched characters ([nameRanges]), its
 * [folder] (null at the vault root), and a [detail] line with its own [detailRanges]: the alias or path that matched,
 * or the matching line of the content. A recent note has no highlight and no detail. [passage] is the part of the body
 * the note opens at, when the hybrid search found one.
 */
data class QuickSwitcherRow(
    val path: RelativePath,
    val name: String,
    val nameRanges: List<IntRange> = emptyList(),
    val detailRanges: List<IntRange> = emptyList(),
    val folder: String? = null,
    val detail: String? = null,
    val passage: IntRange? = null
)
