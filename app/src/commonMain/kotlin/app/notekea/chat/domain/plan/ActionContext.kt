package app.notekea.chat.domain.plan

import app.notekea.chat.domain.ChatExchange
import app.notekea.core.notes.domain.RelativePath

/**
 * What the plan may name in the vault, by name only (3.9): [today], the [openNote], its [folders], the [notes] closest
 * to the request (not all of a large vault), its [properties], [tags], the app's [themes], the chat so far
 * ([history]) and where the person is ([working], `WorkContext.described`).
 */
data class ActionContext(
    val today: String,
    val openNote: String? = null,
    val latestNote: String? = null,
    val changedToday: List<String> = emptyList(),
    val folders: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val properties: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val history: List<ChatExchange> = emptyList(),
    val language: String? = null,
    val working: String? = null
) {
    /** The context as the model reads it, a line each. */
    fun described(): String = listOfNotNull(
        "Today: $today",
        working,
        openNote?.let { "Open note: $it" },
        latestNote?.let { "Last changed note: $it" },
        "Notes changed today: ${changedToday.take(n = MAX_NAMES).joinToString().ifEmpty { "none" }}",
        listed(
            label = "Folders",
            names = folders
        ),
        listed(
            label = "Notes",
            names = notes
        ),
        listed(
            label = "Properties",
            names = properties
        ),
        listed(
            label = "Tags",
            names = tags
        ),
        listed(
            label = "Themes",
            names = themes
        ),
        history
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                prefix = "Earlier in this chat:\n",
                separator = "\n"
            ) { "Q: ${it.question}\nA: ${it.reply}" }
    ).joinToString(separator = "\n")

    private fun listed(
        label: String,
        names: List<String>
    ): String? = names
        .takeIf { it.isNotEmpty() }
        ?.take(n = MAX_NAMES)
        ?.joinToString(
            prefix = "$label: ",
            separator = ", "
        )

    companion object {
        private const val MAX_NAMES = 60

        /** Every folder of [paths] up to the vault's root: Projects holds only Projects/Garden, yet may be named. */
        fun foldersOf(paths: List<RelativePath>): List<String> = paths
            .flatMap { path ->
                generateSequence(seed = path.parent) { it.parent }
                    .takeWhile {
                        !it.isRoot
                    }
                    .map { it.value }
            }
            .distinct()
            .sorted()
    }
}
