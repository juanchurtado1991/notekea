package app.notekea.notes.domain

/** How a link to a note is written, which decides how its target is resolved and rewritten. */
enum class LinkKind {
    /** `[text](<Recipes/Café con leche.md>)`: a Markdown link whose target is written as is, spaces included. */
    MARKDOWN_ANGLE,

    /** `[text](Recipes/Caf%C3%A9.md)`: a Markdown link whose target is URL-encoded. */
    MARKDOWN_ENCODED,

    /** `[[Note]]`, `[[folder/Note|alias]]`, `![[Note#Heading]]`: Obsidian's wiki links and embeds. */
    WIKI
}
