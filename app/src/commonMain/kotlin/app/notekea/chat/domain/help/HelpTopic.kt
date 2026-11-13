package app.notekea.chat.domain.help

/**
 * One topic of the app's guide: its [title], the [words] people say for it (English and Spanish), a request the chat
 * can do for them ([tryRequest], "…" where they finish it), and its [text], with `{name}` for a shortcut.
 */
data class HelpTopic(
    val title: String,
    val words: List<String>,
    val text: String,
    val tryRequest: String?
)
