package app.notekea.notes.presentation.editor

/** What a stretch of a note's Markdown is, for the editor's highlighting (`ROADMAP.md` 1.5). */
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
