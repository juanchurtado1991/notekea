package app.notekea.editor.presentation.markdown

/**
 * What a stretch of a note's Markdown is, for the editor's highlighting. [SYNTAX] is a mark the editor hides away from
 * the cursor; [MARKER] is a line's own mark (a quote's, a list's), which stays in some form.
 */
enum class HighlightKind {
    BOLD,
    CODE_BLOCK,
    CODE_INLINE,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    HEADING_4,
    HEADING_5,
    HEADING_6,
    HIGHLIGHT,
    ITALIC,
    LINK,
    MARKER,
    STRIKETHROUGH,
    SYNTAX,
    TAG,
    WIKI_LINK
}
