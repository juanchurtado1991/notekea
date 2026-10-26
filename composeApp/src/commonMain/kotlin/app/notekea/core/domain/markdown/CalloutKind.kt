package app.notekea.core.domain.markdown

/**
 * The callout types of Obsidian and the names that mean the same ([aliases]), as written after `[!`. [of] reads a
 * written type, ignoring letter case; an unknown type is a [NOTE], so it never shows as broken text.
 */
enum class CalloutKind(val aliases: List<String>) {
    NOTE(aliases = emptyList()),
    ABSTRACT(aliases = listOf("summary", "tldr")),
    INFO(aliases = emptyList()),
    TODO(aliases = emptyList()),
    TIP(aliases = listOf("hint", "important")),
    SUCCESS(aliases = listOf("check", "done")),
    QUESTION(aliases = listOf("help", "faq")),
    WARNING(aliases = listOf("caution", "attention")),
    FAILURE(aliases = listOf("fail", "missing")),
    DANGER(aliases = listOf("error")),
    BUG(aliases = emptyList()),
    EXAMPLE(aliases = emptyList()),
    QUOTE(aliases = listOf("cite"));

    companion object {
        fun of(
            type: String
        ): CalloutKind {
            val written = type.lowercase()
            return entries.firstOrNull { it.name.lowercase() == written || written in it.aliases } ?: NOTE
        }
    }
}
