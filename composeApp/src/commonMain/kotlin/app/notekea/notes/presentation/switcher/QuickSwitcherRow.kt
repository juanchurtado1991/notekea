package app.notekea.notes.presentation.switcher

import app.notekea.notes.domain.RelativePath

/**
 * One result of the palette, ready to show: the note's [name] with its matched characters ([nameRanges]), its
 * [folder] (null at the vault root), and a [detail] line with its own [detailRanges]: the alias or path that matched,
 * or the matching line of the content. A recent note has no highlight and no detail.
 */
data class QuickSwitcherRow(
    val path: RelativePath,
    val name: String,
    val nameRanges: List<IntRange> = emptyList(),
    val detailRanges: List<IntRange> = emptyList(),
    val folder: String? = null,
    val detail: String? = null
)
