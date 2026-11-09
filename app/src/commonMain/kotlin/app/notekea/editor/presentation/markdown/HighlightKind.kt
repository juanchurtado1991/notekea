package app.notekea.editor.presentation.markdown

/** What a stretch of a note's Markdown is, for the editor's highlighting. */
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
    ITALIC,
    LINK,
    SYNTAX,
    TAG,
    WIKI_LINK
}
