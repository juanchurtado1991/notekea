package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Density
import app.notekea.core.domain.StringPatterns
import app.notekea.notes.domain.EmbedFinder

/**
 * Room under a line that holds only an embedded file, in Edit mode (`ROADMAP.md` 1.8): the line from [lineStart] until
 * [lineEnd] is made [height] pixels taller, and [view] is drawn there. [key] names the embed by its place among the
 * note's embedded files, so its measured height survives typing above it.
 */
data class AttachmentSpace(
    val key: String,
    val lineStart: Int,
    val lineEnd: Int,
    val view: AttachmentView,
    val height: Int
) {
    /**
     * The style that makes this line tall enough for its file: the text keeps its own line at the top, then [gap],
     * the file and [gap] again. Null until the file has been measured.
     */
    fun paragraphStyle(
        lineHeight: Float,
        gap: Float,
        density: Density
    ): ParagraphStyle? {
        if (height <= 0) return null
        val total = with(density) { (lineHeight + gap + height + gap).toSp() }
        return ParagraphStyle(
            lineHeight = total,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Top,
                trim = LineHeightStyle.Trim.None
            )
        )
    }

    companion object {
        private const val KEY_SEPARATOR = ":"
        private val embedFinder = EmbedFinder()

        /**
         * The spaces of [text] as it is now, its embeds found again so lines never lag behind typing, matched with the
         * [views] read a moment ago in order by their target (an embed without a view, a missing note, gets no space);
         * [heights] are what each one measured when drawn.
         */
        fun of(
            text: String,
            views: List<AttachmentView>,
            heights: Map<String, Int>
        ): List<AttachmentSpace> {
            val shown = views.filter { it.embed.isOwnLine }
            // Without a file shown on its own line there is nothing to make room for, so the text is not scanned.
            if (shown.isEmpty()) return emptyList()
            var next = 0
            return embedFinder.embeds(text = text, includeNotes = true).filter { it.isOwnLine }.mapIndexedNotNull {
                    index,
                    embed
                ->
                val view = shown.getOrNull(index = next)?.takeIf { it.embed.target == embed.target }
                    ?: return@mapIndexedNotNull null
                next++
                val key = index.toString() + KEY_SEPARATOR + embed.target
                val lineStart =
                    text.lastIndexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = embed.range.first - 1) + 1
                val lineEnd =
                    text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = embed.range.last).takeIf { it >= 0 }
                        ?: text.length
                AttachmentSpace(
                    key = key,
                    lineStart = lineStart,
                    lineEnd = lineEnd,
                    view = view.copy(embed = embed),
                    height =
                    heights[key] ?: 0
                )
            }
        }
    }
}
