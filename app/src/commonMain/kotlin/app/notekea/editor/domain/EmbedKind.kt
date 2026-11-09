package app.notekea.editor.domain

/** How an embed is written: Obsidian's `![[file|300]]` or Markdown's `![alt](path)`. */
enum class EmbedKind {
    WIKI,
    MARKDOWN
}
